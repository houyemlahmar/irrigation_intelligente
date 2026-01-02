# Dockerfile pour ms_eureka (Service Discovery)
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="microservices-team"
LABEL description="Eureka Server - Service Discovery"

WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Port exposé
EXPOSE 8761

# Variables d'environnement pour la configuration
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Démarrage de l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
