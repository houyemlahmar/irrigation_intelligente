package com.example.demo.Services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
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
		log.info("Planification automatique pour parcelle {} avec station {} pour le {}", 
			parcelleId, stationMeteoId, date);
		
		// Appel synchrone vers le microservice Météo via OpenFeign
		List<PrevisionDTO> previsions = meteoClient.getPrevisionsByStationAndDate(stationMeteoId, date);
		
		ProgrammeArrosage programme = new ProgrammeArrosage();
		programme.setParcelleId(parcelleId);
		programme.setStationMeteoId(stationMeteoId);
		programme.setDatePlanifiee(date.atTime(6, 0)); // Par défaut à 6h du matin
		programme.setStatut("PLANIFIE");
		
		if (!previsions.isEmpty()) {
			PrevisionDTO prevision = previsions.get(0);
			log.info("Prévision récupérée: temp={}°C, pluie={}mm, vent={}km/h, humidité={}%", 
				prevision.getTemperatureMax(), prevision.getPluiePrevue(), 
				prevision.getVent(), prevision.getHumidite());
			ajusterProgrammeSelonPrevision(programme, prevision);
		} else {
			// Valeurs par défaut si pas de prévision
			log.warn("Aucune prévision disponible pour station {}, utilisation valeurs par défaut", stationMeteoId);
			programme.setDuree(30); // 30 minutes
			programme.setVolumePrevu(500.0); // 500 litres
		}
		
		ProgrammeArrosage saved = progArrosageRepo.save(programme);
		log.info("Programme créé avec succès: ID={}, durée={}min, volume={}L, statut={}", 
			saved.getId(), saved.getDuree(), saved.getVolumePrevu(), saved.getStatut());
		
		return saved;
	}
	
	// Ajuster le programme selon la prévision météo
	private void ajusterProgrammeSelonPrevision(ProgrammeArrosage programme, PrevisionDTO prevision) {
		double pluiePrevue = prevision.getPluiePrevue() != null ? prevision.getPluiePrevue() : 0;
		double temperatureMax = prevision.getTemperatureMax() != null ? prevision.getTemperatureMax() : 25;
		double vent = prevision.getVent() != null ? prevision.getVent() : 0;
		
		log.info("Ajustement programme avec prévisions: pluie={}mm, temp={}°C, vent={}km/h", 
			pluiePrevue, temperatureMax, vent);
		
		// Logique d'ajustement basée sur les conditions météo
		if (pluiePrevue > 10) {
			// Forte pluie : annuler l'arrosage
			log.info("Forte pluie prévue ({}mm), arrosage annulé", pluiePrevue);
			programme.setDuree(0);
			programme.setVolumePrevu(0.0);
			programme.setStatut("ANNULE");
		} else if (pluiePrevue > 5) {
			// Pluie modérée : réduire de 70%
			log.info("Pluie modérée prévue ({}mm), réduction de 70%", pluiePrevue);
			programme.setDuree(10);
			programme.setVolumePrevu(150.0);
		} else if (pluiePrevue > 0) {
			// Pluie légère : réduire de 50%
			log.info("Pluie légère prévue ({}mm), réduction de 50%", pluiePrevue);
			programme.setDuree(15);
			programme.setVolumePrevu(250.0);
		} else if (temperatureMax > 35) {
			// Température très élevée : augmenter de 50%
			log.info("Température très élevée ({}°C), augmentation de 50%", temperatureMax);
			programme.setDuree(45);
			programme.setVolumePrevu(750.0);
		} else if (temperatureMax > 30) {
			// Température élevée : augmenter de 30%
			log.info("Température élevée ({}°C), augmentation de 30%", temperatureMax);
			programme.setDuree(40);
			programme.setVolumePrevu(650.0);
		} else if (temperatureMax < 15) {
			// Température basse : réduire
			log.info("Température basse ({}°C), réduction de l'arrosage", temperatureMax);
			programme.setDuree(20);
			programme.setVolumePrevu(300.0);
		} else {
			// Conditions normales
			log.info("Conditions normales ({}°C)", temperatureMax);
			programme.setDuree(30);
			programme.setVolumePrevu(500.0);
		}
		
		// Ajustement selon le vent (vent fort > 30 km/h)
		if (vent > 30) {
			log.info("Vent fort ({}km/h), réduction de 20% pour éviter l'évaporation", vent);
			int nouvelleDuree = (int) (programme.getDuree() * 0.8);
			double nouveauVolume = programme.getVolumePrevu() * 0.8;
			programme.setDuree(nouvelleDuree);
			programme.setVolumePrevu(nouveauVolume);
		}
		
		log.info("Programme ajusté: durée={}min, volume={}L", programme.getDuree(), programme.getVolumePrevu());
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
	
	// Démarrer l'exécution d'un programme d'arrosage
	public ProgrammeArrosage demarrerProgramme(Long programmeId) {
		ProgrammeArrosage programme = progArrosageRepo.findById(programmeId).orElse(null);
		
		if (programme != null && "PLANIFIE".equals(programme.getStatut())) {
			programme.setStatut("EN_COURS");
			programme.setDatePlanifiee(LocalDateTime.now()); // Marquer l'heure de début
			return progArrosageRepo.save(programme);
		}
		
		return programme;
	}
	
	// Terminer un programme d'arrosage (appelé automatiquement après la durée ou manuellement)
	public JournalArrosage terminerProgramme(Long programmeId, Double volumeReel, String remarque) {
		ProgrammeArrosage programme = progArrosageRepo.findById(programmeId).orElse(null);
		
		if (programme != null) {
			programme.setStatut("TERMINE");
			progArrosageRepo.save(programme);
			
			JournalArrosage journal = new JournalArrosage();
			journal.setProgrammeId(programmeId);
			journal.setDateExecution(LocalDateTime.now());
			journal.setVolumeReel(volumeReel != null ? volumeReel : programme.getVolumePrevu());
			journal.setRemarque(remarque);
			
			return journalRepo.save(journal);
		}
		
		return null;
	}
	
	// Vérifier et terminer automatiquement les programmes EN_COURS dont la durée est dépassée
	public List<ProgrammeArrosage> verifierEtTerminerProgrammesExpires() {
		List<ProgrammeArrosage> programmes = progArrosageRepo.findByStatut("EN_COURS");
		List<ProgrammeArrosage> programmesTermines = new java.util.ArrayList<>();
		
		for (ProgrammeArrosage programme : programmes) {
			LocalDateTime dateDebut = programme.getDatePlanifiee();
			LocalDateTime dateFin = dateDebut.plusMinutes(programme.getDuree());
			
			if (LocalDateTime.now().isAfter(dateFin)) {
				log.info("Programme {} expiré, termination automatique", programme.getId());
				terminerProgramme(programme.getId(), programme.getVolumePrevu(), "Terminé automatiquement");
				programme.setStatut("TERMINE");
				programmesTermines.add(programme);
			}
		}
		
		return programmesTermines;
	}
	
	// Méthode de compatibilité (conservée pour ne pas casser l'API)
	public JournalArrosage executerProgramme(Long programmeId, Double volumeReel, String remarque) {
		demarrerProgramme(programmeId);
		return terminerProgramme(programmeId, volumeReel, remarque);
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
			
			// Vérifier les conditions critiques et publier événement RabbitMQ
			verifierEtPublierConditionsCritiquesTempsReel(weather);
			
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
	 * Vérifie les conditions critiques pour la météo temps réel et publie un événement RabbitMQ
	 */
	private void verifierEtPublierConditionsCritiquesTempsReel(WeatherDTO weather) {
		double temperature = weather.getTemperature() != null ? weather.getTemperature() : 25;
		int weatherCode = weather.getWeatherCode() != null ? weather.getWeatherCode() : 0;
		
		boolean isCritical = false;
		String typeChangement = "";
		String message = "";
		
		// Forte pluie détectée (codes 51-67, 80-82)
		if ((weatherCode >= 51 && weatherCode <= 67) || 
		    (weatherCode >= 80 && weatherCode <= 82)) {
			isCritical = true;
			typeChangement = "PLUIE_FORTE";
			message = String.format("Forte pluie détectée en temps réel (code: %d)", weatherCode);
		}
		// Température extrême (> 35°C)
		else if (temperature > 35) {
			isCritical = true;
			typeChangement = "TEMPERATURE_EXTREME";
			message = String.format("Température extrême détectée en temps réel: %.1f°C", temperature);
		}
		
		if (isCritical) {
			log.info("=== Conditions critiques détectées en temps réel ===");
			log.info("Type: {}", typeChangement);
			log.info("Message: {}", message);
			
			// Créer et publier l'événement
			ChangementConditionsEvent event = new ChangementConditionsEvent();
			event.setDate(LocalDate.now());
			event.setTemperatureMax(temperature);
			event.setTemperatureMin(temperature);
			event.setTypeChangement(typeChangement);
			event.setMessage(message);
			event.setPluiePrevue(weatherCode >= 51 ? 15.0 : 0.0); // Estimation
			event.setVent(weather.getWindSpeed() != null ? weather.getWindSpeed() : 0.0);
			
			try {
				rabbitTemplate.convertAndSend("meteo.exchange", "meteo.conditions.changement", event);
				log.info("=== Événement météo temps réel publié vers RabbitMQ ===");
			} catch (Exception e) {
				log.error("Erreur lors de la publication de l'événement RabbitMQ: {}", e.getMessage());
			}
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
	
	// Méthode de test pour vérifier la communication Feign
	public List<PrevisionDTO> testGetPrevisions(Long stationId, LocalDate date) {
		log.info("Test récupération prévisions: stationId={}, date={}", stationId, date);
		List<PrevisionDTO> previsions = meteoClient.getPrevisionsByStationAndDate(stationId, date);
		log.info("Nombre de prévisions récupérées: {}", previsions.size());
		return previsions;
	}
}