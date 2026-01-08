# Dockerfile pour ms_eureka (Service Discovery)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Port exposé
EXPOSE 8761

# Démarrage de l'application
ENTRYPOINT ["java","-jar","app.jar"]
