import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PrevisionService } from '../../services/prevision.service';
import { StationService } from '../../services/station.service';
import { NotificationService } from '../../services/notification.service';
import { PrevisionMeteo, StationMeteo } from '../../models/irrigation.models';

@Component({
  selector: 'app-previsions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './previsions.component.html',
  styleUrl: './previsions.component.scss'
})
export class PrevisionsComponent implements OnInit {
  previsions: PrevisionMeteo[] = [];
  filteredPrevisions: PrevisionMeteo[] = [];
  stations: StationMeteo[] = [];
  loading = true;
  showModal = false;
  editMode = false;
  filterStationId: number | null = null;

  currentPrevision: PrevisionMeteo = this.createEmptyPrevision();
  selectedStationId: number | undefined = undefined;

  constructor(
    private previsionService: PrevisionService,
    private stationService: StationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadPrevisions();
    this.loadStations();
  }

  createEmptyPrevision(): PrevisionMeteo {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return {
      date: tomorrow.toISOString().split('T')[0],
      temperatureMax: 25,
      temperatureMin: 15,
      pluiePrevue: 0,
      vent: 10,
      humidite: 60
    };
  }

  loadPrevisions() {
    this.loading = true;
    this.previsionService.getAllPrevisions().subscribe({
      next: (previsions) => {
        this.previsions = previsions.sort((a, b) => 
          new Date(b.date).getTime() - new Date(a.date).getTime()
        );
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading previsions:', err);
        this.loading = false;
      }
    });
  }

  loadStations() {
    this.stationService.getAllStations().subscribe({
      next: (stations) => {
        this.stations = stations;
      },
      error: (err) => console.error('Error loading stations:', err)
    });
  }

  applyFilter() {
    if (this.filterStationId) {
      this.filteredPrevisions = this.previsions.filter(p => 
        p.station?.id === this.filterStationId
      );
    } else {
      this.filteredPrevisions = this.previsions;
    }
  }

  openCreateModal() {
    this.editMode = false;
    this.currentPrevision = this.createEmptyPrevision();
    this.selectedStationId = undefined;
    this.showModal = true;
  }

  openEditModal(prevision: PrevisionMeteo) {
    this.editMode = true;
    this.currentPrevision = { ...prevision };
    this.selectedStationId = prevision.station?.id;
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  savePrevision() {
    const previsionToSave: any = {
      ...this.currentPrevision,
      station: { id: this.selectedStationId }
    };

    if (this.editMode && this.currentPrevision.id) {
      this.previsionService.updatePrevision(this.currentPrevision.id, previsionToSave).subscribe({
        next: (updated) => {
          this.checkAndNotifyCriticalConditions(updated);
          this.loadPrevisions();
          this.closeModal();
          this.notificationService.addNotification('success', 'Prévision mise à jour', 
            `Prévision modifiée avec succès`);
        },
        error: (err) => console.error('Error updating prevision:', err)
      });
    } else {
      this.previsionService.createPrevision(previsionToSave).subscribe({
        next: (created) => {
          this.checkAndNotifyCriticalConditions(created);
          this.loadPrevisions();
          this.closeModal();
          this.notificationService.addNotification('success', 'Prévision créée', 
            `Nouvelle prévision ajoutée avec succès`);
        },
        error: (err) => console.error('Error creating prevision:', err)
      });
    }
  }

  checkAndNotifyCriticalConditions(prevision: PrevisionMeteo) {
    const isCritical = (prevision.pluiePrevue && prevision.pluiePrevue > 10) || 
                       (prevision.temperatureMax && prevision.temperatureMax > 35);
    
    if (isCritical) {
      let message = '';
      if (prevision.pluiePrevue && prevision.pluiePrevue > 10) {
        message = `Forte pluie prévue: ${prevision.pluiePrevue}mm - Les programmes d'arrosage seront ajustés automatiquement`;
      } else {
        message = `Température élevée: ${prevision.temperatureMax}°C - Les programmes d'arrosage seront ajustés automatiquement`;
      }
      
      this.notificationService.addNotification('warning', '⚠️ Conditions Critiques Détectées', message);
    }
  }

  deletePrevision(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette prévision ?')) {
      this.previsionService.deletePrevision(id).subscribe({
        next: () => {
          this.loadPrevisions();
          this.notificationService.addNotification('info', 'Prévision supprimée', 
            'La prévision a été supprimée');
        },
        error: (err) => console.error('Error deleting prevision:', err)
      });
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  getWeatherIcon(prevision: PrevisionMeteo): string {
    if (prevision.pluiePrevue && prevision.pluiePrevue > 10) return '⛈️';
    if (prevision.pluiePrevue && prevision.pluiePrevue > 0) return '🌧️';
    if (prevision.temperatureMax && prevision.temperatureMax > 35) return '🔥';
    if (prevision.temperatureMax && prevision.temperatureMax > 30) return '☀️';
    return '⛅';
  }

  getAlertClass(prevision: PrevisionMeteo): string {
    if ((prevision.pluiePrevue && prevision.pluiePrevue > 10) || 
        (prevision.temperatureMax && prevision.temperatureMax > 35)) {
      return 'alert-critical';
    }
    return '';
  }
}
