import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { StationMeteo, WeatherDTO } from '../models/irrigation.models';

@Injectable({
  providedIn: 'root'
})
export class StationService {
  private apiUrl = `${environment.apiUrl}/stations`;

  constructor(private http: HttpClient) {}

  getAllStations(): Observable<StationMeteo[]> {
    return this.http.get<StationMeteo[]>(`${this.apiUrl}/all`);
  }

  getActiveStations(): Observable<StationMeteo[]> {
    return this.http.get<StationMeteo[]>(`${this.apiUrl}/active`);
  }

  getStationById(id: number): Observable<StationMeteo> {
    return this.http.get<StationMeteo>(`${this.apiUrl}/${id}`);
  }

  getStationWeather(id: number): Observable<WeatherDTO> {
    return this.http.get<WeatherDTO>(`${this.apiUrl}/${id}/weather`);
  }

  createStation(station: StationMeteo): Observable<StationMeteo> {
    return this.http.post<StationMeteo>(`${this.apiUrl}/create`, station);
  }

  updateStation(id: number, station: StationMeteo): Observable<StationMeteo> {
    return this.http.put<StationMeteo>(`${this.apiUrl}/update/${id}`, station);
  }

  deleteStation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
