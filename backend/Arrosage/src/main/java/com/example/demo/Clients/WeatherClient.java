package com.example.demo.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.DTOs.WeatherDTO;

/**
 * Client Feign pour accéder à l'API Open-Meteo via le service Meteo
 */
@FeignClient(name = "weather-service", url = "http://localhost:8084", path = "/api/weather")
public interface WeatherClient {
    
    /**
     * Récupère la météo actuelle pour des coordonnées données
     */
    @GetMapping("/current")
    WeatherDTO getCurrentWeather(
        @RequestParam("latitude") Double latitude,
        @RequestParam("longitude") Double longitude
    );
}
