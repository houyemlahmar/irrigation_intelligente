// Station Météo
export interface StationMeteo {
  id?: number;
  nom: string;
  localisation: string;
  latitude?: number;
  longitude?: number;
  active: boolean;
}

// Prévision Météo
export interface PrevisionMeteo {
  id?: number;
  date: string;
  temperatureMax: number;
  temperatureMin: number;
  pluiePrevue: number;
  vent: number;
  humidite: number;
  station?: StationMeteo;
}

// Programme Arrosage
export interface ProgrammeArrosage {
  id?: number;
  parcelleId: number;
  datePlanifiee: string;
  duree: number;
  volumePrevu: number;
  statut: 'PLANIFIE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  stationMeteoId?: number;
  StationMeteo?: StationMeteo;
}

// Journal Arrosage
export interface JournalArrosage {
  id?: number;
  programmeId: number;
  dateExecution: string;
  volumeReel: number;
  remarque?: string;
}

// Weather DTO (temps réel)
export interface WeatherDTO {
  stationId?: number;
  latitude: number;
  longitude: number;
  temperature: number;
  humidity: number;
  windSpeed: number;
  weatherCode: number;
  precipitation: number;
  timestamp: string;
}

// Dashboard Stats
export interface DashboardStats {
  totalStations: number;
  activeStations: number;
  programmesActifs: number;
  programmesAujourdhui: number;
  volumeTotal: number;
}
