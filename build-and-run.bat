@echo off
echo ========================================
echo Compilando microservicios...
echo ========================================

echo.
echo [1/6] Compilando Eureka Server...
cd eureka-server
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo al compilar Eureka Server
    exit /b 1
)
cd ..

echo.
echo [2/6] Compilando API Gateway...
cd api-gateway
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo al compilar API Gateway
    exit /b 1
)
cd ..

echo.
echo [3/6] Compilando MS-Solicitudes-V2...
cd ms-solicitudes-v2
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo al compilar MS-Solicitudes-V2
    exit /b 1
)
cd ..

echo.
echo [4/6] Compilando MS-Logistica...
cd ms-logistica
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo al compilar MS-Logistica
    exit /b 1
)
cd ..

echo.
echo [5/6] Compilando MS-Facturacion-V2...
cd ms-facturacion-v2
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo al compilar MS-Facturacion-V2
    exit /b 1
)
cd ..

echo.
echo [6/6] Compilando MS-Tracking-V2...
cd ms-tracking-v2
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo al compilar MS-Tracking-V2
    exit /b 1
)
cd ..

echo.
echo ========================================
echo Compilacion completada exitosamente!
echo ========================================
echo.
echo Iniciando contenedores Docker...
echo ========================================

docker-compose up --build

echo.
echo ========================================
echo Proceso finalizado
echo ========================================
