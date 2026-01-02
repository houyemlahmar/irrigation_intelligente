package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO pour parser la réponse JSON de l'API Open-Meteo
 * API: https://api.open-meteo.com/v1/forecast
 */
@Data
public class OpenMeteoResponse {
    
    private Double latitude;
    private Double longitude;
    
    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;
    
    /**
     * Classe interne pour les données météo actuelles
     */
    @Data
    public static class CurrentWeather {
        
        /**
         * Température en degrés Celsius
         */
        private Double temperature;
        
        /**
         * Vitesse du vent en km/h
         */
        @JsonProperty("windspeed")
        private Double windSpeed;
        
        /**
         * Code météo WMO (World Meteorological Organization)
         * 0: Clear sky
         * 1-3: Mainly clear, partly cloudy, overcast
         * 45-48: Fog
         * 51-67: Rain (various intensities)
         * 71-86: Snow
         * 95-99: Thunderstorm
         */
        @JsonProperty("weathercode")
        private Integer weatherCode;
        
        /**
         * Date et heure au format ISO8601 (ex: 2025-01-15T14:30)
         */
        private String time;
    }
}
