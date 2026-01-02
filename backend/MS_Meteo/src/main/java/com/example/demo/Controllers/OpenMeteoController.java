package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTOs.WeatherDTO;
import com.example.demo.Services.OpenMeteoService;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour accéder aux données météo Open-Meteo
 * Endpoint: /api/weather
 */
@RestController
@RequestMapping("/api/weather")
@Slf4j  // méthode de lombok pour gérer les log(info, warn, error)
public class OpenMeteoController {
    
    @Autowired
    private OpenMeteoService openMeteoService;
    
    /**
     * Récupère la météo actuelle pour une localisation
     * 
     * GET /api/weather/current?latitude=36.8065&longitude=10.1815
     * 
     * @param latitude Latitude (-90 à 90)
     * @param longitude Longitude (-180 à 180)
     * @return WeatherDTO avec les données météo actuelles
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        log.info("Request: GET /api/weather/current?latitude={}&longitude={}", latitude, longitude);
        
        try {
            WeatherDTO weather = openMeteoService.getCurrentWeather(latitude, longitude);
            
            if (weather != null) {
                return ResponseEntity.ok(weather);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Impossible de récupérer les données météo. Réessayez plus tard.");
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Paramètres invalides: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("Erreur: " + e.getMessage());
                
        } catch (Exception e) {
            log.error("Erreur serveur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur interne du serveur");
        }
    }
    
    /**
     * Point de test pour vérifier le service
     * 
     * GET /api/weather/test
     * 
     * @return Données météo de Tunis (exemple)
     */
    @GetMapping("/test")
    public ResponseEntity<?> testWeather() {
        log.info("Test endpoint appelé");
        
        // Coordonnées de Tunis, Tunisie
        Double latitude = 36.8065;
        Double longitude = 10.1815;
        
        WeatherDTO weather = openMeteoService.getCurrentWeather(latitude, longitude);
        
        if (weather != null) {
            return ResponseEntity.ok(weather);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Service météo indisponible");
        }
    }
}
