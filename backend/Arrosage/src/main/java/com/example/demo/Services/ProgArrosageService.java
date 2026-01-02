package com.example.demo.Services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Clients.MeteoClient;
import com.example.demo.Clients.WeatherClient;
import com.example.demo.DTOs.ChangementConditionsEvent;
import com.example.demo.DTOs.PrevisionDTO;
import com.example.demo.DTOs.WeatherDTO;
import com.example.demo.Entities.JournalArrosage;
import com.example.demo.Entities.ProgrammeArrosage;
import com.example.demo.Repositories.JournalRepo;
import com.example.demo.Repositories.ProgArrosageRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProgArrosageService {
	
	@Autowired
	private ProgArrosageRepo progArrosageRepo;
	
	@Autowired
	private JournalRepo journalRepo;
	
	@Autowired
	private MeteoClient meteoClient;
	
	@Autowired
	private WeatherClient weatherClient;
	
	// Créer un programme d'arrosage avec planification automatique
	public ProgrammeArrosage createProgrammeAvecPlanificationAuto(ProgrammeArrosage programme) {
		// Récupérer les prévisions météo via OpenFeign (Communication Synchrone)
		List<PrevisionDTO> previsions = meteoClient.getPrevisionsByStationAndDate(
				programme.getStationMeteoId(), 
				programme.getDatePlanifiee().toLocalDate()
		);
		
		// Ajuster le programme en fonction de la météo
		if (!previsions.isEmpty()) {
			PrevisionDTO prevision = previsions.get(0);
			ajusterProgrammeSelonPrevision(programme, prevision);
		}
		
		programme.setStatut("PLANIFIE");
		return progArrosageRepo.save(programme);
	}
	
	// Planifier automatiquement selon les prévisions météo
	public ProgrammeArrosage planifierArrosageAutomatique(Long parcelleId, Long stationMeteoId, LocalDate date) {
		// Appel synchrone vers le microservice Météo via OpenFeign
		List<PrevisionDTO> previsions = meteoClient.getPrevisionsByStationAndDate(stationMeteoId, date);
		
		ProgrammeArrosage programme = new ProgrammeArrosage();
		programme.setParcelleId(parcelleId);
		programme.setStationMeteoId(stationMeteoId);
		programme.setDatePlanifiee(date.atTime(6, 0)); // Par défaut à 6h du matin
		programme.setStatut("PLANIFIE");
		
		if (!previsions.isEmpty()) {
			PrevisionDTO prevision = previsions.get(0);
			ajusterProgrammeSelonPrevision(programme, prevision);
		} else {
			// Valeurs par défaut si pas de prévision
			programme.setDuree(30); // 30 minutes
			programme.setVolumePrevu(500.0); // 500 litres
		}
		
		return progArrosageRepo.save(programme);
	}
	
	// Ajuster le programme selon la prévision météo
	private void ajusterProgrammeSelonPrevision(ProgrammeArrosage programme, PrevisionDTO prevision) {
		double pluiePrevue = prevision.getPluiePrevue() != null ? prevision.getPluiePrevue() : 0;
		double temperatureMax = prevision.getTemperatureMax() != null ? prevision.getTemperatureMax() : 25;
		
		// Logique d'ajustement basée sur les conditions météo
		if (pluiePrevue > 10) {
			// Forte pluie : annuler l'arrosage
			programme.setDuree(0);
			programme.setVolumePrevu(0.0);
			programme.setStatut("ANNULE");
		} else if (pluiePrevue > 5) {
			// Pluie modérée : réduire de 70%
			programme.setDuree(10);
			programme.setVolumePrevu(150.0);
		} else if (pluiePrevue > 0) {
			// Pluie légère : réduire de 50%
			programme.setDuree(15);
			programme.setVolumePrevu(250.0);
		} else if (temperatureMax > 35) {
			// Température très élevée : augmenter de 50%
			programme.setDuree(45);
			programme.setVolumePrevu(750.0);
		} else if (temperatureMax > 30) {
			// Température élevée : augmenter de 30%
			programme.setDuree(40);
			programme.setVolumePrevu(650.0);
		} else {
			// Conditions normales
			programme.setDuree(30);
			programme.setVolumePrevu(500.0);
		}
	}
	
	// Ajuster les programmes existants suite à un événement météo (Communication Asynchrone)
	public void ajusterProgrammesSelonMeteo(ChangementConditionsEvent event) {
		// Récupérer tous les programmes planifiés pour cette station
		List<ProgrammeArrosage> programmes = progArrosageRepo.findByStationMeteoId(event.getStationId());
		
		for (ProgrammeArrosage programme : programmes) {
			if (programme.getStatut().equals("PLANIFIE") && 
				programme.getDatePlanifiee().toLocalDate().equals(event.getDate())) {
				
				// Créer un DTO pour ajuster
				PrevisionDTO prevision = new PrevisionDTO();
				prevision.setDate(event.getDate());
				prevision.setTemperatureMax(event.getTemperatureMax());
				prevision.setTemperatureMin(event.getTemperatureMin());
				prevision.setPluiePrevue(event.getPluiePrevue());
				prevision.setVent(event.getVent());
				prevision.setStationId(event.getStationId());
				prevision.setNomStation(event.getNomStation());
				
				ajusterProgrammeSelonPrevision(programme, prevision);
				progArrosageRepo.save(programme);
				
				System.out.println("Programme " + programme.getId() + " ajusté : " + 
						programme.getDuree() + " min, " + programme.getVolumePrevu() + " L");
			}
		}
	}
	
	// Exécuter un programme d'arrosage
	public JournalArrosage executerProgramme(Long programmeId, Double volumeReel, String remarque) {
		ProgrammeArrosage programme = progArrosageRepo.findById(programmeId).orElse(null);
		
		if (programme != null) {
			programme.setStatut("TERMINE");
			progArrosageRepo.save(programme);
			
			JournalArrosage journal = new JournalArrosage();
			journal.setProgrammeId(programmeId);
			journal.setDateExecution(LocalDateTime.now());
			journal.setVolumeReel(volumeReel);
			journal.setRemarque(remarque);
			
			return journalRepo.save(journal);
		}
		
		return null;
	}
	
	// Récupérer tous les programmes
	public List<ProgrammeArrosage> getAllProgrammes() {
		return progArrosageRepo.findAll();
	}
	
	// Récupérer un programme par ID
	public ProgrammeArrosage getProgrammeById(Long id) {
		return progArrosageRepo.findById(id).orElse(null);
	}
	
	// Récupérer les programmes par parcelle
	public List<ProgrammeArrosage> getProgrammesByParcelle(Long parcelleId) {
		return progArrosageRepo.findByParcelleId(parcelleId);
	}
	
	// Récupérer les programmes par statut
	public List<ProgrammeArrosage> getProgrammesByStatut(String statut) {
		return progArrosageRepo.findByStatut(statut);
	}
	
	// Récupérer les programmes par période
	public List<ProgrammeArrosage> getProgrammesByPeriod(LocalDateTime start, LocalDateTime end) {
		return progArrosageRepo.findByDatePlanifieeBetween(start, end);
	}
	
	// Mettre à jour un programme
	public ProgrammeArrosage updateProgramme(Long id, ProgrammeArrosage programme) {
		ProgrammeArrosage existing = progArrosageRepo.findById(id).orElse(null);
		if (existing != null) {
			existing.setParcelleId(programme.getParcelleId());
			existing.setDatePlanifiee(programme.getDatePlanifiee());
			existing.setDuree(programme.getDuree());
			existing.setVolumePrevu(programme.getVolumePrevu());
			existing.setStatut(programme.getStatut());
			existing.setStationMeteoId(programme.getStationMeteoId());
			return progArrosageRepo.save(existing);
		}
		return null;
	}
	
	// Supprimer un programme
	public void deleteProgramme(Long id) {
		progArrosageRepo.deleteById(id);
	}
	
	/**
	 * Planifier l'arrosage en utilisant la météo en temps réel (Open-Meteo)
	 */
	public ProgrammeArrosage planifierArrosageAvecMeteoTempsReel(
			Long parcelleId, 
			Double latitude, 
			Double longitude) {
		
		log.info("Planification arrosage avec météo temps réel pour parcelle {} (lat={}, lon={})", 
			parcelleId, latitude, longitude);
		
		try {
			// Récupérer la météo actuelle via Open-Meteo
			WeatherDTO weather = weatherClient.getCurrentWeather(latitude, longitude);
			
			if (weather == null) {
				log.warn("Impossible de récupérer la météo temps réel, utilisation des valeurs par défaut");
				return creerProgrammeParDefaut(parcelleId);
			}
			
			// Créer le programme d'arrosage basé sur la météo actuelle
			ProgrammeArrosage programme = new ProgrammeArrosage();
			programme.setParcelleId(parcelleId);
			programme.setDatePlanifiee(LocalDateTime.now().plusHours(1)); // Dans 1 heure
			programme.setStatut("PLANIFIE");
			
			// Ajuster selon la météo actuelle
			ajusterProgrammeSelonMeteoActuelle(programme, weather);
			
			log.info("Programme créé: durée={}min, volume={}L, météo: {}°C, {}", 
				programme.getDuree(), programme.getVolumePrevu(), 
				weather.getTemperature(), weather.getWeatherDescription());
			
			return progArrosageRepo.save(programme);
			
		} catch (Exception e) {
			log.error("Erreur lors de la récupération de la météo: {}", e.getMessage());
			return creerProgrammeParDefaut(parcelleId);
		}
	}
	
	/**
	 * Ajuste le programme d'arrosage selon la météo actuelle
	 */
	private void ajusterProgrammeSelonMeteoActuelle(ProgrammeArrosage programme, WeatherDTO weather) {
		double temperature = weather.getTemperature() != null ? weather.getTemperature() : 25;
		double windSpeed = weather.getWindSpeed() != null ? weather.getWindSpeed() : 0;
		int weatherCode = weather.getWeatherCode() != null ? weather.getWeatherCode() : 0;
		
		// Pluie en cours (codes 51-67, 80-82)
		if ((weatherCode >= 51 && weatherCode <= 67) || 
		    (weatherCode >= 80 && weatherCode <= 82)) {
			log.info("Pluie détectée (code {}), arrosage annulé", weatherCode);
			programme.setDuree(0);
			programme.setVolumePrevu(0.0);
			programme.setStatut("ANNULE");
			return;
		}
		
		// Neige (codes 71-86)
		if (weatherCode >= 71 && weatherCode <= 86) {
			log.info("Neige détectée, arrosage annulé");
			programme.setDuree(0);
			programme.setVolumePrevu(0.0);
			programme.setStatut("ANNULE");
			return;
		}
		
		// Température très élevée (> 35°C)
		if (temperature > 35) {
			log.info("Température élevée ({}°C), augmentation de l'arrosage de 50%", temperature);
			programme.setDuree(45);
			programme.setVolumePrevu(750.0);
		}
		// Température élevée (30-35°C)
		else if (temperature > 30) {
			log.info("Température modérément élevée ({}°C), augmentation de 30%", temperature);
			programme.setDuree(40);
			programme.setVolumePrevu(650.0);
		}
		// Température basse (< 15°C)
		else if (temperature < 15) {
			log.info("Température basse ({}°C), réduction de l'arrosage", temperature);
			programme.setDuree(20);
			programme.setVolumePrevu(300.0);
		}
		// Conditions normales
		else {
			log.info("Conditions normales ({}°C)", temperature);
			programme.setDuree(30);
			programme.setVolumePrevu(500.0);
		}
		
		// Ajustement selon le vent (vent fort > 30 km/h)
		if (windSpeed > 30) {
			log.info("Vent fort ({}km/h), réduction de 20% pour éviter l'évaporation", windSpeed);
			programme.setDuree((int) (programme.getDuree() * 0.8));
			programme.setVolumePrevu(programme.getVolumePrevu() * 0.8);
		}
	}
	
	/**
	 * Crée un programme avec des valeurs par défaut
	 */
	private ProgrammeArrosage creerProgrammeParDefaut(Long parcelleId) {
		log.info("Création d'un programme par défaut pour parcelle {}", parcelleId);
		ProgrammeArrosage programme = new ProgrammeArrosage();
		programme.setParcelleId(parcelleId);
		programme.setDatePlanifiee(LocalDateTime.now().plusHours(1));
		programme.setDuree(30);
		programme.setVolumePrevu(500.0);
		programme.setStatut("PLANIFIE");
		return progArrosageRepo.save(programme);
	}
}