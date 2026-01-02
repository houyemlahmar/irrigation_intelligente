package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entities.PrevisionMeteo;
import com.example.demo.Services.PrevisionMeteoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/previsions")
public class PrevisionMeteoController {
	
	@Autowired
	private PrevisionMeteoService previsionService;
	
	@GetMapping("/all")
	public ResponseEntity<List<PrevisionMeteo>> getAllPrevisions() {
		return ResponseEntity.ok(previsionService.getAllPrevisions());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<PrevisionMeteo> getPrevisionById(@PathVariable Long id) {
		PrevisionMeteo prevision = previsionService.getPrevisionById(id);
		if (prevision != null) {
			return ResponseEntity.ok(prevision);
		}
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/station/{stationId}")
	public ResponseEntity<List<PrevisionMeteo>> getPrevisionsByStation(@PathVariable Long stationId) {
		return ResponseEntity.ok(previsionService.getPrevisionsByStation(stationId));
	}
	
	@GetMapping("/station/{stationId}/date/{date}")
	public ResponseEntity<List<PrevisionMeteo>> getPrevisionsByStationAndDate(
			@PathVariable Long stationId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResponseEntity.ok(previsionService.getPrevisionsByStationAndDate(stationId, date));
	}
	
	@GetMapping("/station/{stationId}/period")
	public ResponseEntity<List<PrevisionMeteo>> getPrevisionsByStationAndPeriod(
			@PathVariable Long stationId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return ResponseEntity.ok(previsionService.getPrevisionsByStationAndPeriod(stationId, startDate, endDate));
	}
	
	@PostMapping("/create")
	public ResponseEntity<PrevisionMeteo> createPrevision(@Valid @RequestBody PrevisionMeteo prevision) {
		PrevisionMeteo created = previsionService.createPrevision(prevision);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<PrevisionMeteo> updatePrevision(
			@PathVariable Long id,
			@Valid @RequestBody PrevisionMeteo prevision) {
		PrevisionMeteo updated = previsionService.updatePrevision(id, prevision);
		if (updated != null) {
			return ResponseEntity.ok(updated);
		}
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deletePrevision(@PathVariable Long id) {
		previsionService.deletePrevision(id);
		return ResponseEntity.noContent().build();
	}
}