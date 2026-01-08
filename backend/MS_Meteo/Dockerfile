# Dockerfile pour MS_Meteo (Microservice Métier)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Port exposé
EXPOSE 8084

# Démarrage de l'application
ENTRYPOINT ["java","-jar","app.jar"]
