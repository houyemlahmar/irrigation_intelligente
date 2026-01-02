package com.example.demo.Listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.Config.RabbitMQConfig;
import com.example.demo.DTOs.ChangementConditionsEvent;
import com.example.demo.Services.ProgArrosageService;

@Component
public class MeteoEventListener {
	
	@Autowired
	private ProgArrosageService progArrosageService;
	
	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void handleChangementConditions(ChangementConditionsEvent event) {
		System.out.println("📨 Événement météo reçu : " + event.getMessage());
		System.out.println("   Station: " + event.getNomStation());
		System.out.println("   Date: " + event.getDate());
		System.out.println("   Température Max: " + event.getTemperatureMax() + "°C");
		System.out.println("   Pluie prévue: " + event.getPluiePrevue() + " mm");
		
		// Ajuster les programmes d'arrosage en conséquence
		progArrosageService.ajusterProgrammesSelonMeteo(event);
		
		System.out.println("✅ Programmes d'arrosage ajustés avec succès");
	}
}
