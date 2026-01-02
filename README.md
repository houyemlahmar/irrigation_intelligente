🌱 Système d'Irrigation Intelligente - Architecture Microservices
📋 Description du Projet
Application web basée sur une architecture microservices pour la gestion intelligente de l'arrosage agricole. Le système utilise les prévisions météorologiques et les données météo en temps réel (API Open-Meteo) pour optimiser automatiquement les programmes d'arrosage.

Fonctionnalités principales :
Gestion des stations météo : Création et gestion des stations avec coordonnées GPS
Intégration Open-Meteo : Récupération de la météo en temps réel via API externe (sans clé API)
Gestion des prévisions météo : Création, consultation et mise à jour des données prévisionnelles
Planification automatique de l'arrosage : Ajustement intelligent basé sur les conditions météo actuelles et prévues
Communication synchrone : Via OpenFeign et RestTemplate entre microservices
Communication asynchrone : Via RabbitMQ pour les événements météo critiques
Configuration centralisée : Via Spring Cloud Config Server (GitHub)
Service Discovery : Via Netflix Eureka
API Gateway : Routage dynamique via Spring Cloud Gateway
🏗️ Architecture
                    ┌────────────────┐
                    │  Config Server │
                    │   (Port 9999)  │
                    │   GitHub Repo  │
                    └───────┬────────┘
                            │
┌─────────────┐     ┌───────┴─────┐      ┌──────────┐     ┌─────────────┐
│   Client    │────▶│   Gateway   │────▶│  Eureka  │     │  RabbitMQ   │
│  (Postman)  │     │ (Port 9090) │      │  (8761)  │     │   (5672)    │
└─────────────┘     └──────┬──────┘      └────┬─────┘     └──────┬──────┘
                           │                  │                  │
              ┌────────────┴─────────────┐    │                  │
              ▼                          ▼    │                  │
     ┌─────────────────┐      ┌──────────────────┐               │
     │     Meteo       │      │    Arrosage      │               │
     │   (Port 8084)   │      │   (Port 8083)    │               │
     │                 │      │                  │               │
     │ - Stations      │◀─────│ - Programmes     │              │
     │ - Prévisions    │ Feign│ - Journaux       │               │
     │ - Open-Meteo ☁️ │      │ - Parcelles      │              │
     └────────┬────────┘      └────────┬─────────┘               │
              │                        │                         │
              ├────────────────────────┴─────────────────────────┘
              │        RabbitMQ Event Bus (Asynchrone)
              │
              ▼
       ┌──────────────┐
       │ Open-Meteo   │
       │     API      │
       │ (External)   │
       └──────────────┘
       
🛠️ Technologies Utilisées
Backend
| Technologie | Version | Description | |-------------|---------|-------------| | Java | 17 | Langage de programmation | | Spring Boot | 4.0.1 | Framework principal | | Spring Cloud | 2025.1.0 | Microservices patterns | | Spring Cloud Netflix Eureka | - | Service Discovery | | Spring Cloud Config | - | Configuration centralisée (GitHub) | | Spring Cloud Gateway | - | API Gateway | | Spring Cloud OpenFeign | - | Communication synchrone inter-services | | Spring AMQP (RabbitMQ) | - | Messaging asynchrone (événements météo) | | RestTemplate | - | Client HTTP pour API externe (Open-Meteo) | | Spring Data JPA | - | Persistance des données | | MySQL | 8.0 | Base de données (production) | | Lombok | - | Réduction du boilerplate code | | Maven | 3.8+ | Build tool |

API Externe
| API | Description | |-----|-------------| | Open-Meteo | API météo gratuite, sans clé API requise | | URL | https://api.open-meteo.com/v1/forecast | | Features | Météo actuelle, prévisions, données historiques |

Message Broker
| Technologie | Port | Description | |-------------|------|-------------| | RabbitMQ | 5672 (AMQP) | Message broker pour événements asynchrones |

📁 Structure des Microservices
irrigation-intelligente/
├── .gitignore
├── README.md
├── backend/
│   ├── config-repo/              # 📝 Configuration centralisée (GitHub)
│   │   ├── README.md
│   │   ├── Arrosage.properties
│   │   ├── Gateway.properties
│   │   └── Meteo.properties
│   │
│   ├── ms-eureka/                # 🔍 Service Discovery
│   │   ├── src/
│   │   └── pom.xml
│   │
│   ├── MS-config/                # ⚙️ Configuration Server
│   │   ├── src/
│   │   └── pom.xml
│   │
│   ├── Gateway/                  # 🚪 API Gateway
│   │   ├── src/
│   │   └── pom.xml
│   │
│   ├── Meteo/            # 🌦️ Service Météo
│   │   ├── src/
│   │   │   ├── Controllers/
│   │   │   │   ├── StationMeteoController.java
│   │   │   │   ├── PrevisionMeteoController.java
│   │   │   │   └── OpenMeteoController.java    
│   │   │   ├── Services/
│   │   │   │   ├── StationMeteoService.java
│   │   │   │   ├── PrevisionMeteoService.java
│   │   │   │   └── OpenMeteoService.java       
│   │   │   ├── Clients/
│   │   │   │   └── OpenMeteoClient.java        
│   │   │   ├── Repositories/
│   │   │   ├── Entities/
│   │   │   ├── DTOs/
│   │   │   │   ├── OpenMeteoResponse.java      
│   │   │   │   ├── WeatherDTO.java             
│   │   │   │   └── ChangementConditionsEvent.java
│   │   │   └── Config/
│   │   │       ├── RabbitMQConfig.java         # 🐰 RabbitMQ
│   │   │       └── RestTemplateConfig.java     
│   │   ├── pom.xml
│   │   ├── OPEN_METEO_INTEGRATION.md          # ⭐ Documentation
│   │   └── GUIDE_TESTS_POSTMAN.md             # ⭐ Tests API
│   │
│   └── Arrosage/         # 💧 Service Arrosage
│       ├── src/
│       │   ├── Controllers/
│       │   │   ├── ProgArrosageController.java
│       │   │   └── JournalController.java
│       │   ├── Services/
│       │   │   ├── ProgArrosageService.java
│       │   │   ├── JournalService.java
│       │   │   └── MeteoEventConsumer.java     # 🐰 RabbitMQ Consumer
│       │   ├── Clients/
│       │   │   ├── MeteoClient.java            # Feign
│       │   │   └── WeatherClient.java           Feign
│       │   ├── Listeners/
│       │   │   └── MeteoEventListener.java     # 🐰 RabbitMQ Listener
│       │   ├── Repositories/
│       │   ├── Entities/
│       │   ├── DTOs/
│       │   └── Config/
│       │       └── RabbitMQConfig.java         # 🐰 RabbitMQ
│       └── pom.xml
│
├── frontend/                     # (À venir)
│
└── deployment/                   # (À venir)
    ├── docker/
    └── k8s/
        

📡 Communication entre Microservices
🔄 Communication Synchrone
1. OpenFeign (Inter-services)
Arrosage → Meteo : Récupération des prévisions météo

2. RestTemplate (API Externe)
Meteo → Open-Meteo API : Récupération météo temps réel

🐰 Communication Asynchrone (RabbitMQ)
Configuration RabbitMQ
Exchange : meteo.exchange (TopicExchange) Queue : meteo.changement.conditions Routing Key : meteo.conditions.#

Publisher (Meteo Service)
Publie des événements lors de conditions météo critiques 

Consumer (Arrosage Service)
Écoute et réagit aux événements météo 

Scénarios d'événements
| Condition | Déclencheur | Action Arrosage | |-----------|-------------|-----------------| | Forte pluie | pluie > 10mm | ❌ Annulation des programmes | | Température élevée | temp > 35°C | ⬆️ Augmentation de 50% | | Pluie modérée | 5mm < pluie < 10mm | ⬇️ Réduction de 70% | | Vent fort | vent > 30 km/h | ⬇️ Réduction de 20% |


📚 Documentation Complémentaire
OPEN_METEO_INTEGRATION.md - Documentation technique Open-Meteo
GUIDE_TESTS_POSTMAN.md - Guide complet tests Postman
config-repo/README.md - Documentation configuration centralisée

👥 Auteurs
Houyem LAHMAR  
Projet : Système d'Irrigation Intelligente
Année : 2026

⭐ N'oubliez pas de mettre une étoile au projet si vous le trouvez utile !