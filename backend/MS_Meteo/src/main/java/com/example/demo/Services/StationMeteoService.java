package com.example.demo.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.StationMeteo;
import com.example.demo.Repositories.StationMeteoRepository;

@Service
public class StationMeteoService {
	
	@Autowired
	private StationMeteoRepository stationRepo;
	
	public List<StationMeteo> getAllStations() {
		return stationRepo.findAll();
	}
	
	public StationMeteo getStationById(Long id) {
		return stationRepo.findById(id).orElse(null);
	}
	
	public List<StationMeteo> getActiveStations() {
		return stationRepo.findByActiveTrue();
	}
	
	public StationMeteo createStation(StationMeteo station) {
		return stationRepo.save(station);
	}
	
	public StationMeteo updateStation(Long id, StationMeteo station) {
		if (stationRepo.existsById(id)) {
			station.setId(id);
			return stationRepo.save(station);
		}
		return null;
	}
	
	public void deleteStation(Long id) {
		stationRepo.deleteById(id);
	}
}
