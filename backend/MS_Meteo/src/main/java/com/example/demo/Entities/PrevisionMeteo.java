package com.example.demo.Entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class PrevisionMeteo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = "La date est obligatoire")
	private LocalDate date;
	
	@Min(value = -50, message = "Température max invalide")
	@Max(value = 60, message = "Température max invalide")
	private Double temperatureMax;
	
	@Min(value = -50, message = "Température min invalide")
	@Max(value = 60, message = "Température min invalide")
	private Double temperatureMin;
	
	@Min(value = 0, message = "La pluie prévue doit être positive")
	private Double pluiePrevue; // en mm
	
	@Min(value = 0, message = "Le vent doit être positif")
	private Double vent; // en km/h
	
	@Min(value = 0, message = "L'humidité doit être entre 0 et 100")
	@Max(value = 100, message = "L'humidité doit être entre 0 et 100")
	private Double humidite; // en %
	
	@ManyToOne
	@JoinColumn(name = "station_id")
	private StationMeteo station;
}