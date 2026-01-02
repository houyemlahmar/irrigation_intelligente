package com.example.demo.Config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration pour RestTemplate avec gestion des timeouts
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Crée un RestTemplate configuré avec des timeouts
     * - Connection timeout: 5 secondes
     * - Read timeout: 5 secondes
     * 
     * @return RestTemplate configuré
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        
        return new RestTemplate(factory);
    }
}