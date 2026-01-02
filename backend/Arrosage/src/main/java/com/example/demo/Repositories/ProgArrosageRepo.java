package com.example.demo.Repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.ProgrammeArrosage;

@Repository
public interface ProgArrosageRepo extends JpaRepository<ProgrammeArrosage, Long> {
	
	List<ProgrammeArrosage> findByParcelleId(Long parcelleId);
	
	List<ProgrammeArrosage> findByStatut(String statut);
	
	List<ProgrammeArrosage> findByStationMeteoId(Long stationMeteoId);
	
	List<ProgrammeArrosage> findByDatePlanifieeBetween(LocalDateTime start, LocalDateTime end);
}
