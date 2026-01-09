import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PrevisionMeteo } from '../models/irrigation.models';

@Injectable({
  providedIn: 'root'
})
export class PrevisionService {
  private apiUrl = `${environment.apiUrl}/previsions`;

  constructor(private http: HttpClient) {}

  getAllPrevisions(): Observable<PrevisionMeteo[]> {
    return this.http.get<PrevisionMeteo[]>(`${this.apiUrl}/all`);
  }

  getPrevisionById(id: number): Observable<PrevisionMeteo> {
    return this.http.get<PrevisionMeteo>(`${this.apiUrl}/${id}`);
  }

  getPrevisionsByStation(stationId: number): Observable<PrevisionMeteo[]> {
    return this.http.get<PrevisionMeteo[]>(`${this.apiUrl}/station/${stationId}`);
  }

  getPrevisionsByStationAndDate(stationId: number, date: string): Observable<PrevisionMeteo[]> {
    return this.http.get<PrevisionMeteo[]>(`${this.apiUrl}/station/${stationId}/date/${date}`);
  }

  createPrevision(prevision: PrevisionMeteo): Observable<PrevisionMeteo> {
    return this.http.post<PrevisionMeteo>(`${this.apiUrl}/create`, prevision);
  }

  updatePrevision(id: number, prevision: PrevisionMeteo): Observable<PrevisionMeteo> {
    return this.http.put<PrevisionMeteo>(`${this.apiUrl}/update/${id}`, prevision);
  }

  deletePrevision(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
