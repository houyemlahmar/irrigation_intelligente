# Intégration API Open-Meteo

## 📋 Description

Ce projet intègre l'API Open-Meteo pour fournir des données météorologiques en temps réel sans nécessiter de clé API.

## 🚀 Fonctionnalités

### Client REST Open-Meteo
- ✅ Appels HTTP via RestTemplate
- ✅ Gestion des timeouts (5 secondes)
- ✅ Gestion des erreurs HTTP (4xx/5xx)
- ✅ Gestion des erreurs réseau
- ✅ Validation des coordonnées GPS
- ✅ Conversion des codes météo WMO en descriptions

### Endpoints Disponibles

#### 1. Météo Actuelle par Coordonnées
```http
GET http://localhost:8084/api/weather/current?latitude=36.8065&longitude=10.1815
```

**Réponse:**
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

#### 2. Test avec Coordonnées de Tunis
```http
GET http://localhost:8084/api/weather/test
```

#### 3. Météo pour une Station Météo
```http
GET http://localhost:8084/api/stations/{id}/weather
```

**Exemple:**
```http
GET http://localhost:8084/api/stations/1/weather
```

## 📦 Classes Créées

### DTOs
- **OpenMeteoResponse.java** - Parse la réponse JSON d'Open-Meteo
- **WeatherDTO.java** - DTO simplifié pour les clients

### Client
- **OpenMeteoClient.java** - Client REST pour l'API Open-Meteo
  - Méthode: `getWeather(latitude, longitude)`
  - Gestion complète des erreurs
  - Validation des paramètres

### Service
- **OpenMeteoService.java** - Couche service
  - `getCurrentWeather(latitude, longitude)`
  - `getWeatherForStation(stationId, latitude, longitude)`

### Configuration
- **RestTemplateConfig.java** - Configuration RestTemplate avec timeouts

### Contrôleur
- **OpenMeteoController.java** - Endpoints REST pour la météo

## ⚙️ Configuration

### application.properties
```properties
# Open-Meteo API Configuration
openmeteo.api.base-url=https://api.open-meteo.com/v1/forecast
openmeteo.api.timeout=5000

# Logging
logging.level.com.example.demo.Clients=INFO
logging.level.com.example.demo.Services=INFO
```

## 🔍 Codes Météo WMO

| Code | Description |
|------|-------------|
| 0 | Ciel dégagé |
| 1-3 | Clair à couvert |
| 45-48 | Brouillard |
| 51-67 | Pluie (intensités variées) |
| 71-86 | Neige |
| 95-99 | Orage |

## 🧪 Tests Postman

### 1. Test Météo de Tunis
```http
GET http://localhost:8084/api/weather/test
```

### 2. Test avec Coordonnées Personnalisées
```http
GET http://localhost:8084/api/weather/current?latitude=48.8566&longitude=2.3522
```
*(Paris, France)*

### 3. Météo d'une Station
**Prérequis:** Créer une station avec coordonnées GPS
```http
POST http://localhost:8084/api/stations/create
Content-Type: application/json

{
  "nom": "Station Paris",
  "localisation": "Paris Centre",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "active": true
}
```

Puis récupérer sa météo:
```http
GET http://localhost:8084/api/stations/1/weather
```

## 🛡️ Gestion des Erreurs

### Erreurs HTTP Client (4xx)
- **400 Bad Request** - Paramètres invalides
- **404 Not Found** - Endpoint introuvable

### Erreurs HTTP Serveur (5xx)
- **500-599** - Problèmes serveur Open-Meteo

### Erreurs Réseau
- **Timeout** - Connexion ou lecture timeout (5s)
- **Connection Failed** - Impossible de contacter l'API

### Validation
- Latitude: -90 à 90
- Longitude: -180 à 180
- Champs obligatoires

## 📊 Logs

Les logs sont configurés pour afficher:
- Les appels API
- Les réponses reçues
- Les erreurs rencontrées
- Les validations

Exemple:
```
INFO  - Appel API Open-Meteo: https://api.open-meteo.com/v1/forecast?latitude=36.8065&longitude=10.1815&current_weather=true
INFO  - Données météo récupérées avec succès pour lat=36.8065, lon=10.1815
INFO  - Météo récupérée: 18.5°C, vent: 12.0 km/h, Ciel dégagé
```

## 🔗 Documentation API Open-Meteo

- Documentation officielle: https://open-meteo.com/en/docs
- Pas de clé API requise
- Limite: 10,000 appels/jour (gratuit)

## 🚦 Ordre de Démarrage

1. **MS_config** (port 9999)
2. **ms_eureka** (port 8761)
3. **Meteo** (port 8084)
4. **Arrosage** (port 8083)
5. **Gateway** (port 8080)

## ✅ Points Vérifiés

- ✅ RestTemplate configuré avec timeouts
- ✅ Gestion des erreurs HTTP
- ✅ Gestion des timeouts réseau
- ✅ URL configurée dans properties
- ✅ DTO pour parser le JSON
- ✅ Code documenté
- ✅ Validation des paramètres
- ✅ Logs informatifs
- ✅ Endpoints testables

## 🎯 Intégration avec Arrosage

Le service Arrosage peut maintenant utiliser la météo en temps réel:
1. Récupérer la météo d'une station via Feign Client
2. Ajuster automatiquement l'arrosage selon:
   - Température actuelle
   - Vitesse du vent
   - Conditions météo (pluie, etc.)

## 📝 Notes

- L'API Open-Meteo ne nécessite pas d'authentification
- Les données sont mises à jour toutes les heures
- Format de réponse: JSON
- Timeouts configurés à 5 secondes
