import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StationService } from '../../services/station.service';
import { ProgrammeService } from '../../services/programme.service';
import { StationMeteo, ProgrammeArrosage, DashboardStats } from '../../models/irrigation.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats = {
    totalStations: 0,
    activeStations: 0,
    programmesActifs: 0,
    programmesAujourdhui: 0,
    volumeTotal: 0
  };

  recentProgrammes: ProgrammeArrosage[] = [];
  stations: StationMeteo[] = [];
  loading = true;

  constructor(
    private stationService: StationService,
    private programmeService: ProgrammeService
  ) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.loading = true;

    // Load stations
    this.stationService.getAllStations().subscribe({
      next: (stations) => {
        this.stations = stations;
        this.stats.totalStations = stations.length;
        this.stats.activeStations = stations.filter(s => s.active).length;
      },
      error: (err) => console.error('Error loading stations:', err)
    });

    // Load programmes
    this.programmeService.getAllProgrammes().subscribe({
      next: (programmes) => {
        this.recentProgrammes = programmes.slice(0, 5);
        this.stats.programmesActifs = programmes.filter(p => p.statut === 'PLANIFIE' || p.statut === 'EN_COURS').length;
        
        const today = new Date().toISOString().split('T')[0];
        this.stats.programmesAujourdhui = programmes.filter(p => 
          p.datePlanifiee.startsWith(today)
        ).length;

        this.stats.volumeTotal = programmes
          .filter(p => p.statut === 'TERMINE')
          .reduce((sum, p) => sum + (p.volumePrevu || 0), 0);

        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading programmes:', err);
        this.loading = false;
      }
    });
  }

  getStatutClass(statut: string): string {
    const classes: any = {
      'PLANIFIE': 'status-planned',
      'EN_COURS': 'status-progress',
      'TERMINE': 'status-completed',
      'ANNULE': 'status-cancelled'
    };
    return classes[statut] || '';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
