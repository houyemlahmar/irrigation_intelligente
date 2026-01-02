# 🧪 Guide Complet des Tests Postman - Projets Meteo & Arrosage

## 📋 Ordre de Démarrage des Services

1. **MS_config** (port 9999)
2. **ms_eureka** (port 8761) 
3. **Meteo** (port 8084)
4. **Arrosage** (port 8083)
5. **Gateway** (port 9090)

---

## 🌤️ Service METEO (Port 8084)

### StationMeteo APIs

| Méthode | URL | Corps JSON |
|---------|-----|------------|
| GET | `http://localhost:8084/api/stations/all` | - |
| GET | `http://localhost:8084/api/stations/1` | - |
| GET | `http://localhost:8084/api/stations/active` | - |
| POST | `http://localhost:8084/api/stations/create` | ✓ Voir ci-dessous |
| PUT | `http://localhost:8084/api/stations/update/1` | ✓ Voir ci-dessous |
| DELETE | `http://localhost:8084/api/stations/delete/1` | - |
| **GET** | **`http://localhost:8084/api/stations/1/weather`** | - **(NOUVEAU)** |

**JSON pour POST/PUT Station:**
```json
{
  "nom": "Station Tunis",
  "localisation": "Tunis Centre",
  "latitude": 36.8065,
  "longitude": 10.1815,
  "active": true
}
```

### PrevisionMeteo APIs

| Méthode | URL | Corps JSON |
|---------|-----|------------|
| GET | `http://localhost:8084/api/previsions/all` | - |
| GET | `http://localhost:8084/api/previsions/1` | - |
| GET | `http://localhost:8084/api/previsions/station/1` | - |
| GET | `http://localhost:8084/api/previsions/station/1/date/2025-01-15` | - |
| GET | `http://localhost:8084/api/previsions/station/1/period?startDate=2025-01-01&endDate=2025-01-31` | - |
| POST | `http://localhost:8084/api/previsions` | ✓ Voir ci-dessous |
| PUT | `http://localhost:8084/api/previsions/update/1` | ✓ Voir ci-dessous |
| DELETE | `http://localhost:8084/api/previsions/delete/1` | - |

**JSON pour POST/PUT Prevision:**
```json
{
  "date": "2025-01-15",
  "temperatureMax": 28.5,
  "temperatureMin": 15.0,
  "pluiePrevue": 5.2,
  "vent": 12.0,
  "humidite": 65.0,
  "station": {
    "id": 1
  }
}
```

### 🆕 Open-Meteo APIs (Météo en Temps Réel)

| Méthode | URL | Description |
|---------|-----|-------------|
| **GET** | `http://localhost:8084/api/weather/test` | Test avec Tunis |
| **GET** | `http://localhost:8084/api/weather/current?latitude=36.8065&longitude=10.1815` | Météo actuelle |

**Exemples de coordonnées:**
- Tunis: `latitude=36.8065&longitude=10.1815`
- Paris: `latitude=48.8566&longitude=2.3522`
- New York: `latitude=40.7128&longitude=-74.0060`

**Réponse Exemple:**
```json
{
  "temperature": 18.5,
  "windSpeed": 12.0,
  "weatherCode": 0,
  "weatherDescription": "Ciel dégagé",
  "time": "2025-12-31T14:30",
  "latitude": 36.8065,
  "longitude": 10.1815
}
```

---

## 💧 Service ARROSAGE (Port 8083)

### ProgrammeArrosage APIs

| Méthode | URL | Corps JSON |
|---------|-----|------------|
| GET | `http://localhost:8083/api/programmes` | - |
| GET | `http://localhost:8083/api/programmes/1` | - |
| GET | `http://localhost:8083/api/programmes/parcelle/1` | - |
| GET | `http://localhost:8083/api/programmes/statut/PLANIFIE` | - |
| GET | `http://localhost:8083/api/programmes/period?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59` | - |
| POST | `http://localhost:8083/api/programmes/auto` | ✓ Voir ci-dessous |
| POST | `http://localhost:8083/api/programmes/planifier-auto?parcelleId=1&stationMeteoId=1&date=2025-01-15` | - |
| **POST** | **`http://localhost:8083/api/programmes/planifier-temps-reel?parcelleId=1&latitude=36.8065&longitude=10.1815`** | - **(NOUVEAU)** |
| POST | `http://localhost:8083/api/programmes/1/executer?volumeReel=150.5&remarque=Test` | - |
| PUT | `http://localhost:8083/api/programmes/update/1` | ✓ Voir ci-dessous |
| DELETE | `http://localhost:8083/api/programmes/delete/1` | - |

**JSON pour POST/PUT Programme:**
```json
{
  "parcelleId": 1,
  "datePlanifiee": "2025-01-15T08:00:00",
  "duree": 30,
  "volumePrevu": 200.0,
  "statut": "PLANIFIE",
  "stationMeteoId": 1
}
```

### JournalArrosage APIs

| Méthode | URL |
|---------|-----|
| GET | `http://localhost:8083/api/journal` |
| GET | `http://localhost:8083/api/journal/1` |
| GET | `http://localhost:8083/api/journal/programme/1` |
| GET | `http://localhost:8083/api/journal/period?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59` |

---
## 🚀 Scénarios de Test Complets
Scénario 1 : Test Météo Temps Réel
### 1. Tester avec Tunis
GET http://localhost:8084/api/weather/test

### 2. Tester avec coordonnées personnalisées (Paris)
GET http://localhost:8084/api/weather/current?latitude=48.8566&longitude=2.3522

Scénario 2 : Planification avec Météo Temps Réel
### 1. Planifier l'arrosage avec météo actuelle
POST http://localhost:8083/api/programmes/planifier-temps-reel?parcelleId=1&latitude=36.8065&longitude=10.1815

### Le système :
### - Récupère la météo actuelle via Open-Meteo
### - Ajuste automatiquement la durée et le volume
### - Crée le programme optimisé

Scénario 3 : Événement RabbitMQ
### 1. Créer une prévision avec conditions critiques
POST http://localhost:8084/api/previsions
{
  "date": "2026-01-15",
  "temperatureMax": 40.0,      # ⚠️ Temp élevée !
  "temperatureMin": 25.0,
  "pluiePrevue": 0,
  "vent": 15.0,
  "humidite": 30.0,
  "station": { "id": 1 }
}

### 2. Le service Meteo publie automatiquement un événement RabbitMQ

### 3. Le service Arrosage reçoit l'événement et ajuste les programmes

### 4. Vérifier les programmes ajustés
GET http://localhost:8083/api/programmes/statut/PLANIFIE

Scénario 4 : Workflow Complet
### 1. Créer une station avec GPS
POST http://localhost:8084/api/stations/create
{
  "nom": "Station Test",
  "localisation": "Tunis",
  "latitude": 36.8065,
  "longitude": 10.1815,
  "active": true
}

### 2. Récupérer la météo temps réel de la station
GET http://localhost:8084/api/stations/1/weather

### 3. Planifier l'arrosage avec météo temps réel
POST http://localhost:8083/api/programmes/planifier-temps-reel?parcelleId=1&latitude=36.8065&longitude=10.1815

### 4. Exécuter le programme
POST http://localhost:8083/api/programmes/1/executer?volumeReel=450.5&remarque=Exécution réussie

### 5. Consulter le journal
GET http://localhost:8083/api/journal/programme/1

---

## 📊 Codes Météo WMO

| Code | Description | Impact sur Arrosage |
|------|-------------|---------------------|
| 0 | Ciel dégagé | Normal |
| 1-3 | Clair à couvert | Normal |
| 45-48 | Brouillard | Normal |
| 51-67 | Pluie | **ANNULÉ** |
| 71-86 | Neige | **ANNULÉ** |
| 95-99 | Orage | **ANNULÉ** |

## 🎯 Logique d'Ajustement Automatique

### Température
- **> 35°C**: +50% (45min, 750L)
- **30-35°C**: +30% (40min, 650L)
- **15-30°C**: Normal (30min, 500L)
- **< 15°C**: -40% (20min, 300L)

### Vent
- **> 30 km/h**: -20% pour éviter l'évaporation

### Pluie
- **En cours**: Programme annulé
- **Prévue > 10mm**: Annulé
- **Prévue 5-10mm**: -70%
- **Prévue 0-5mm**: -50%

---

## ⚠️ Points Importants

**Pas d'espaces dans les URLs !**
❌ `date/2025-01-15 ` (avec espace)
✅ `date/2025-01-15` (sans espace)

**Format des Query Params:**
✅ `?latitude=36.8065&longitude=10.1815`
❌ `?latitude=36.8065 &longitude=10.1815 `

---

## ✅ Checklist de Test

- [ ] Config Server démarré (9999)
- [ ] Eureka démarré (8761)
- [ ] Meteo démarré (8084)
- [ ] Arrosage démarré (8083)
- [ ] Test météo Tunis
- [ ] Test météo coordonnées personnalisées
- [ ] Création station avec GPS
- [ ] Récupération météo station
- [ ] Planification arrosage temps réel
- [ ] Vérification ajustement automatique
- [ ] Exécution programme
- [ ] Consultation journal

---

## 🎯 Résumé des Nouveaux Endpoints

| Service | Endpoint | Description |
|---------|----------|-------------|
| Meteo | `GET /api/weather/test` | Test avec Tunis |
| Meteo | `GET /api/weather/current` | Météo par coordonnées |
| Meteo | `GET /api/stations/{id}/weather` | Météo d'une station |
| Arrosage | `POST /api/programmes/planifier-temps-reel` | Planifier avec météo actuelle |

**Tous les endpoints sont testés et fonctionnels ! 🚀**
