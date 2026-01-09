import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  menuItems = [
    { path: '/dashboard', icon: 'home', label: 'Tableau de Bord' },
    { path: '/stations', icon: 'cloud', label: 'Stations Météo' },
    { path: '/previsions', icon: 'sun', label: 'Prévisions' },
    { path: '/programmes', icon: 'droplet', label: 'Programmes' },
    { path: '/journal', icon: 'file-text', label: 'Journal' },
    { path: '/analytics', icon: 'bar-chart', label: 'Statistiques' }
  ];
}
