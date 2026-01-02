package com.example.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.StationMeteo;

@Repository
public interface StationMeteoRepository extends JpaRepository<StationMeteo, Long> {
	
	List<StationMeteo> findByActiveTrue();
	
	List<StationMeteo> findByLocalisation(String localisation);
}
