# =============================================================================
# SCRIPT DE INICIO DEL SISTEMA DE TRANSPORTES
# =============================================================================
# Este script reinicia todo el sistema y carga automáticamente los datos
# de prueba necesarios para ejecutar la collection de Postman.
#
# Uso: .\inicio-sistema.ps1
# =============================================================================

Write-Host "`n╔══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  🚀 INICIANDO SISTEMA DE TRANSPORTES TPI                 ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Paso 1: Detener servicios actuales
Write-Host "🛑 Deteniendo servicios actuales..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null

# Paso 2: Limpiar base de datos anterior
Write-Host "🗑️  Limpiando base de datos anterior..." -ForegroundColor Yellow
Remove-Item -Recurse -Force ./pgdata -ErrorAction SilentlyContinue

# Paso 3: Iniciar servicios de infraestructura
Write-Host "🔧 Iniciando servicios de infraestructura..." -ForegroundColor Yellow
docker-compose up -d 2>&1 | Out-Null

# Paso 4: Esperar a que Postgres esté listo
Write-Host "⏳ Esperando a PostgreSQL (15s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Paso 5: Iniciar microservicios
Write-Host "🚀 Iniciando microservicios..." -ForegroundColor Yellow
docker start postgres 2>&1 | Out-Null
Start-Sleep -Seconds 5
docker start ms-solicitudes-v2 ms-logistica ms-facturacion-v2 ms-tracking-v2 api-gateway 2>&1 | Out-Null

# Paso 6: Esperar inicialización completa
Write-Host "⏳ Esperando inicialización completa (50s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 50

# Verificar estado
Write-Host "`n📊 Estado de servicios:" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}" | Select-String -Pattern "ms-|api-gateway|eureka|postgres|keycloak" | ForEach-Object { Write-Host "  $_" -ForegroundColor Green }

# Verificar carga de datos
Write-Host "`n🗄️  Verificando carga automática de datos:" -ForegroundColor Cyan
$env:PGPASSWORD='password'
$datos = docker exec postgres psql -U user -d tpi_db -t -c "SELECT 'Clientes: ' || COUNT(*) FROM v2_clientes UNION ALL SELECT 'Contenedores: ' || COUNT(*) FROM v2_contenedores UNION ALL SELECT 'Solicitudes: ' || COUNT(*) FROM v2_solicitudes UNION ALL SELECT 'Transportistas: ' || COUNT(*) FROM v2_transportistas UNION ALL SELECT 'Camiones: ' || COUNT(*) FROM v2_camiones UNION ALL SELECT 'Depósitos: ' || COUNT(*) FROM v2_depositos UNION ALL SELECT 'Tarifas: ' || COUNT(*) FROM v2_tarifas;"
$datos | ForEach-Object { if($_ -match "\d+") { Write-Host "  $_" -ForegroundColor Green } }

Write-Host "`n╔══════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✅ SISTEMA INICIADO CORRECTAMENTE                       ║" -ForegroundColor Green
Write-Host "║                                                          ║" -ForegroundColor Green
Write-Host "║  🌐 Endpoints disponibles:                               ║" -ForegroundColor Green
Write-Host "║    • API Gateway:    http://localhost:8080               ║" -ForegroundColor Green
Write-Host "║    • Eureka Server:  http://localhost:8761               ║" -ForegroundColor Green
Write-Host "║    • Keycloak Admin: http://localhost:9090               ║" -ForegroundColor Green
Write-Host "║    • PgAdmin:        http://localhost:5050               ║" -ForegroundColor Green
Write-Host "║                                                          ║" -ForegroundColor Green
Write-Host "║  🔑 Credenciales Keycloak:                               ║" -ForegroundColor Green
Write-Host "║    • Usuario: admin  /  Contraseña: admin123            ║" -ForegroundColor Green
Write-Host "║                                                          ║" -ForegroundColor Green
Write-Host "║  📝 Colección Postman: Sistema_Transportes_TPI.json     ║" -ForegroundColor Green
Write-Host "║    • 91 requests organizados en 6 carpetas               ║" -ForegroundColor Green
Write-Host "║    • OAuth2 configurado automáticamente                  ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════╝" -ForegroundColor Green
