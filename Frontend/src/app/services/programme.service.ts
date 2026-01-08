import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProgrammeArrosage, JournalArrosage } from '../models/irrigation.models';

@Injectable({
  providedIn: 'root'
})
export class ProgrammeService {
  private apiUrl = `${environment.apiUrl}/programmes`;

  constructor(private http: HttpClient) {}

  getAllProgrammes(): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}`);
  }

  getProgrammeById(id: number): Observable<ProgrammeArrosage> {
    return this.http.get<ProgrammeArrosage>(`${this.apiUrl}/${id}`);
  }

  getProgrammesByParcelle(parcelleId: number): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}/parcelle/${parcelleId}`);
  }

  getProgrammesByStatut(statut: string): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getProgrammesByPeriod(start: string, end: string): Observable<ProgrammeArrosage[]> {
    const params = new HttpParams()
      .set('start', start)
      .set('end', end);
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}/period`, { params });
  }

  createProgrammeAuto(programme: ProgrammeArrosage): Observable<ProgrammeArrosage> {
    return this.http.post<ProgrammeArrosage>(`${this.apiUrl}/auto`, programme);
  }

  planifierArrosageAuto(parcelleId: number, stationMeteoId: number, date: string): Observable<ProgrammeArrosage> {
    const params = new HttpParams()
      .set('parcelleId', parcelleId.toString())
      .set('stationMeteoId', stationMeteoId.toString())
      .set('date', date);
    return this.http.post<ProgrammeArrosage>(`${this.apiUrl}/planifier-auto`, null, { params });
  }

  planifierArrosageTempsReel(parcelleId: number, latitude: number, longitude: number): Observable<ProgrammeArrosage> {
    const params = new HttpParams()
      .set('parcelleId', parcelleId.toString())
      .set('latitude', latitude.toString())
      .set('longitude', longitude.toString());
    return this.http.post<ProgrammeArrosage>(`${this.apiUrl}/planifier-temps-reel`, null, { params });
  }

  executerProgramme(id: number, volumeReel: number, remarque?: string): Observable<JournalArrosage> {
    let params = new HttpParams().set('volumeReel', volumeReel.toString());
    if (remarque) {
      params = params.set('remarque', remarque);
    }
    return this.http.post<JournalArrosage>(`${this.apiUrl}/${id}/executer`, null, { params });
  }

  updateProgramme(id: number, programme: ProgrammeArrosage): Observable<ProgrammeArrosage> {
    return this.http.put<ProgrammeArrosage>(`${this.apiUrl}/update/${id}`, programme);
  }

  deleteProgramme(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
