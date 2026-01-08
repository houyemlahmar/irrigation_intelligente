import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { JournalArrosage } from '../models/irrigation.models';

@Injectable({
  providedIn: 'root'
})
export class JournalService {
  private apiUrl = `${environment.apiUrl}/journal`;

  constructor(private http: HttpClient) {}

  getAllJournaux(): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}`);
  }

  getJournalById(id: number): Observable<JournalArrosage> {
    return this.http.get<JournalArrosage>(`${this.apiUrl}/${id}`);
  }

  getJournauxByProgramme(programmeId: number): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}/programme/${programmeId}`);
  }

  getJournauxByPeriod(start: string, end: string): Observable<JournalArrosage[]> {
    const params = new HttpParams()
      .set('start', start)
      .set('end', end);
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}/period`, { params });
  }
}
