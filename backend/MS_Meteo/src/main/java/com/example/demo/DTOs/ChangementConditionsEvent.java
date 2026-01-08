package com.example.demo.DTOs;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangementConditionsEvent {
	
	private Long stationId;
	private String nomStation;
	private LocalDate date;
	private Double temperatureMax;
	private Double temperatureMin;
	private Double pluiePrevue;
	private Double vent;
	private String typeChangement; // PLUIE_FORTE, TEMPERATURE_EXTREME, etc.
	private String message;
}
