package com.example.demo.Services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTOs.ChangementConditionsEvent;
import com.example.demo.Entities.PrevisionMeteo;
import com.example.demo.Repositories.PrevisionMeteoRepository;

@Service
public class PrevisionMeteoService {
	
	@Autowired
	private PrevisionMeteoRepository previsionRepo;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private static final String EXCHANGE_NAME = "meteo.exchange";
	private static final String ROUTING_KEY = "meteo.conditions.changement";
	
	public List<PrevisionMeteo> getAllPrevisions() {
		return previsionRepo.findAll();
	}
	
	public PrevisionMeteo getPrevisionById(Long id) {
		return previsionRepo.findById(id).orElse(null);
	}
	
	public List<PrevisionMeteo> getPrevisionsByStation(Long stationId) {
		return previsionRepo.findByStationId(stationId);
	}
	
	public List<PrevisionMeteo> getPrevisionsByStationAndDate(Long stationId, LocalDate date) {
		return previsionRepo.findByStationIdAndDate(stationId, date);
	}
	
	public List<PrevisionMeteo> getPrevisionsByStationAndPeriod(Long stationId, LocalDate startDate, LocalDate endDate) {
		return previsionRepo.findByStationIdAndDateBetween(stationId, startDate, endDate);
	}
	
	public PrevisionMeteo createPrevision(PrevisionMeteo prevision) {
		PrevisionMeteo saved = previsionRepo.save(prevision);
		
		// Si conditions critiques, envoyer un événement asynchrone via RabbitMQ
		if (isConditionCritique(prevision)) {
			publierChangementConditions(prevision);
		}
		
		return saved;
	}
	
	public PrevisionMeteo updatePrevision(Long id, PrevisionMeteo prevision) {
		if (previsionRepo.existsById(id)) {
			prevision.setId(id);
			PrevisionMeteo updated = previsionRepo.save(prevision);
			
			// Notifier le changement de conditions
			if (isConditionCritique(prevision)) {
				publierChangementConditions(prevision);
			}
			
			return updated;
		}
		return null;
	}
	
	public void deletePrevision(Long id) {
		previsionRepo.deleteById(id);
	}
	
	private boolean isConditionCritique(PrevisionMeteo prevision) {
		// Conditions critiques : forte pluie ou température extrême
		return (prevision.getPluiePrevue() != null && prevision.getPluiePrevue() > 10) ||
			   (prevision.getTemperatureMax() != null && prevision.getTemperatureMax() > 35);
	}
	
	private void publierChangementConditions(PrevisionMeteo prevision) {
		ChangementConditionsEvent event = new ChangementConditionsEvent();
		event.setStationId(prevision.getStation() != null ? prevision.getStation().getId() : null);
		event.setNomStation(prevision.getStation() != null ? prevision.getStation().getNom() : "Inconnue");
		event.setDate(prevision.getDate());
		event.setPluiePrevue(prevision.getPluiePrevue());
		event.setTemperatureMax(prevision.getTemperatureMax());
		event.setTemperatureMin(prevision.getTemperatureMin());
		event.setVent(prevision.getVent());
		
		if (prevision.getPluiePrevue() != null && prevision.getPluiePrevue() > 10) {
			event.setTypeChangement("PLUIE_FORTE");
			event.setMessage("Forte pluie prévue: " + prevision.getPluiePrevue() + " mm");
		} else {
			event.setTypeChangement("TEMPERATURE_EXTREME");
			event.setMessage("Température élevée prévue: " + prevision.getTemperatureMax() + "°C");
		}
		
		// Publication asynchrone via RabbitMQ
		rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
		System.out.println("=== Événement météo publié vers RabbitMQ ===");
		System.out.println("   Station: " + event.getNomStation());
		System.out.println("   Type: " + event.getTypeChangement());
		System.out.println("   Message: " + event.getMessage());
	}
}
