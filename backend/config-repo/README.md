# Configuration Repository

Ce dossier contient les fichiers de configuration centralisée pour tous les microservices.

## 📋 Fichiers de configuration

| Fichier | Service | Description |
|---------|---------|-------------|
| `Arrosage.properties` | arrosage-service | Configuration du service d'arrosage |
| `Gateway.properties` | gateway-service | Configuration de l'API Gateway |
| `Meteo.properties` | meteo-service | Configuration du service météo |

## 🔧 Utilisation

Ces fichiers sont lus par le **Config Server** (ms-config) au démarrage de chaque service.

### Ordre de démarrage
1. ms-config (port 9999) ← lit ces fichiers
2. ms-eureka (port 8761)
3. Autres services (lisent leur config depuis ms-config)

## 🔐 Sécurité

⚠️ **Ne jamais commiter de secrets** (mots de passe, clés API, tokens)
- Utilisez des variables d'environnement
- Ou créez des fichiers `-secret.properties` (exclus par .gitignore)
