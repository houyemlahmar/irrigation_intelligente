# 🌐 Événements RabbitMQ pour Planification Temps Réel

## 📋 Vue d'ensemble

Le système déclenche maintenant des événements RabbitMQ **automatiquement** lors de la planification temps réel, en plus des événements déclenchés lors de la création/modification de prévisions.

## 🔄 Flux Complets

### 1️⃣ Planification Automatique (Base de Données)
```
Frontend: Click "Planifier" (auto)
    ↓
Backend Arrosage: planifierArrosageAutomatique()
    ↓
OpenFeign → MS_Meteo: getPrevisionsByStationAndDate()
    ↓
Ajustement programme selon PrevisionDTO
    ↓
Programme sauvegardé en BDD
```

**Note:** Les événements RabbitMQ sont déclenchés **uniquement** lors de la création/modification de prévisions avec conditions critiques (> 10mm pluie OU > 35°C temp).

### 2️⃣ Planification Temps Réel (Open-Meteo API) ✨ NOUVEAU
```
Frontend: Click "Planifier" (temps réel)
    ↓
Backend Arrosage: planifierArrosageAvecMeteoTempsReel()
    ↓
HTTP REST → Open-Meteo API: getCurrentWeather(lat, lon)
    ↓
Ajustement programme selon WeatherDTO
    ↓
✨ NOUVEAU: verifierEtPublierConditionsCritiquesTempsReel()
    ↓
Si critique: RabbitMQ Event → meteo.exchange
    ↓
Arrosage: MeteoEventListener reçoit l'événement
    ↓
Frontend: Notification toast affichée
```

## ⚠️ Conditions Critiques (Temps Réel)

### Détection Automatique

**Forte Pluie:**
- Weather codes 51-67 (pluie)
- Weather codes 80-82 (averses)
- **Action:** Programme annulé (durée=0, volume=0)
- **Event Type:** `PLUIE_FORTE`

**Température Extrême:**
- Température > 35°C
- **Action:** Programme augmenté (+50% : 45min, 750L)
- **Event Type:** `TEMPERATURE_EXTREME`

### Structure de l'Événement

```java
ChangementConditionsEvent {
    date: LocalDate.now()
    temperatureMax: temperature actuelle
    temperatureMin: temperature actuelle
    pluiePrevue: 15.0 (si pluie) ou 0.0
    vent: windSpeed
    typeChangement: "PLUIE_FORTE" ou "TEMPERATURE_EXTREME"
    message: "Forte pluie détectée..." ou "Température extrême..."
}
```

## 📱 Notifications Frontend

### Types de Notifications

**1. Pluie Forte Détectée ⚠️**
```typescript
Type: warning (orange)
Titre: "⚠️ Conditions Météo Critiques"
Message: "Forte pluie détectée en temps réel - Le programme d'arrosage a été annulé automatiquement"
```

**2. Température Élevée 🔥**
```typescript
Type: warning (orange)
Titre: "🔥 Température Élevée Détectée"
Message: "Température élevée en temps réel - Programme ajusté : 45 min, 750L"
```

**3. Conditions Normales ✅**
```typescript
Type: success (vert)
Titre: "✅ Programme Planifié"
Message: "Programme créé avec succès : 30 min, 500L"
```

## 🧪 Tests

### Test 1 - Planification Temps Réel avec Pluie

**Scénario:** Planifier un programme dans une zone avec pluie active

1. Accéder à `/programmes`
2. Cliquer sur "Planifier Programme"
3. Sélectionner "Temps Réel"
4. Entrer coordonnées GPS d'une zone pluvieuse
   - Exemple: Londres (51.5074, -0.1278)
5. Cliquer sur "Planifier"

**Résultat attendu:**
- ✅ Programme créé avec statut `ANNULE`
- ✅ Durée = 0 min, Volume = 0L
- ✅ Notification orange: "⚠️ Conditions Météo Critiques"
- ✅ Log backend: "Forte pluie détectée (code: XX)"
- ✅ Log backend: "=== Événement météo temps réel publié vers RabbitMQ ==="
- ✅ Log backend: "📨 Événement météo reçu"

### Test 2 - Planification Temps Réel avec Chaleur

**Scénario:** Planifier un programme dans une zone très chaude

1. Accéder à `/programmes`
2. Cliquer sur "Planifier Programme"
3. Sélectionner "Temps Réel"
4. Entrer coordonnées GPS d'une zone chaude
   - Exemple: Dubaï (25.2048, 55.2708) en été
   - Ou simuler avec mock data
5. Cliquer sur "Planifier"

**Résultat attendu:**
- ✅ Programme créé avec statut `PLANIFIE`
- ✅ Durée = 45 min, Volume = 750L
- ✅ Notification orange: "🔥 Température Élevée Détectée"
- ✅ Log backend: "Température élevée (38°C), augmentation de l'arrosage de 50%"
- ✅ Log backend: "=== Événement météo temps réel publié vers RabbitMQ ==="

### Test 3 - Planification Temps Réel Normale

**Scénario:** Planifier un programme dans une zone avec météo normale

1. Utiliser coordonnées avec conditions normales
   - Exemple: Paris (48.8566, 2.3522)
2. Cliquer sur "Planifier"

**Résultat attendu:**
- ✅ Programme créé avec statut `PLANIFIE`
- ✅ Durée = 30 min, Volume = 500L
- ✅ Notification verte: "✅ Programme Planifié"
- ✅ **AUCUN** événement RabbitMQ publié (conditions normales)

## 🔧 Configuration Backend

### ProgArrosageService.java

**Nouvelle méthode:**
```java
private void verifierEtPublierConditionsCritiquesTempsReel(WeatherDTO weather)
```

**Injection:**
```java
@Autowired
private RabbitTemplate rabbitTemplate;
```

**Publication:**
```java
rabbitTemplate.convertAndSend(
    "meteo.exchange", 
    "meteo.conditions.changement", 
    event
);
```

## 🎨 Configuration Frontend

### programmes.component.ts

**Nouvelle méthode:**
```typescript
verifierConditionsCritiquesTempsReel(programme: ProgrammeArrosage)
```

**Service injecté:**
```typescript
constructor(
    private notificationService: NotificationService
)
```

**Appel après planification:**
```typescript
planifierArrosageTempsReel(...).subscribe({
    next: (programme) => {
        this.verifierConditionsCritiquesTempsReel(programme);
    }
})
```

## 📊 Comparaison des Systèmes

| Aspect | Planification Auto | Planification Temps Réel |
|--------|-------------------|--------------------------|
| **Source données** | PrevisionMeteo (BDD) | Open-Meteo API |
| **Communication** | OpenFeign (Sync) | HTTP REST |
| **RabbitMQ** | Création/Modification prévisions | ✨ Maintenant aussi ! |
| **Déclencheur** | Conditions critiques en BDD | Conditions critiques en temps réel |
| **Notification** | Backend → Frontend | ✨ Backend → Frontend |

## ✨ Avantages

1. **Cohérence:** Les deux modes de planification déclenchent des notifications
2. **Visibilité:** L'utilisateur est toujours informé des conditions critiques
3. **Traçabilité:** Événements RabbitMQ loggés dans les deux microservices
4. **Flexibilité:** Possibilité d'ajouter des listeners supplémentaires

## 🚀 Prochaines Étapes

- [ ] Ajouter un historique des événements RabbitMQ
- [ ] Créer un dashboard de monitoring des conditions critiques
- [ ] Implémenter des alertes par email/SMS
- [ ] Ajouter des statistiques sur les ajustements automatiques
- [ ] WebSocket pour notifications en temps réel sans polling
