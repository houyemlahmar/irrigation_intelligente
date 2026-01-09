import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProgrammeService } from '../../services/programme.service';
import { StationService } from '../../services/station.service';
import { NotificationService } from '../../services/notification.service';
import { ProgrammeArrosage, StationMeteo } from '../../models/irrigation.models';
import { MapPickerComponent } from '../../components/map-picker/map-picker.component';

@Component({
  selector: 'app-programmes',
  standalone: true,
  imports: [CommonModule, FormsModule, MapPickerComponent],
  templateUrl: './programmes.component.html',
  styleUrl: './programmes.component.scss'
})
export class ProgrammesComponent implements OnInit, OnDestroy {
  programmes: ProgrammeArrosage[] = [];
  filteredProgrammes: ProgrammeArrosage[] = [];
  stations: StationMeteo[] = [];
  loading = true;
  showModal = false;
  showPlanificationModal = false;
  filterStatut = 'TOUS';

  // Pour le compteur d'exécution
  programmesEnCours: Map<number, { dureeRestante: number, interval: any }> = new Map();

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
    private stationService: StationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadProgrammes();
    this.loadStations();
  }

  ngOnDestroy() {
    // Nettoyer tous les intervals
    this.programmesEnCours.forEach(prog => {
      if (prog.interval) {
        clearInterval(prog.interval);
      }
    });
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
        
        // Restaurer les compteurs pour les programmes EN_COURS
        this.restaurerCompteurs();
      },
      error: (err) => {
        console.error('Error loading programmes:', err);
        this.loading = false;
      }
    });
  }

  restaurerCompteurs() {
    // D'abord vérifier côté backend les programmes expirés
    this.programmeService.verifierProgrammesExpires().subscribe({
      next: (programmesTermines) => {
        if (programmesTermines.length > 0) {
          console.log(`${programmesTermines.length} programme(s) expiré(s) terminé(s) automatiquement`);
          // Recharger pour avoir les statuts à jour
          this.loadProgrammesInternal();
          return;
        }
        
        // Puis restaurer les compteurs pour les programmes encore EN_COURS
        this.restaurerCompteursInternal();
      },
      error: (err) => {
        console.error('Erreur lors de la vérification des programmes expirés:', err);
        // Continuer quand même avec la restauration locale
        this.restaurerCompteursInternal();
      }
    });
  }

  private restaurerCompteursInternal() {
    const programmesEnCours = this.programmes.filter(p => p.statut === 'EN_COURS');
    
    programmesEnCours.forEach(programme => {
      if (!programme.id) return;

      const dateDebut = new Date(programme.datePlanifiee);
      const maintenant = new Date();
      const tempsEcouleMs = maintenant.getTime() - dateDebut.getTime();
      const tempsEcouleSecondes = Math.floor(tempsEcouleMs / 1000);
      const dureeTotaleSecondes = programme.duree * 60;
      const dureeRestanteSecondes = dureeTotaleSecondes - tempsEcouleSecondes;

      if (dureeRestanteSecondes <= 0) {
        // Le programme aurait dû être terminé, le terminer maintenant
        console.log(`Programme ${programme.id} devrait être terminé, termination automatique...`);
        this.terminerProgrammeAutomatique(programme);
      } else {
        // Démarrer le compteur avec le temps restant
        console.log(`Restauration du compteur pour programme ${programme.id}, temps restant: ${dureeRestanteSecondes}s`);
        this.demarrerCompteurAvecDureeRestante(programme, dureeRestanteSecondes);
      }
    });
  }

  private loadProgrammesInternal() {
    this.programmeService.getAllProgrammes().subscribe({
      next: (programmes) => {
        this.programmes = programmes.sort((a, b) => 
          new Date(b.datePlanifiee).getTime() - new Date(a.datePlanifiee).getTime()
        );
        this.applyFilter();
        this.restaurerCompteursInternal();
      }
    });
  }

  demarrerCompteurAvecDureeRestante(programme: ProgrammeArrosage, dureeRestanteSecondes: number) {
    if (!programme.id) return;

    // Créer l'entrée dans la map
    this.programmesEnCours.set(programme.id, {
      dureeRestante: dureeRestanteSecondes,
      interval: null
    });

    // Démarrer le compteur (mise à jour chaque seconde)
    const interval = setInterval(() => {
      const progData = this.programmesEnCours.get(programme.id!);
      if (!progData) return;

      progData.dureeRestante--;

      if (progData.dureeRestante <= 0) {
        // Temps écoulé, terminer le programme
        clearInterval(interval);
        this.terminerProgrammeAutomatique(programme);
      }
    }, 1000);

    // Stocker l'interval
    const progData = this.programmesEnCours.get(programme.id);
    if (progData) {
      progData.interval = interval;
    }
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

  onPlanificationLocationSelected(location: { latitude: number, longitude: number }) {
    this.planification.latitude = location.latitude;
    this.planification.longitude = location.longitude;
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
        next: (programme) => {
          this.loadProgrammes();
          this.closePlanificationModal();
          
          // Vérifier les conditions critiques et notifier
          this.verifierConditionsCritiquesTempsReel(programme);
        },
        error: (err) => console.error('Error planning programme:', err)
      });
    }
  }

  executerProgramme(programme: ProgrammeArrosage) {
    if (!programme.id) return;

    // Démarrer le programme (passe à EN_COURS)
    this.programmeService.demarrerProgramme(programme.id).subscribe({
      next: (programmeUpdated) => {
        // Mettre à jour le statut localement
        const index = this.programmes.findIndex(p => p.id === programme.id);
        if (index !== -1) {
          this.programmes[index] = programmeUpdated;
          this.applyFilter();
        }

        // Démarrer le compteur
        this.demarrerCompteur(programmeUpdated);
      },
      error: (err) => console.error('Error starting programme:', err)
    });
  }

  demarrerCompteur(programme: ProgrammeArrosage) {
    if (!programme.id) return;

    const dureeEnSecondes = programme.duree * 60; // Convertir minutes en secondes
    this.demarrerCompteurAvecDureeRestante(programme, dureeEnSecondes);
  }

  terminerProgrammeAutomatique(programme: ProgrammeArrosage) {
    if (!programme.id) return;

    this.programmeService.terminerProgramme(programme.id).subscribe({
      next: () => {
        // Retirer de la map
        this.programmesEnCours.delete(programme.id!);
        // Recharger les programmes
        this.loadProgrammes();
      },
      error: (err) => {
        console.error('Error terminating programme:', err);
        this.programmesEnCours.delete(programme.id!);
      }
    });
  }

  getDureeRestante(programmeId: number): string {
    const progData = this.programmesEnCours.get(programmeId);
    if (!progData) return '';

    const minutes = Math.floor(progData.dureeRestante / 60);
    const secondes = progData.dureeRestante % 60;
    return `${minutes}:${secondes.toString().padStart(2, '0')}`;
  }

  isProgrammeEnCours(programmeId?: number): boolean {
    if (!programmeId) return false;
    return this.programmesEnCours.has(programmeId);
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
    return programme.statut === 'PLANIFIE' || programme.statut === 'EN_COURS';
  }

  verifierConditionsCritiquesTempsReel(programme: ProgrammeArrosage) {
    // Vérifier si le programme a été annulé (conditions critiques)
    if (programme.statut === 'ANNULE' && programme.duree === 0) {
      this.notificationService.addNotification(
        'warning',
        '⚠️ Conditions Météo Critiques',
        'Forte pluie détectée en temps réel - Le programme d\'arrosage a été annulé automatiquement'
      );
    }
    // Vérifier si augmentation significative (température > 35°C)
    else if (programme.duree >= 45 && programme.volumePrevu >= 750) {
      this.notificationService.addNotification(
        'warning',
        '🔥 Température Élevée Détectée',
        `Température élevée en temps réel - Programme ajusté : ${programme.duree} min, ${programme.volumePrevu}L`
      );
    }
    // Conditions normales ou légères modifications
    else {
      this.notificationService.addNotification(
        'success',
        '✅ Programme Planifié',
        `Programme créé avec succès : ${programme.duree} min, ${programme.volumePrevu}L`
      );
    }
  }
}