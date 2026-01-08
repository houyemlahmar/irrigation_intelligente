import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { StationsComponent } from './pages/stations/stations.component';
import { ProgrammesComponent } from './pages/programmes/programmes.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'stations', component: StationsComponent },
  { path: 'programmes', component: ProgrammesComponent },
  { path: '**', redirectTo: '/dashboard' }
];
