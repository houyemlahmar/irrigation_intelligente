package com.example.demo.Repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.PrevisionMeteo;

@Repository
public interface PrevisionMeteoRepository extends JpaRepository<PrevisionMeteo, Long> {
	
	List<PrevisionMeteo> findByStationId(Long stationId);
	
	List<PrevisionMeteo> findByStationIdAndDate(Long stationId, LocalDate date);
	
	List<PrevisionMeteo> findByStationIdAndDateBetween(Long stationId, LocalDate startDate, LocalDate endDate);
	
	List<PrevisionMeteo> findByDate(LocalDate date);
}
