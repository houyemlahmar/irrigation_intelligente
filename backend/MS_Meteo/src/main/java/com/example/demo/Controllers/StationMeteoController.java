package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTOs.WeatherDTO;
import com.example.demo.Entities.StationMeteo;
import com.example.demo.Services.OpenMeteoService;
import com.example.demo.Services.StationMeteoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stations")
public class StationMeteoController {
	
	@Autowired
	private StationMeteoService stationService;
	
	@Autowired
	private OpenMeteoService openMeteoService;
	
	@GetMapping("/all")
	public ResponseEntity<List<StationMeteo>> getAllStations() {
		return ResponseEntity.ok(stationService.getAllStations());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<StationMeteo> getStationById(@PathVariable Long id) {
		StationMeteo station = stationService.getStationById(id);
		if (station != null) {
			return ResponseEntity.ok(station);
		}
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/active")
	public ResponseEntity<List<StationMeteo>> getActiveStations() {
		return ResponseEntity.ok(stationService.getActiveStations());
	}
	
	@PostMapping("/create")
	public ResponseEntity<StationMeteo> createStation(@Valid @RequestBody StationMeteo station) {
		StationMeteo created = stationService.createStation(station);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<StationMeteo> updateStation(
			@PathVariable Long id,
			@Valid @RequestBody StationMeteo station) {
		StationMeteo updated = stationService.updateStation(id, station);
		if (updated != null) {
			return ResponseEntity.ok(updated);
		}
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
		stationService.deleteStation(id);
		return ResponseEntity.noContent().build();
	}
	
	/**
	 * Récupère la météo en temps réel pour une station donnée
	 * GET /api/stations/{id}/weather
	 */
	@GetMapping("/{id}/weather")
	public ResponseEntity<?> getStationWeather(@PathVariable Long id) {
		StationMeteo station = stationService.getStationById(id);
		
		if (station == null) {
			return ResponseEntity.notFound().build();
		}
		
		if (station.getLatitude() == null || station.getLongitude() == null) {
			return ResponseEntity.badRequest()
				.body("La station ne possède pas de coordonnées GPS");
		}
		
		WeatherDTO weather = openMeteoService.getWeatherForStation(
			id, 
			station.getLatitude(), 
			station.getLongitude()
		);
		
		if (weather != null) {
			return ResponseEntity.ok(weather);
		} else {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Impossible de récupérer la météo pour cette station");
		}
	}
}