# Dockerfile pour Gateway (Spring Cloud Gateway)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Port exposé
EXPOSE 9090

# Démarrage de l'application
ENTRYPOINT ["java","-jar","app.jar"]
