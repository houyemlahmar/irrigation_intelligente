# 📢 Système de Notifications et Prévisions Météo

## 🎯 Nouvelles Fonctionnalités

### 1. Interface de Gestion des Prévisions Météo

Une interface complète pour créer, modifier et supprimer les prévisions météorologiques.

**Route:** `/previsions`

**Fonctionnalités:**
- ✅ Création de nouvelles prévisions avec formulaire complet
- ✅ Modification des prévisions existantes
- ✅ Suppression des prévisions
- ✅ Filtrage par station météo
- ✅ Affichage visuel avec icônes météo (☀️ 🌧️ ⛈️ 🔥)
- ✅ Alerte visuelle pour conditions critiques
- ✅ Tri par date (plus récentes en premier)

**Détection des Conditions Critiques:**
- Pluie > 10mm → Icône ⛈️ + Badge d'alerte
- Température > 35°C → Icône 🔥 + Badge d'alerte
- Bordure orange sur les cartes à conditions critiques

### 2. Système de Notifications en Temps Réel

Un système de notifications toast qui s'affiche en haut à droite de l'écran.

**Composant:** `NotificationPanelComponent`

**Types de notifications:**
- ✅ **Success** (Vert) - Opérations réussies
- ⚠️ **Warning** (Orange) - Conditions critiques détectées
- ❌ **Error** (Rouge) - Erreurs
- ℹ️ **Info** (Bleu) - Informations générales

**Caractéristiques:**
- Animation slide-in depuis la droite
- Auto-fermeture après 10 secondes
- Fermeture manuelle avec bouton ×
- Timestamp "Il y a X minutes"
- Badge de compteur dans le header
- Empilage vertical des notifications

### 3. Intégration RabbitMQ - Notifications Automatiques

**Événements déclenchés automatiquement:**

Lorsqu'une prévision est **créée** ou **modifiée** avec des conditions critiques:
- Pluie prévue > 10mm
- Température max > 35°C

**Flux d'événements:**
```
Prévision créée/modifiée
    ↓
MS_Meteo: isConditionCritique() = true
    ↓
RabbitMQ: Envoi ChangementConditionsEvent
    ↓
Arrosage: MeteoEventListener reçoit l'événement
    ↓
Ajustement automatique des programmes
    ↓
Frontend: Notification affichée
```

**Notification Frontend:**
```javascript
// Lors de la création/modification de prévision
⚠️ Conditions Critiques Détectées
Forte pluie prévue: 12.0mm - Les programmes d'arrosage seront ajustés automatiquement
```

## 🚀 Utilisation

### Créer une Prévision

1. Accéder à `/previsions`
2. Cliquer sur "Nouvelle Prévision"
3. Remplir le formulaire:
   - Station météo *
   - Date *
   - Température min/max
   - Pluie prévue (mm)
   - Vent (km/h)
   - Humidité (%)
4. Cliquer sur "Créer"

**Si conditions critiques:** Une notification orange s'affichera automatiquement !

### Tester les Notifications RabbitMQ

**Test 1 - Forte Pluie:**
```bash
POST http://localhost:9090/api/previsions/create
{
  "date": "2026-01-15",
  "temperatureMax": 28.0,
  "temperatureMin": 18.0,
  "pluiePrevue": 15.0,  # > 10mm = critique !
  "vent": 20.0,
  "humidite": 75.0,
  "station": { "id": 1 }
}
```

**Résultat attendu:**
- ✅ Prévision créée en base de données
- ✅ Événement RabbitMQ publié vers `meteo.exchange`
- ✅ Arrosage reçoit l'événement sur `meteo.queue`
- ✅ Programmes ajustés automatiquement
- ✅ Logs dans console Arrosage:
  ```
  📨 Événement météo reçu : Forte pluie prévue: 15.0 mm
  ✅ Programmes d'arrosage ajustés avec succès
  ```
- ✅ Notification dans l'interface frontend

**Test 2 - Température Extrême:**
```bash
POST http://localhost:9090/api/previsions/create
{
  "date": "2026-01-16",
  "temperatureMax": 38.0,  # > 35°C = critique !
  "temperatureMin": 22.0,
  "pluiePrevue": 0,
  "vent": 15.0,
  "humidite": 40.0,
  "station": { "id": 2 }
}
```

**Résultat attendu:**
- Même flux que Test 1
- Message: "Température élevée: 38.0°C"

## 📊 Interface Utilisateur

### Page Prévisions
- **Grid Layout** - Cartes responsive (min 380px)
- **Filtre** - Dropdown pour filtrer par station
- **Carte Prévision** - Header gradient violet, détails météo, actions
- **Modal** - Formulaire de création/modification
- **Empty State** - Message + bouton si aucune prévision

### Notifications Panel
- **Position** - Fixed, top-right (top: 80px, right: 20px)
- **Z-index** - 2000 (au-dessus du contenu)
- **Animation** - slideIn 0.3s ease-out
- **Stack** - Flex column, gap 1rem

### Header Badge
- **Badge Rouge** - Compteur de notifications non lues
- **Mise à jour** - Automatique via RxJS Observable

## 🔧 Architecture Technique

### Services

**PrevisionService** (`prevision.service.ts`)
```typescript
getAllPrevisions(): Observable<PrevisionMeteo[]>
getPrevisionById(id: number): Observable<PrevisionMeteo>
getPrevisionsByStation(stationId: number): Observable<PrevisionMeteo[]>
createPrevision(prevision: PrevisionMeteo): Observable<PrevisionMeteo>
updatePrevision(id: number, prevision: PrevisionMeteo): Observable<PrevisionMeteo>
deletePrevision(id: number): Observable<void>
```

**NotificationService** (`notification.service.ts`)
```typescript
getNotifications(): Observable<Notification[]>
addNotification(type, title, message): void
removeNotification(id: number): void
markAsRead(id: number): void
clearAll(): void
getUnreadCount(): number
```

### Composants

**PrevisionsComponent**
- Gestion CRUD complète
- Détection automatique des conditions critiques
- Notifications intégrées

**NotificationPanelComponent**
- Affichage toast en haut à droite
- Auto-destruction après 10s
- Intégration RxJS

**HeaderComponent**
- Badge de compteur mis à jour en temps réel
- Observable du NotificationService

## 🎨 Design

**Couleurs:**
- Success: #4CAF50 (Vert)
- Warning: #FF9800 (Orange)
- Error: #f44336 (Rouge)
- Info: #2196F3 (Bleu)

**Icônes Météo:**
- ⛈️ Pluie > 10mm
- 🌧️ Pluie > 0mm
- 🔥 Temp > 35°C
- ☀️ Temp > 30°C
- ⛅ Conditions normales

## 📝 Logs Backend

### MS_Meteo
```
=== Prévision créée avec succès ===
=== Conditions critiques détectées ===
Type: PLUIE_FORTE
Pluie prévue: 15.0 mm
=== Événement météo publié vers RabbitMQ ===
```

### Arrosage
```
📨 Événement météo reçu : Forte pluie prévue: 15.0 mm
   Station: null
   Date: 2026-01-15
   Température Max: 28.0°C
   Pluie prévue: 15.0 mm
✅ Programmes d'arrosage ajustés avec succès
```

## ✨ Améliorations Futures

- [ ] WebSocket pour notifications en temps réel sans polling
- [ ] Centre de notifications avec historique
- [ ] Filtres de notifications par type
- [ ] Sons de notification (optionnel)
- [ ] Notification push via Service Worker
- [ ] Export des prévisions en CSV/PDF
- [ ] Graphiques de tendances météo
- [ ] Prévisions à 7 jours
- [ ] Intégration API météo externe (Open-Meteo)
