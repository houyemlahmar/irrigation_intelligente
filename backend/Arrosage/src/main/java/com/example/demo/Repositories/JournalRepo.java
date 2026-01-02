package com.example.demo.Repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.JournalArrosage;

@Repository
public interface JournalRepo extends JpaRepository<JournalArrosage, Long> {
	
	List<JournalArrosage> findByProgrammeId(Long programmeId);
	
	List<JournalArrosage> findByDateExecutionBetween(LocalDateTime start, LocalDateTime end);
}
