package com.example.demo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les données météorologiques Open-Meteo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    
    private Double temperature;
    private Double windSpeed;
    private Integer weatherCode;
    private String weatherDescription;
    private String time;
    private Double latitude;
    private Double longitude;
}
