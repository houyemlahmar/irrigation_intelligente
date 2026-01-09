import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';

@Component({
  selector: 'app-map-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map-picker.component.html',
  styleUrl: './map-picker.component.scss'
})
export class MapPickerComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() latitude: number = 36.8065; // Tunis par défaut
  @Input() longitude: number = 10.1815;
  @Input() height: string = '400px';
  @Output() locationSelected = new EventEmitter<{ latitude: number, longitude: number }>();

  private map: L.Map | null = null;
  private marker: L.Marker | null = null;

  ngOnInit() {
    // Correction du bug des icônes Leaflet avec Webpack
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
      iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png'
    });
  }

  ngAfterViewInit() {
    setTimeout(() => this.initMap(), 100);
  }

  ngOnDestroy() {
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap() {
    // Initialiser la carte
    this.map = L.map('map-picker', {
      center: [this.latitude, this.longitude],
      zoom: 13
    });

    // Ajouter la couche de tuiles OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Ajouter un marqueur initial
    this.addMarker(this.latitude, this.longitude);

    // Écouter les clics sur la carte
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;
      this.addMarker(lat, lng);
      this.locationSelected.emit({ latitude: lat, longitude: lng });
    });
  }

  private addMarker(lat: number, lng: number) {
    if (!this.map) return;

    // Supprimer l'ancien marqueur
    if (this.marker) {
      this.map.removeLayer(this.marker);
    }

    // Ajouter un nouveau marqueur
    this.marker = L.marker([lat, lng]).addTo(this.map);
    this.marker.bindPopup(`<b>Position sélectionnée</b><br>Lat: ${lat.toFixed(4)}<br>Lng: ${lng.toFixed(4)}`).openPopup();
  }

  public updatePosition(lat: number, lng: number) {
    this.latitude = lat;
    this.longitude = lng;
    
    if (this.map) {
      this.map.setView([lat, lng], 13);
      this.addMarker(lat, lng);
    }
  }
}
