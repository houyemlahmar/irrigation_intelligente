package com.example.demo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplifié pour les données météorologiques
 * Utilisé pour retourner les informations météo aux clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    
    /**
     * Température en degrés Celsius
     */
    private Double temperature;
    
    /**
     * Vitesse du vent en km/h
     */
    private Double windSpeed;
    
    /**
     * Code météo WMO
     */
    private Integer weatherCode;
    
    /**
     * Description du code météo
     */
    private String weatherDescription;
    
    /**
     * Date et heure de l'observation
     */
    private String time;
    
    /**
     * Latitude de la localisation
     */
    private Double latitude;
    
    /**
     * Longitude de la localisation
     */
    private Double longitude;
}
