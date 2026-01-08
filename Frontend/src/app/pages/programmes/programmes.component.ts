import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProgrammeService } from '../../services/programme.service';
import { StationService } from '../../services/station.service';
import { ProgrammeArrosage, StationMeteo } from '../../models/irrigation.models';

@Component({
  selector: 'app-programmes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './programmes.component.html',
  styleUrl: './programmes.component.scss'
})
export class ProgrammesComponent implements OnInit {
  programmes: ProgrammeArrosage[] = [];
  filteredProgrammes: ProgrammeArrosage[] = [];
  stations: StationMeteo[] = [];
  loading = true;
  showModal = false;
  showPlanificationModal = false;
  filterStatut = 'TOUS';

  currentProgramme: ProgrammeArrosage = this.createEmptyProgramme();

  planification = {
    type: 'auto', // 'auto' ou 'temps-reel'
    parcelleId: 1,
    stationMeteoId: undefined as number | undefined,
    date: new Date().toISOString().split('T')[0],
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined
  };

  execution = {
    programmeId: undefined as number | undefined,
    volumeReel: 0,
    remarque: ''
  };

  constructor(
    private programmeService: ProgrammeService,
    private stationService: StationService
  ) {}

  ngOnInit() {
    this.loadProgrammes();
    this.loadStations();
  }

  createEmptyProgramme(): ProgrammeArrosage {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return {
      parcelleId: 1,
      datePlanifiee: tomorrow.toISOString().slice(0, 16),
      duree: 30,
      volumePrevu: 100,
      statut: 'PLANIFIE',
      stationMeteoId: undefined
    };
  }

  loadProgrammes() {
    this.loading = true;
    this.programmeService.getAllProgrammes().subscribe({
      next: (programmes) => {
        this.programmes = programmes.sort((a, b) => 
          new Date(b.datePlanifiee).getTime() - new Date(a.datePlanifiee).getTime()
        );
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading programmes:', err);
        this.loading = false;
      }
    });
  }

  loadStations() {
    this.stationService.getActiveStations().subscribe({
      next: (stations) => {
        this.stations = stations;
      },
      error: (err) => console.error('Error loading stations:', err)
    });
  }

  applyFilter() {
    if (this.filterStatut === 'TOUS') {
      this.filteredProgrammes = this.programmes;
    } else {
      this.filteredProgrammes = this.programmes.filter(p => p.statut === this.filterStatut);
    }
  }

  openPlanificationModal() {
    this.showPlanificationModal = true;
  }

  closePlanificationModal() {
    this.showPlanificationModal = false;
  }

  planifierProgramme() {
    if (this.planification.type === 'auto') {
      if (!this.planification.stationMeteoId) {
        alert('Veuillez sélectionner une station météo');
        return;
      }

      this.programmeService.planifierArrosageAuto(
        this.planification.parcelleId,
        this.planification.stationMeteoId,
        this.planification.date
      ).subscribe({
        next: () => {
          this.loadProgrammes();
          this.closePlanificationModal();
        },
        error: (err) => console.error('Error planning programme:', err)
      });
    } else {
      if (!this.planification.latitude || !this.planification.longitude) {
        alert('Veuillez entrer les coordonnées GPS');
        return;
      }

      this.programmeService.planifierArrosageTempsReel(
        this.planification.parcelleId,
        this.planification.latitude,
        this.planification.longitude
      ).subscribe({
        next: () => {
          this.loadProgrammes();
          this.closePlanificationModal();
        },
        error: (err) => console.error('Error planning programme:', err)
      });
    }
  }

  executerProgramme(programme: ProgrammeArrosage) {
    this.execution.programmeId = programme.id;
    this.execution.volumeReel = programme.volumePrevu;
    this.execution.remarque = '';

    const volumeReel = prompt(`Volume réel utilisé (L):`, programme.volumePrevu.toString());
    if (volumeReel) {
      this.programmeService.executerProgramme(
        programme.id!,
        parseFloat(volumeReel),
        this.execution.remarque
      ).subscribe({
        next: () => {
          this.loadProgrammes();
        },
        error: (err) => console.error('Error executing programme:', err)
      });
    }
  }

  deleteProgramme(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce programme ?')) {
      this.programmeService.deleteProgramme(id).subscribe({
        next: () => {
          this.loadProgrammes();
        },
        error: (err) => console.error('Error deleting programme:', err)
      });
    }
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
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isExecutable(programme: ProgrammeArrosage): boolean {
    return programme.statut === 'PLANIFIE';
  }
}
