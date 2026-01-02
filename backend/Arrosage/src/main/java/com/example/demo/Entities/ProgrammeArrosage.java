package com.example.demo.Entities;

import java.time.LocalDateTime;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgrammeArrosage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = "L'ID de la parcelle est obligatoire")
	private Long parcelleId;
	
	@NotNull(message = "La date planifiée est obligatoire")
	private LocalDateTime datePlanifiee;
	
	@Min(value = 0, message = "La durée doit être positive")
	private Integer duree; // Durée en minutes
	
	@Min(value = 0, message = "Le volume doit être positif")
	private Double volumePrevu; // Volume en litres
	
	@Pattern(regexp = "PLANIFIE|EN_COURS|TERMINE|ANNULE", message = "Statut invalide")
	private String statut; // PLANIFIE, EN_COURS, TERMINE, ANNULE
	
	private Long stationMeteoId;
	
	@Transient
    private StationMeteo StationMeteo;
}