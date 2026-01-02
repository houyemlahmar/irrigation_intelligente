package com.example.demo.Clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.demo.DTOs.OpenMeteoResponse;
import com.example.demo.DTOs.WeatherDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Client REST pour l'API Open-Meteo
 * Permet de récupérer les données météorologiques en temps réel
 * 
 * @author Your Name
 * @version 1.0
 */
@Component
@Slf4j
public class OpenMeteoClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${openmeteo.api.base-url}")
    private String baseUrl;
    
    @Value("${openmeteo.api.timeout:5000}")
    private int timeout;
    
    public OpenMeteoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Récupère les données météo actuelles pour une localisation donnée
     * 
     * @param latitude Latitude de la localisation (-90 à 90)
     * @param longitude Longitude de la localisation (-180 à 180)
     * @return WeatherDTO contenant les données météo ou null en cas d'erreur
     * @throws IllegalArgumentException si les coordonnées sont invalides
     */
    public WeatherDTO getWeather(Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);
        
        String url = buildUrl(latitude, longitude);
        log.info("Appel API Open-Meteo: {}", url);
        
        try {
            ResponseEntity<OpenMeteoResponse> response = restTemplate.getForEntity(
                url, 
                OpenMeteoResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Données météo récupérées avec succès pour lat={}, lon={}", latitude, longitude);
                return mapToWeatherDTO(response.getBody());
            } else {
                log.warn("Réponse vide ou statut inattendu: {}", response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            log.error("Erreur client HTTP (4xx): {} - {}", e.getStatusCode(), e.getMessage());
            handleClientError(e);
            return null;
            
        } catch (HttpServerErrorException e) {
            log.error("Erreur serveur HTTP (5xx): {} - {}", e.getStatusCode(), e.getMessage());
            handleServerError(e);
            return null;
            
        } catch (ResourceAccessException e) {
            log.error("Timeout ou problème réseau: {}", e.getMessage());
            handleNetworkError(e);
            return null;
            
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'appel API: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Construit l'URL complète avec les paramètres
     */
    private String buildUrl(Double latitude, Double longitude) {
        return String.format("%s?latitude=%.4f&longitude=%.4f&current_weather=true", 
            baseUrl, latitude, longitude);
    }
    
    /**
     * Valide les coordonnées géographiques
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("La latitude et la longitude sont obligatoires");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("La latitude doit être entre -90 et 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("La longitude doit être entre -180 et 180");
        }
    }
    
    /**
     * Convertit la réponse Open-Meteo en DTO simplifié
     */
    private WeatherDTO mapToWeatherDTO(OpenMeteoResponse response) {
        WeatherDTO dto = new WeatherDTO();
        dto.setLatitude(response.getLatitude());
        dto.setLongitude(response.getLongitude());
        
        if (response.getCurrentWeather() != null) {
            OpenMeteoResponse.CurrentWeather current = response.getCurrentWeather();
            dto.setTemperature(current.getTemperature());
            dto.setWindSpeed(current.getWindSpeed());
            dto.setWeatherCode(current.getWeatherCode());
            dto.setWeatherDescription(getWeatherDescription(current.getWeatherCode()));
            dto.setTime(current.getTime());
        }
        
        return dto;
    }
    
    /**
     * Convertit le code météo WMO en description lisible
     */
    private String getWeatherDescription(Integer code) {
        if (code == null) return "Inconnu";
        
        return switch (code) {
            case 0 -> "Ciel dégagé";
            case 1 -> "Principalement dégagé";
            case 2 -> "Partiellement nuageux";
            case 3 -> "Couvert";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine";
            case 61, 63, 65 -> "Pluie";
            case 71, 73, 75 -> "Neige";
            case 77 -> "Grains de neige";
            case 80, 81, 82 -> "Averses de pluie";
            case 85, 86 -> "Averses de neige";
            case 95 -> "Orage";
            case 96, 99 -> "Orage avec grêle";
            default -> "Code météo: " + code;
        };
    }
    
    /**
     * Gère les erreurs client (4xx)
     */
    private void handleClientError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            log.error("Paramètres invalides envoyés à l'API");
        } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.error("Endpoint API introuvable");
        }
    }
    
    /**
     * Gère les erreurs serveur (5xx)
     */
    private void handleServerError(HttpServerErrorException e) {
        log.error("Le serveur Open-Meteo rencontre des problèmes. Réessayez plus tard.");
    }
    
    /**
     * Gère les erreurs réseau (timeout, connexion)
     */
    private void handleNetworkError(ResourceAccessException e) {
        log.error("Impossible de contacter l'API Open-Meteo. Vérifiez votre connexion réseau.");
    }
}
