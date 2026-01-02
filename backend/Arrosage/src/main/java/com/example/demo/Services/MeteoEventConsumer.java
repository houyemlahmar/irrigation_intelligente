package com.example.demo.Services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Config.RabbitMQConfig;
import com.example.demo.DTOs.ChangementConditionsEvent;

@Service
public class MeteoEventConsumer {
	
	@Autowired
	private ProgArrosageService progArrosageService;
	
	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void handleChangementConditions(ChangementConditionsEvent event) {
		System.out.println("=== Événement météo reçu ===");
		System.out.println("Station: " + event.getNomStation());
		System.out.println("Type: " + event.getTypeChangement());
		System.out.println("Message: " + event.getMessage());
		System.out.println("Date: " + event.getDate());
		System.out.println("Pluie prévue: " + event.getPluiePrevue() + " mm");
		System.out.println("Température max: " + event.getTemperatureMax() + "°C");
		
		// Ajuster automatiquement les programmes d'arrosage en fonction des conditions
		progArrosageService.ajusterProgrammesSelonMeteo(event);
	}
}
