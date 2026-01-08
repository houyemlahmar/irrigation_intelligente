import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StationService } from '../../services/station.service';
import { StationMeteo, WeatherDTO } from '../../models/irrigation.models';

@Component({
  selector: 'app-stations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stations.component.html',
  styleUrl: './stations.component.scss'
})
export class StationsComponent implements OnInit {
  stations: StationMeteo[] = [];
  filteredStations: StationMeteo[] = [];
  loading = true;
  showCreateModal = false;
  editMode = false;
  searchQuery = '';

  currentStation: StationMeteo = {
    nom: '',
    localisation: '',
    latitude: undefined,
    longitude: undefined,
    active: true
  };

  weatherData: Map<number, WeatherDTO> = new Map();

  constructor(private stationService: StationService) {}

  ngOnInit() {
    this.loadStations();
  }

  loadStations() {
    this.loading = true;
    this.stationService.getAllStations().subscribe({
      next: (stations) => {
        this.stations = stations;
        this.filteredStations = stations;
        this.loading = false;

        // Load weather for stations with coordinates
        stations.forEach(station => {
          if (station.id && station.latitude && station.longitude) {
            this.loadWeather(station.id);
          }
        });
      },
      error: (err) => {
        console.error('Error loading stations:', err);
        this.loading = false;
      }
    });
  }

  loadWeather(stationId: number) {
    this.stationService.getStationWeather(stationId).subscribe({
      next: (weather) => {
        this.weatherData.set(stationId, weather);
      },
      error: (err) => console.error(`Error loading weather for station ${stationId}:`, err)
    });
  }

  filterStations() {
    const query = this.searchQuery.toLowerCase();
    this.filteredStations = this.stations.filter(station =>
      station.nom.toLowerCase().includes(query) ||
      station.localisation.toLowerCase().includes(query)
    );
  }

  openCreateModal() {
    this.editMode = false;
    this.currentStation = {
      nom: '',
      localisation: '',
      latitude: undefined,
      longitude: undefined,
      active: true
    };
    this.showCreateModal = true;
  }

  openEditModal(station: StationMeteo) {
    this.editMode = true;
    this.currentStation = { ...station };
    this.showCreateModal = true;
  }

  closeModal() {
    this.showCreateModal = false;
  }

  saveStation() {
    if (this.editMode && this.currentStation.id) {
      this.stationService.updateStation(this.currentStation.id, this.currentStation).subscribe({
        next: () => {
          this.loadStations();
          this.closeModal();
        },
        error: (err) => console.error('Error updating station:', err)
      });
    } else {
      this.stationService.createStation(this.currentStation).subscribe({
        next: () => {
          this.loadStations();
          this.closeModal();
        },
        error: (err) => console.error('Error creating station:', err)
      });
    }
  }

  deleteStation(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette station ?')) {
      this.stationService.deleteStation(id).subscribe({
        next: () => {
          this.loadStations();
        },
        error: (err) => console.error('Error deleting station:', err)
      });
    }
  }

  getWeather(stationId: number): WeatherDTO | undefined {
    return this.weatherData.get(stationId);
  }

  getWeatherIcon(weatherCode: number): string {
    // Simplified weather code interpretation
    if (weatherCode === 0) return '☀️';
    if (weatherCode <= 3) return '⛅';
    if (weatherCode <= 48) return '☁️';
    if (weatherCode <= 67) return '🌧️';
    if (weatherCode <= 77) return '❄️';
    return '⛈️';
  }
}
