package com.example.demo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Clients.OpenMeteoClient;
import com.example.demo.DTOs.WeatherDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour gérer les appels à l'API Open-Meteo
 * Fournit une couche d'abstraction entre les contrôleurs et le client API
 */
@Service
@Slf4j
public class OpenMeteoService {
    
    @Autowired
    private OpenMeteoClient openMeteoClient;
    
    /**
     * Récupère les données météo actuelles pour une localisation
     * 
     * @param latitude Latitude de la localisation
     * @param longitude Longitude de la localisation
     * @return WeatherDTO avec les données météo
     */
    public WeatherDTO getCurrentWeather(Double latitude, Double longitude) {
        log.info("Demande de météo pour lat={}, lon={}", latitude, longitude);
        
        try {
            WeatherDTO weather = openMeteoClient.getWeather(latitude, longitude);
            
            if (weather != null) {
                log.info("Météo récupérée: {}°C, vent: {} km/h, {}", 
                    weather.getTemperature(), 
                    weather.getWindSpeed(), 
                    weather.getWeatherDescription());
            } else {
                log.warn("Impossible de récupérer les données météo");
            }
            
            return weather;
            
        } catch (IllegalArgumentException e) {
            log.error("Coordonnées invalides: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la météo: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Récupère la météo pour une station météo (utilise ses coordonnées)
     * 
     * @param stationId ID de la station météo
     * @param latitude Latitude de la station
     * @param longitude Longitude de la station
     * @return WeatherDTO avec les données météo
     */
    public WeatherDTO getWeatherForStation(Long stationId, Double latitude, Double longitude) {
        log.info("Récupération météo pour station ID={}", stationId);
        return getCurrentWeather(latitude, longitude);
    }
}
