@echo off
set GITHUB_USER=houyemlahmar

echo ============================================
echo Tagging and Pushing to GitHub Container Registry
echo ============================================

echo Tagging images...
docker tag irrigasmart-eureka:latest ghcr.io/%GITHUB_USER%/irrigasmart-eureka:latest
docker tag irrigasmart-config:latest ghcr.io/%GITHUB_USER%/irrigasmart-config:latest
docker tag irrigasmart-gateway:latest ghcr.io/%GITHUB_USER%/irrigasmart-gateway:latest
docker tag irrigasmart-meteo:latest ghcr.io/%GITHUB_USER%/irrigasmart-meteo:latest
docker tag irrigasmart-arrosage:latest ghcr.io/%GITHUB_USER%/irrigasmart-arrosage:latest
docker tag irrigasmart-frontend:latest ghcr.io/%GITHUB_USER%/irrigasmart-frontend:latest

echo.
echo Pushing images...
docker push ghcr.io/%GITHUB_USER%/irrigasmart-eureka:latest
docker push ghcr.io/%GITHUB_USER%/irrigasmart-config:latest
docker push ghcr.io/%GITHUB_USER%/irrigasmart-gateway:latest
docker push ghcr.io/%GITHUB_USER%/irrigasmart-meteo:latest
docker push ghcr.io/%GITHUB_USER%/irrigasmart-arrosage:latest
docker push ghcr.io/%GITHUB_USER%/irrigasmart-frontend:latest

echo.
echo ============================================
echo All images pushed successfully!
echo ============================================