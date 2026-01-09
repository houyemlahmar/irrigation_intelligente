# 🌱 Système d'Irrigation Intelligente - IrrigaSmart

Application web basée sur une architecture microservices pour la gestion automatisée de l'arrosage agricole avec optimisation météorologique en temps réel.

## 🚀 Fonctionnalités Principales

### ⚙️ Gestion des Programmes d'Arrosage
- **Planification Automatique** : Utilise les prévisions météo stockées en base de données
- **Planification Temps Réel** : Intégration API Open-Meteo pour données météo actuelles
- **Exécution en temps réel** : Compteur dégressif avec barre de progression
- **Persistance** : Restauration automatique des états après rafraîchissement
- **Termination automatique** : Fin de programme détectée côté serveur et client

### 🌤️ Système Météorologique
- **Stations Météo** : CRUD complet avec sélection GPS sur carte interactive (Leaflet)
- **Prévisions** : Interface complète pour gérer les prévisions avec détection de conditions critiques
- **Open-Meteo API** : Données météo en temps réel basées sur coordonnées GPS
- **Indicateurs visuels** : Icônes météo dynamiques (☀️ 🌧️ ⛈️ 🔥 ⛅)

### 🔔 Notifications Intelligentes
- **Centre de notifications** : Dropdown dans le header avec historique complet
- **Toast temporaires** : Notifications flottantes avec auto-fermeture
- **Badge de compteur** : Indicateur visuel des notifications non lues
- **Détection automatique** : Alertes lors de conditions critiques (pluie >10mm, température >35°C)
- **Types multiples** : Success ✅, Warning ⚠️, Error ❌, Info ℹ️

### 🗺️ Sélection GPS Interactive
- **Cartes Leaflet** : Intégration OpenStreetMap pour sélection de coordonnées
- **Click-to-select** : Placement de marqueur par clic sur la carte
- **Utilisé dans** : Stations météo et planification temps réel

### 📡 Communication Asynchrone (RabbitMQ)
- **Événements automatiques** : Déclenchés lors de conditions critiques détectées
- **Planification auto** : Événements lors de création/modification de prévisions critiques
- **Planification temps réel** : Événements lors de détection de conditions critiques via API
- **Ajustement automatique** : Modification des programmes existants en réponse aux événements

## 🏗️ Architecture Technique

### Microservices
- **ms_eureka** (8761) : Service Discovery
- **MS_config** (9999) : Configuration centralisée
- **Gateway** (9090) : API Gateway avec CORS
- **MS_Meteo** (8084) : Stations, prévisions, RabbitMQ publisher
- **Arrosage** (8083) : Programmes, OpenFeign client, RabbitMQ consumer

### Technologies
- **Backend** : Spring Boot 3.5.7, Java 17, MySQL, RabbitMQ (CloudAMQP)
- **Frontend** : Angular 19, TypeScript, Leaflet, RxJS
- **Communication** : OpenFeign (sync), RabbitMQ (async), REST API

### Microservice Météo
- Gestion des stations météo (CRUD)
- Gestion des prévisions météo
- Intégration API Open-Meteo (météo temps réel)
- Publication d'événements RabbitMQ lors de conditions critiques

### Microservice Arrosage
- Planification automatique selon les prévisions
- Ajustement intelligent du volume d'eau :
  - Pluie > 10mm → Annulation
  - Température > 35°C → +50% volume
  - Vent > 30km/h → -20% volume
- Journal d'exécution des arrosages

## 🔄 Communication

| Type | Technologie | Usage |
|------|-------------|-------|

## 🚀 Installation & Démarrage

### Prérequis
- Java 17, Maven
- Node.js 18+, npm
- MySQL (port 3307)
- RabbitMQ (CloudAMQP configuré)

### Démarrage Backend
```bash
# Ordre de lancement
1. cd backend/ms_eureka && mvnw spring-boot:run
2. cd backend/MS_config && mvnw spring-boot:run  
3. cd backend/Gateway && mvnw spring-boot:run
4. cd backend/MS_Meteo && mvnw spring-boot:run
5. cd backend/Arrosage && mvnw spring-boot:run
```

### Démarrage Frontend
```bash
cd Frontend
npm install
npm start
# http://localhost:4200
```

## 📡 API Endpoints

### Stations & Prévisions (via Gateway :9090)
```
GET/POST  /api/stations/**
GET/POST  /api/previsions/**
GET       /api/weather/current?latitude=X&longitude=Y
```

### Programmes d'Arrosage
```
GET   /api/programmes
POST  /api/programmes/planifier-auto
POST  /api/programmes/planifier-temps-reel
POST  /api/programmes/{id}/demarrer
POST  /api/programmes/{id}/terminer
```

## 📚 Documentation Complémentaire

- [NOTIFICATIONS_GUIDE.md](NOTIFICATIONS_GUIDE.md) - Système de notifications et prévisions
- [RABBITMQ_TEMPS_REEL.md](RABBITMQ_TEMPS_REEL.md) - Événements RabbitMQ temps réel

## 👥 Auteur
- Houyem Lahmar - Ingénieur génie logiciel
Développé dans le cadre d'un projet académique de gestion intelligente de l'irrigation agricole.
