# Dockerfile pour MS_config (Config Server)
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="microservices-team"
LABEL description="Spring Cloud Config Server"

WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Port exposé
EXPOSE 9999

# Variables d'environnement
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV EUREKA_SERVER_URL=http://ms-eureka:8761/eureka

# Démarrage de l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
