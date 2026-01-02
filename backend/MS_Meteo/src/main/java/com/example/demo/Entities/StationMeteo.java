package com.example.demo.Entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class StationMeteo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "Le nom de la station est obligatoire")
	private String nom;
	
	@NotBlank(message = "La localisation est obligatoire")
	private String localisation;
	
	private Double latitude;
	
	private Double longitude;
	
	@NotNull(message = "Le statut actif est obligatoire")
	private Boolean active;
	
	@OneToMany(mappedBy = "station")
	@JsonIgnore
	private List<PrevisionMeteo> previsions;
}