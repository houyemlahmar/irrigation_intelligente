package com.example.demo.Controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entities.JournalArrosage;
import com.example.demo.Entities.ProgrammeArrosage;
import com.example.demo.Services.ProgArrosageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/programmes")
public class ProgArrosageController {
	
	@Autowired
	private ProgArrosageService progArrosageService;
	
	// Créer un programme avec planification automatique
	@PostMapping("/auto")
	public ResponseEntity<ProgrammeArrosage> createProgrammeAvecPlanificationAuto(
			@Valid @RequestBody ProgrammeArrosage programme) {
		ProgrammeArrosage created = progArrosageService.createProgrammeAvecPlanificationAuto(programme);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}
	
	// Planifier automatiquement selon les prévisions météo
	@PostMapping("/planifier-auto")
	public ResponseEntity<ProgrammeArrosage> planifierArrosageAutomatique(
			@RequestParam Long parcelleId,
			@RequestParam Long stationMeteoId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		ProgrammeArrosage programme = progArrosageService.planifierArrosageAutomatique(
				parcelleId, stationMeteoId, date);
		return new ResponseEntity<>(programme, HttpStatus.CREATED);
	}
	
	/**
	 * Planifier l'arrosage avec la météo en temps réel Open-Meteo
	 * POST /api/programmes/planifier-temps-reel?parcelleId=1&latitude=36.8065&longitude=10.1815
	 */
	@PostMapping("/planifier-temps-reel")
	public ResponseEntity<ProgrammeArrosage> planifierArrosageTempsReel(
			@RequestParam Long parcelleId,
			@RequestParam Double latitude,
			@RequestParam Double longitude) {
		ProgrammeArrosage programme = progArrosageService.planifierArrosageAvecMeteoTempsReel(
				parcelleId, latitude, longitude);
		return new ResponseEntity<>(programme, HttpStatus.CREATED);
	}
	
	// Démarrer l'exécution d'un programme (passe à EN_COURS)
	@PostMapping("/{id}/demarrer")
	public ResponseEntity<ProgrammeArrosage> demarrerProgramme(@PathVariable Long id) {
		ProgrammeArrosage programme = progArrosageService.demarrerProgramme(id);
		if (programme != null) {
			return new ResponseEntity<>(programme, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// Terminer un programme d'arrosage (passe à TERMINE)
	@PostMapping("/{id}/terminer")
	public ResponseEntity<JournalArrosage> terminerProgramme(
			@PathVariable Long id,
			@RequestParam(required = false) Double volumeReel,
			@RequestParam(required = false) String remarque) {
		JournalArrosage journal = progArrosageService.terminerProgramme(id, volumeReel, remarque);
		if (journal != null) {
			return new ResponseEntity<>(journal, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// Vérifier et terminer automatiquement les programmes expirés
	@PostMapping("/verifier-expires")
	public ResponseEntity<List<ProgrammeArrosage>> verifierProgrammesExpires() {
		List<ProgrammeArrosage> programmesTermines = progArrosageService.verifierEtTerminerProgrammesExpires();
		return new ResponseEntity<>(programmesTermines, HttpStatus.OK);
	}
	
	// Exécuter un programme d'arrosage (ancien comportement - exécution immédiate)
	@PostMapping("/{id}/executer")
	public ResponseEntity<JournalArrosage> executerProgramme(
			@PathVariable Long id,
			@RequestParam Double volumeReel,
			@RequestParam(required = false) String remarque) {
		JournalArrosage journal = progArrosageService.executerProgramme(id, volumeReel, remarque);
		if (journal != null) {
			return new ResponseEntity<>(journal, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// Récupérer tous les programmes
	@GetMapping
	public ResponseEntity<List<ProgrammeArrosage>> getAllProgrammes() {
		List<ProgrammeArrosage> programmes = progArrosageService.getAllProgrammes();
		return new ResponseEntity<>(programmes, HttpStatus.OK);
	}
	
	// Récupérer un programme par ID
	@GetMapping("/{id}")
	public ResponseEntity<ProgrammeArrosage> getProgrammeById(@PathVariable Long id) {
		ProgrammeArrosage programme = progArrosageService.getProgrammeById(id);
		if (programme != null) {
			return new ResponseEntity<>(programme, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// Récupérer les programmes par parcelle
	@GetMapping("/parcelle/{parcelleId}")
	public ResponseEntity<List<ProgrammeArrosage>> getProgrammesByParcelle(@PathVariable Long parcelleId) {
		List<ProgrammeArrosage> programmes = progArrosageService.getProgrammesByParcelle(parcelleId);
		return new ResponseEntity<>(programmes, HttpStatus.OK);
	}
	
	// Récupérer les programmes par statut
	@GetMapping("/statut/{statut}")
	public ResponseEntity<List<ProgrammeArrosage>> getProgrammesByStatut(@PathVariable String statut) {
		List<ProgrammeArrosage> programmes = progArrosageService.getProgrammesByStatut(statut);
		return new ResponseEntity<>(programmes, HttpStatus.OK);
	}
	
	// Récupérer les programmes par période
	@GetMapping("/period")
	public ResponseEntity<List<ProgrammeArrosage>> getProgrammesByPeriod(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		List<ProgrammeArrosage> programmes = progArrosageService.getProgrammesByPeriod(start, end);
		return new ResponseEntity<>(programmes, HttpStatus.OK);
	}
	
	// Mettre à jour un programme
	@PutMapping("/update/{id}")
	public ResponseEntity<ProgrammeArrosage> updateProgramme(
			@PathVariable Long id,
			@Valid @RequestBody ProgrammeArrosage programme) {
		ProgrammeArrosage updated = progArrosageService.updateProgramme(id, programme);
		if (updated != null) {
			return new ResponseEntity<>(updated, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// Supprimer un programme
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteProgramme(@PathVariable Long id) {
		progArrosageService.deleteProgramme(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}