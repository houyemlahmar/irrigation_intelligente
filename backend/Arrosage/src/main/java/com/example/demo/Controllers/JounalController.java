package com.example.demo.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entities.JournalArrosage;
import com.example.demo.Repositories.JournalRepo;

@RestController
@RequestMapping("/api/journal")
public class JounalController {
	
	@Autowired
	private JournalRepo journalRepo;
	
	// Récupérer tous les journaux
	@GetMapping
	public ResponseEntity<List<JournalArrosage>> getAllJournaux() {
		List<JournalArrosage> journaux = journalRepo.findAll();
		return new ResponseEntity<>(journaux, HttpStatus.OK);
	}
	
	// Récupérer un journal par ID
	@GetMapping("/{id}")
	public ResponseEntity<JournalArrosage> getJournalById(@PathVariable Long id) {
		return journalRepo.findById(id)
				.map(journal -> new ResponseEntity<>(journal, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
	
	// Récupérer les journaux par programme
	@GetMapping("/programme/{programmeId}")
	public ResponseEntity<List<JournalArrosage>> getJournauxByProgramme(@PathVariable Long programmeId) {
		List<JournalArrosage> journaux = journalRepo.findByProgrammeId(programmeId);
		return new ResponseEntity<>(journaux, HttpStatus.OK);
	}
	
	// Récupérer les journaux par période
	@GetMapping("/period")
	public ResponseEntity<List<JournalArrosage>> getJournauxByPeriod(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		List<JournalArrosage> journaux = journalRepo.findByDateExecutionBetween(start, end);
		return new ResponseEntity<>(journaux, HttpStatus.OK);
	}
}