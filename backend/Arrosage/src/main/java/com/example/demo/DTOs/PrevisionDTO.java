package com.example.demo.DTOs;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrevisionDTO {
	private Long id;
	private LocalDate date;
	private Double temperatureMax;
	private Double temperatureMin;
	private Double pluiePrevue;
	private Double vent;
	private Long stationId;
	private String nomStation;
}
