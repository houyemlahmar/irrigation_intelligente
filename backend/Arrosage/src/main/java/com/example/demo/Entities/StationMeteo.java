package com.example.demo.Entities;

import lombok.Data;

@Data
public class StationMeteo {

	private Long id;
	private String nom;
	private String localisation;
	private Double latitude;
	private Double longitude;
	private Boolean active;
	
}