package com.example.demo.DTOs;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ChangementConditionsEvent {
	
	private Long stationId;
	private String nomStation;
	private String typeChangement; // PLUIE_FORTE, TEMPERATURE_EXTREME, etc.
	private String message;
	private LocalDate date;
	private Double pluiePrevue;
	private Double temperatureMax;
}
