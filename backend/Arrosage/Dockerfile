# Dockerfile pour MS_Arrosage (Microservice Métier)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Port exposé
EXPOSE 8083

# Démarrage de l'application
ENTRYPOINT ["java","-jar","app.jar"]
