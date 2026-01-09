@echo off
echo ============================================
echo Building Docker Images for IrrigaSmart
echo ============================================

echo.
echo [1/6] Building Eureka...
docker build -t irrigasmart-eureka:latest ./backend/ms_eureka

echo.
echo [2/6] Building Config Server...
docker build -t irrigasmart-config:latest ./backend/MS_config

echo.
echo [3/6] Building Gateway...
docker build -t irrigasmart-gateway:latest ./backend/Gateway

echo.
echo [4/6] Building MS Meteo...
docker build -t irrigasmart-meteo:latest ./backend/MS_Meteo

echo.
echo [5/6] Building MS Arrosage...
docker build -t irrigasmart-arrosage:latest ./backend/Arrosage

echo.
echo [6/6] Building Frontend...
docker build -t irrigasmart-frontend:latest ./Frontend

echo.
echo ============================================
echo All images built successfully!
echo ============================================
docker images | findstr irrigasmart