package com.example.demo.Clients;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.DTOs.PrevisionDTO;

@FeignClient(name = "Meteo", path = "/api/previsions")
public interface MeteoClient {
	
	@GetMapping("/station/{stationId}")
	List<PrevisionDTO> getPrevisionsByStation(@PathVariable("stationId") Long stationId);
	
	@GetMapping("/station/{stationId}/date/{date}")
	List<PrevisionDTO> getPrevisionsByStationAndDate(
			@PathVariable("stationId") Long stationId,
			@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
	
	@GetMapping("/station/{stationId}/period")
	List<PrevisionDTO> getPrevisionsByStationAndPeriod(
			@PathVariable("stationId") Long stationId,
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}