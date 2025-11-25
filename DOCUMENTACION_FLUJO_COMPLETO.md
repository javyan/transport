# 📋 DOCUMENTACIÓN COMPLETA DEL SISTEMA DE TRANSPORTE DE CONTENEDORES

## 📌 Índice
1. [Arquitectura del Sistema](#arquitectura-del-sistema)
2. [Flujo Completo de Negocio](#flujo-completo-de-negocio)
3. [Estados y Transiciones](#estados-y-transiciones)
4. [Entidades del Sistema](#entidades-del-sistema)
5. [Cálculos y Fórmulas](#cálculos-y-fórmulas)
6. [Integración entre Microservicios](#integración-entre-microservicios)
7. [Casos de Uso Detallados](#casos-de-uso-detallados)

---

## 🏗️ Arquitectura del Sistema

### Microservicios Implementados

```
┌─────────────────┐
│   API Gateway   │ :8080
│  (Entrada única)│
└────────┬────────┘
         │
    ┌────┴────┬─────────┬──────────┬──────────────┐
    │         │         │          │              │
┌───▼────┐ ┌─▼────┐ ┌─▼─────┐ ┌──▼────┐ ┌──────▼──────┐
│Eureka  │ │MS-   │ │MS-    │ │MS-    │ │MS-          │
│Server  │ │Solic.│ │Logíst.│ │Track. │ │Facturación  │
│:8761   │ │:8081 │ │:8082  │ │:8083  │ │:8084        │
└────────┘ └──────┘ └───────┘ └───────┘ └─────────────┘
                        │                      │
                   ┌────▼──────┐          ┌───▼────┐
                   │PostgreSQL │          │Google  │
                   │(Shared DB)│          │Maps API│
                   └───────────┘          └────────┘
```

### Responsabilidades por Microservicio

| Microservicio | Puerto | Responsabilidad |
|--------------|--------|-----------------|
| **ms-solicitudes-v2** | 8081 | Gestión de solicitudes, contenedores y clientes |
| **ms-logistica** | 8082 | Rutas, tramos, camiones, depósitos, transportistas |
| **ms-tracking-v2** | 8083 | Seguimiento de contenedores en tiempo real |
| **ms-facturacion-v2** | 8084 | Tarifas, facturas y estadías en depósitos |

---

## 🔄 Flujo Completo de Negocio

### 📊 Diagrama de Flujo General

```
┌─────────────────────────────────────────────────────────────────┐
│ FASE 1: CREACIÓN DE SOLICITUD                                  │
└─────────────────────────────────────────────────────────────────┘
  Cliente/Operador
       ↓
  POST /api/solicitudes
       ↓
  ┌─────────────────────────────────┐
  │ 1. Crear/Obtener Cliente        │
  │ 2. Crear Contenedor             │
  │ 3. Crear Solicitud (BORRADOR)   │
  └─────────────────────────────────┘
       ↓
  Estado: SOLICITUD=BORRADOR, CONTENEDOR=PENDIENTE

┌─────────────────────────────────────────────────────────────────┐
│ FASE 2: PLANIFICACIÓN DE RUTA                                  │
└─────────────────────────────────────────────────────────────────┘
  Operador
       ↓
  POST /api/rutas/calcular
       ↓
  ┌─────────────────────────────────┐
  │ 1. Consultar Google Maps API    │
  │ 2. Calcular distancias          │
  │ 3. Crear Tramos (ESTIMADO)      │
  │ 4. Calcular costo estimado      │
  │ 5. Guardar Ruta                 │
  └─────────────────────────────────┘
       ↓
  Estado: TRAMOS=ESTIMADO, SOLICITUD=BORRADOR

┌─────────────────────────────────────────────────────────────────┐
│ FASE 3: CONFIRMACIÓN                                            │
└─────────────────────────────────────────────────────────────────┘
  Operador
       ↓
  PATCH /api/solicitudes/{id}/confirmar
       ↓
  Estado: SOLICITUD=PLANIFICADA

┌─────────────────────────────────────────────────────────────────┐
│ FASE 4: ASIGNACIÓN DE RECURSOS                                 │
└─────────────────────────────────────────────────────────────────┘
  Operador (por cada tramo)
       ↓
  POST /api/tramos/{id}/asignar?camionId=X&transportistaId=Y
       ↓
  ┌─────────────────────────────────┐
  │ 1. Validar disponibilidad       │
  │ 2. Validar capacidades          │
  │ 3. Asignar recursos             │
  │ 4. Actualizar estados           │
  └─────────────────────────────────┘
       ↓
  Estado: TRAMO=ASIGNADO, CAMION=ASIGNADO, TRANSPORTISTA=EN_USO

┌─────────────────────────────────────────────────────────────────┐
│ FASE 5: EJECUCIÓN DEL TRANSPORTE                               │
└─────────────────────────────────────────────────────────────────┘
  
  ┌─── BUCLE POR CADA TRAMO ───┐
  │                             │
  │ Transportista               │
  │      ↓                      │
  │ PATCH /api/tramos/{id}/iniciar
  │      ↓                      │
  │ ┌─────────────────────────┐│
  │ │ 1. Validar estado       ││
  │ │ 2. Registrar inicio     ││
  │ │ 3. Si origen=DEPOSITO → ││
  │ │    Registrar SALIDA     ││
  │ │ 4. Cambiar estado       ││
  │ └─────────────────────────┘│
  │      ↓                      │
  │ Estado: TRAMO=INICIADO,    │
  │         CAMION=EN_USO,     │
  │         SOLICITUD=EN_TRANSITO
  │         (si es 1er tramo)  │
  │                             │
  │ ... Transporte físico ...  │
  │                             │
  │ Transportista               │
  │      ↓                      │
  │ PATCH /api/tramos/{id}/finalizar
  │      ↓                      │
  │ ┌─────────────────────────┐│
  │ │ 1. Registrar fin        ││
  │ │ 2. Si destino=DEPOSITO →││
  │ │    Registrar ENTRADA    ││
  │ │ 3. Liberar recursos     ││
  │ │ 4. Si último tramo →    ││
  │ │    Finalizar solicitud  ││
  │ │    Generar factura      ││
  │ └─────────────────────────┘│
  │      ↓                      │
  │ Estado: TRAMO=FINALIZADO,  │
  │         CAMION=DISPONIBLE, │
  │         TRANSPORTISTA=DISPONIBLE
  │                             │
  └─────────────────────────────┘
       ↓
  (Si todos los tramos finalizaron)
       ↓
  Estado: SOLICITUD=ENTREGADA, CONTENEDOR=ENTREGADO

┌─────────────────────────────────────────────────────────────────┐
│ FASE 6: FACTURACIÓN AUTOMÁTICA                                 │
└─────────────────────────────────────────────────────────────────┘
  Sistema (automático al finalizar último tramo)
       ↓
  POST /api/facturas/generar?solicitudId=X
       ↓
  ┌─────────────────────────────────┐
  │ 1. Obtener tramos finalizados   │
  │ 2. Consultar estadías           │
  │ 3. Calcular costos reales:      │
  │    - Gestión                    │
  │    - Transporte                 │
  │    - Combustible                │
  │    - Estadías                   │
  │ 4. Aplicar impuestos (21% IVA)  │
  │ 5. Generar número de factura    │
  └─────────────────────────────────┘
       ↓
  Estado: FACTURA=GENERADA
```

---

## 🔀 Estados y Transiciones

### 1. Estados de SOLICITUD

```
┌──────────┐  confirmar()   ┌────────────┐  iniciarTramo(1er)  ┌─────────────┐
│ BORRADOR │ ────────────→  │ PLANIFICADA│ ──────────────────→ │ EN_TRANSITO │
└──────────┘                └────────────┘                      └─────────────┘
                                                                        │
                                                   finalizarTramo(último)
                                                                        ↓
                                                                 ┌──────────┐
                                                                 │ENTREGADA │
                                                                 └──────────┘
```

**Transiciones:**
- **BORRADOR → PLANIFICADA**: `confirmarSolicitud(id)` - Requiere ruta asignada
- **PLANIFICADA → EN_TRANSITO**: `iniciarTramo(primerTramo)` - Automático al iniciar 1er tramo
- **EN_TRANSITO → ENTREGADA**: `finalizarTramo(ultimoTramo)` - Automático al finalizar todos los tramos

**Validaciones:**
- No se puede confirmar sin ruta calculada
- No se puede iniciar sin camión asignado
- No se puede finalizar si no están todos los tramos completados

---

### 2. Estados de CONTENEDOR

```
┌──────────┐  crearSolicitud()  ┌────────────┐  iniciarTramo()  ┌───────────┐
│PENDIENTE │ ─────────────────→ │ EN_ESPERA  │ ───────────────→ │EN_TRANSITO│
└──────────┘                    └────────────┘                   └───────────┘
                                                                        │
                                                  finalizarUltimoTramo()
                                                                        ↓
                                     ┌────────────┐  salirDeposito()  ┌──────────┐
                                     │EN_DEPOSITO │ ←───────────────  │ENTREGADO │
                                     └────────────┘  (si aplica)      └──────────┘
```

**Transiciones:**
- **PENDIENTE → EN_ESPERA**: Al crear solicitud
- **EN_ESPERA → EN_TRANSITO**: Al iniciar primer tramo
- **EN_TRANSITO → EN_DEPOSITO**: Al finalizar tramo en depósito
- **EN_DEPOSITO → EN_TRANSITO**: Al iniciar tramo desde depósito
- **EN_TRANSITO → ENTREGADO**: Al finalizar último tramo

---

### 3. Estados de TRAMO

```
┌──────────┐  crearTramos()  ┌──────────┐  asignarCamion()  ┌──────────┐
│ ESTIMADO │ ──────────────→ │ ESTIMADO │ ────────────────→ │ ASIGNADO │
└──────────┘                 └──────────┘                    └──────────┘
                                                                    │
                                                         iniciarTramo()
                                                                    ↓
┌────────────┐  finalizarTramo()  ┌──────────┐                          
│ FINALIZADO │ ←───────────────── │ INICIADO │                          
└────────────┘                    └──────────┘                          
```

**Transiciones:**
- **ESTIMADO → ASIGNADO**: `asignarCamion(tramoId, camionId, transportistaId)`
- **ASIGNADO → INICIADO**: `iniciarTramo(tramoId)` - Solo por transportista
- **INICIADO → FINALIZADO**: `finalizarTramo(tramoId)` - Solo por transportista

**Validaciones:**
- Solo ASIGNADO puede pasar a INICIADO
- Solo INICIADO puede pasar a FINALIZADO
- No se puede asignar si camión/transportista no disponible

---

### 4. Estados de CAMIÓN

```
┌────────────┐  asignarCamion()  ┌──────────┐  iniciarTramo()  ┌────────┐
│ DISPONIBLE │ ────────────────→ │ ASIGNADO │ ───────────────→ │ EN_USO │
└────────────┘                   └──────────┘                   └────────┘
      ↑                                                               │
      │                                              finalizarTramo() │
      └───────────────────────────────────────────────────────────────┘
```

**Transiciones:**
- **DISPONIBLE → ASIGNADO**: Al asignar a un tramo
- **ASIGNADO → EN_USO**: Al iniciar tramo
- **EN_USO → DISPONIBLE**: Al finalizar tramo (automático)

**Atributos Actualizados:**
- `ubicacionActual`: Se actualiza al finalizar tramo (= destino del tramo)

---

### 5. Estados de TRANSPORTISTA

```
┌────────────┐  asignarCamion()  ┌────────┐
│ DISPONIBLE │ ────────────────→ │ EN_USO │
└────────────┘                   └────────┘
      ↑                               │
      │          finalizarTramo()     │
      └───────────────────────────────┘
```

**Transiciones:**
- **DISPONIBLE → EN_USO**: Al asignar a un tramo
- **EN_USO → DISPONIBLE**: Al finalizar tramo (automático)

---

### 6. Estados de ESTADÍA

```
┌──────────┐  finalizarTramo(destino=DEPOSITO)  ┌──────────┐
│   N/A    │ ──────────────────────────────────→│ EN_CURSO │
└──────────┘                                     └──────────┘
                                                       │
                                    iniciarTramo(origen=DEPOSITO)
                                                       ↓
                                                 ┌────────────┐
                                                 │ FINALIZADA │
                                                 └────────────┘
```

**Transiciones:**
- **N/A → EN_CURSO**: Automático al finalizar tramo con destino=DEPOSITO
- **EN_CURSO → FINALIZADA**: Automático al iniciar tramo con origen=DEPOSITO

**Cálculo automático:**
```java
dias = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
if (dias < 1) dias = 1; // Mínimo 1 día
costoTotal = dias × costoDia;
```

---

### 7. Estados de FACTURA

```
┌─────┐  generarFactura()  ┌──────────┐
│ N/A │ ─────────────────→ │ GENERADA │
└─────┘                    └──────────┘
```

**Trigger:**
- Se genera automáticamente al finalizar el último tramo
- Estado único: `GENERADA`

---

## 🗂️ Entidades del Sistema

### 1. **Cliente** (ms-solicitudes-v2)

```java
Cliente {
    id: Long
    nombre: String
    apellido: String
    email: String (único)
    telefono: String
    direccion: String
    fechaRegistro: LocalDateTime
}
```

**Operaciones:**
- `POST /api/clientes` - Crear cliente
- `GET /api/clientes` - Listar clientes
- `GET /api/clientes/{id}` - Obtener cliente

---

### 2. **Contenedor** (ms-solicitudes-v2)

```java
Contenedor {
    id: Long
    codigoContenedor: String (único)
    pesoKg: Double
    volumenM3: Double
    estado: String // PENDIENTE, EN_ESPERA, EN_TRANSITO, EN_DEPOSITO, ENTREGADO
    clienteId: Long
    observaciones: String
    fechaCreacion: LocalDateTime
}
```

**Ciclo de vida:**
1. Creado con solicitud → `PENDIENTE`
2. Solicitud confirmada → `EN_ESPERA`
3. Primer tramo inicia → `EN_TRANSITO`
4. Tramo finaliza en depósito → `EN_DEPOSITO`
5. Tramo desde depósito inicia → `EN_TRANSITO`
6. Último tramo finaliza → `ENTREGADO`

---

### 3. **Solicitud** (ms-solicitudes-v2)

```java
Solicitud {
    id: Long
    clienteId: Long
    contenedorId: Long
    origenDireccion: String
    origenLat: Double
    origenLon: Double
    destinoDireccion: String
    destinoLat: Double
    destinoLon: Double
    estado: String // BORRADOR, PLANIFICADA, EN_TRANSITO, ENTREGADA
    costoEstimado: Double
    tiempoEstimadoHoras: Double
    costoReal: Double
    tiempoRealHoras: Double
    fechaSolicitud: LocalDateTime
    fechaEntrega: LocalDateTime
}
```

**Relaciones:**
- `1 Solicitud : 1 Contenedor`
- `1 Solicitud : 1 Cliente`
- `1 Solicitud : N Tramos`
- `1 Solicitud : 1 Factura`

---

### 4. **Ruta** (ms-logistica)

```java
Ruta {
    id: Long
    solicitudId: Long
    tipoRuta: String // DIRECTA, CON_DEPOSITOS
    cantidadTramos: Integer
    distanciaTotalKm: Double
    tiempoEstimadoHoras: Double
    costoEstimado: Double
    estado: String // TENTATIVA, ASIGNADA
    fechaCreacion: LocalDateTime
}
```

**Tipos de Ruta:**
- **DIRECTA**: Origen → Destino (1 tramo)
- **CON_DEPOSITOS**: Origen → Depósito(s) → Destino (N tramos)

---

### 5. **Tramo** (ms-logistica)

```java
Tramo {
    id: Long
    solicitudId: Long
    rutaId: Long
    camionId: Long
    transportistaId: Long
    origenTipo: String // CLIENTE, DEPOSITO
    origenId: Long
    origenDireccion: String
    destinoTipo: String // CLIENTE, DEPOSITO
    destinoId: Long
    destinoDireccion: String
    tipoTramo: String // ORIGEN_DESTINO, ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO
    distanciaKm: Double
    ordenTramo: Integer
    estado: String // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
    fechaInicio: LocalDateTime
    fechaFin: LocalDateTime
}
```

**Tipos de Tramo:**
- `ORIGEN_DESTINO`: Cliente → Cliente (ruta directa)
- `ORIGEN_DEPOSITO`: Cliente → Depósito
- `DEPOSITO_DEPOSITO`: Depósito → Depósito
- `DEPOSITO_DESTINO`: Depósito → Cliente

---

### 6. **Camión** (ms-logistica)

```java
Camion {
    id: Long
    patente: String (único)
    marca: String
    modelo: String
    capacidadKg: Double
    capacidadM3: Double
    consumoLtKm: Double
    costoKm: Double
    transportistaId: Long
    ubicacionActual: String
    estado: String // DISPONIBLE, ASIGNADO, EN_USO, MANTENIMIENTO
}
```

**Validaciones de Capacidad:**
```java
if (contenedor.pesoKg > camion.capacidadKg) {
    throw new IllegalStateException("Camión no soporta el peso");
}
if (contenedor.volumenM3 > camion.capacidadM3) {
    throw new IllegalStateException("Camión no soporta el volumen");
}
```

---

### 7. **Transportista** (ms-logistica)

```java
Transportista {
    id: Long
    nombre: String
    apellido: String
    dni: String (único)
    telefono: String
    licenciaConducir: String
    estado: String // DISPONIBLE, EN_USO, INACTIVO
    fechaRegistro: LocalDateTime
}
```

---

### 8. **Depósito** (ms-logistica)

```java
Deposito {
    id: Long
    nombre: String
    direccion: String
    lat: Double
    lon: Double
    capacidadMaximaM3: Double
    costoDia: Double // Costo de estadía por día
    estado: String // ACTIVO, INACTIVO
    telefono: String
}
```

**Uso:**
- Punto intermedio de almacenamiento
- Genera estadías cuando contenedor llega/sale

---

### 9. **EstadíaDepósito** (ms-facturacion-v2)

```java
EstadiaDeposito {
    id: Long
    contenedorId: Long
    depositoId: Long
    fechaEntrada: LocalDateTime
    fechaSalida: LocalDateTime
    diasEstadia: Integer
    costoDia: Double // Copia del costo del depósito al momento
    costoTotal: Double // dias × costoDia
    estado: String // EN_CURSO, FINALIZADA
    observaciones: String
}
```

**Registro Automático:**
- **ENTRADA**: Al finalizar tramo con `destinoTipo=DEPOSITO`
- **SALIDA**: Al iniciar tramo con `origenTipo=DEPOSITO`

---

### 10. **Tarifa** (ms-facturacion-v2)

```java
Tarifa {
    id: Long
    nombre: String
    cargoGestionBase: Double
    cargoGestionPorTramo: Double
    costoBaseKm: Double
    precioCombustibleLitro: Double
    estado: String // VIGENTE, HISTORICA
    fechaVigenciaDesde: LocalDate
    fechaVigenciaHasta: LocalDate
}
```

**Uso:**
- Solo 1 tarifa `VIGENTE` a la vez
- Se consulta al generar factura

---

### 11. **Factura** (ms-facturacion-v2)

```java
Factura {
    id: Long
    solicitudId: Long
    tarifaId: Long
    numeroFactura: String // Formato: FACT-YYYYMMDD-NNNN
    cargoGestion: Double
    costoTransporte: Double
    costoCombustible: Double
    costoEstadias: Double
    subtotal: Double
    impuestos: Double // 21% IVA
    total: Double
    estado: String // GENERADA
    fechaEmision: LocalDateTime
}
```

---

## 💰 Cálculos y Fórmulas

### 1. Cálculo de Distancias (Google Maps API)

```java
// Llamada a Google Maps Directions API
Request request = new Request.Builder()
    .url("https://maps.googleapis.com/maps/api/directions/json?" +
         "origin=" + origenLat + "," + origenLon +
         "&destination=" + destinoLat + "," + destinoLon +
         "&key=" + API_KEY)
    .build();

Response response = httpClient.newCall(request).execute();
JsonNode root = objectMapper.readTree(response.body().string());

// Extraer distancia en metros
int distanciaMetros = root.get("routes").get(0)
                          .get("legs").get(0)
                          .get("distance").get("value").asInt();

Double distanciaKm = distanciaMetros / 1000.0;
```

---

### 2. Cálculo de Tiempo Estimado

```java
// Velocidad promedio: 80 km/h
Double tiempoEstimadoHoras = distanciaKm / 80.0;
```

---

### 3. Cálculo de Costo Estimado (al crear ruta)

```java
Tarifa tarifa = obtenerTarifaVigente();

// 1. Cargo de Gestión
Double cargoGestion = tarifa.getCargoGestionBase() + 
                     (cantidadTramos × tarifa.getCargoGestionPorTramo());

// 2. Costo de Transporte (estimado con tarifa base)
Double costoTransporte = distanciaTotalKm × tarifa.getCostoBaseKm();

// 3. Costo de Combustible (estimado con consumo promedio)
Double consumoPromedio = 0.08; // L/km promedio
Double costoCombustible = distanciaTotalKm × consumoPromedio × 
                         tarifa.getPrecioCombustibleLitro();

// 4. Costo Estadías (estimado en 0 en fase de planificación)
Double costoEstadias = 0.0;

// TOTAL ESTIMADO
Double costoEstimado = cargoGestion + costoTransporte + costoCombustible;
```

---

### 4. Cálculo de Costo Real (al generar factura)

```java
Tarifa tarifa = obtenerTarifaVigente();
List<Tramo> tramos = obtenerTramosPorSolicitud(solicitudId);
Solicitud solicitud = obtenerSolicitud(solicitudId);

// 1. Cargo de Gestión (basado en cantidad real de tramos)
Double cargoGestion = tarifa.getCargoGestionBase() + 
                     (tramos.size() × tarifa.getCargoGestionPorTramo());

// 2. Costo de Transporte REAL (usando costo/km de cada camión)
Double costoTransporte = tramos.stream()
    .mapToDouble(tramo -> {
        Camion camion = obtenerCamion(tramo.getCamionId());
        return tramo.getDistanciaKm() × camion.getCostoKm();
    })
    .sum();

// 3. Costo de Combustible REAL (usando consumo de cada camión)
Double costoCombustible = tramos.stream()
    .mapToDouble(tramo -> {
        Camion camion = obtenerCamion(tramo.getCamionId());
        return tramo.getDistanciaKm() × 
               camion.getConsumoLtKm() × 
               tarifa.getPrecioCombustibleLitro();
    })
    .sum();

// 4. Costo de Estadías REAL (estadías finalizadas del contenedor)
Long contenedorId = solicitud.getContenedorId();
List<EstadiaDeposito> estadias = 
    estadiaDepositoRepository.findByContenedorIdAndEstado(contenedorId, "FINALIZADA");

Double costoEstadias = estadias.stream()
    .mapToDouble(EstadiaDeposito::getCostoTotal)
    .sum();

// SUBTOTAL
Double subtotal = cargoGestion + costoTransporte + costoCombustible + costoEstadias;

// IMPUESTOS (21% IVA)
Double impuestos = subtotal × 0.21;

// TOTAL REAL
Double total = subtotal + impuestos;
```

**Ejemplo Numérico:**
```
Cargo Gestión:      $50,000 (base) + 2 tramos × $10,000 = $70,000
Costo Transporte:   700km × $15,000/km = $10,500,000
Costo Combustible:  700km × 0.08L/km × $1,200/L = $67,200
Costo Estadías:     3 días × $50,000/día = $150,000
────────────────────────────────────────────────────────
Subtotal:           $10,787,200
Impuestos (21%):    $2,265,312
────────────────────────────────────────────────────────
TOTAL:              $13,052,512
```

---

### 5. Cálculo de Estadía

```java
// Al SALIR del depósito (iniciar tramo con origen=DEPOSITO)
LocalDateTime entrada = estadia.getFechaEntrada();
LocalDateTime salida = LocalDateTime.now();

long dias = ChronoUnit.DAYS.between(entrada, salida);
if (dias < 1) dias = 1; // Mínimo 1 día

Double costoTotal = dias × estadia.getCostoDia();

estadia.setFechaSalida(salida);
estadia.setDiasEstadia((int) dias);
estadia.setCostoTotal(costoTotal);
estadia.setEstado("FINALIZADA");
```

**Ejemplo:**
```
Entrada:     2025-11-20 14:30
Salida:      2025-11-23 09:15
Días:        3 días
Costo/día:   $50,000
────────────────────────────
Costo Total: $150,000
```

---

### 6. Cálculo de Tiempo Real

```java
// Al finalizar todos los tramos
Double tiempoRealHoras = tramos.stream()
    .mapToDouble(tramo -> {
        if (tramo.getFechaInicio() != null && tramo.getFechaFin() != null) {
            Duration duracion = Duration.between(
                tramo.getFechaInicio(), 
                tramo.getFechaFin()
            );
            return duracion.toMinutes() / 60.0;
        }
        return 0.0;
    })
    .sum();
```

---

## 🔗 Integración entre Microservicios

### Comunicación Síncrona (Feign Clients)

#### ms-logistica → ms-solicitudes-v2

```java
@FeignClient(name = "MS-SOLICITUDES-V2")
public interface SolicitudClient {
    @GetMapping("/api/solicitudes/{id}")
    SolicitudDTO obtenerSolicitud(@PathVariable Long id);
    
    @PatchMapping("/api/solicitudes/{id}/estado")
    void actualizarEstado(@PathVariable Long id, @RequestParam String estado);
    
    @PatchMapping("/api/solicitudes/{id}/finalizar")
    void finalizarSolicitud(@PathVariable Long id, 
                           @RequestParam Double costoReal,
                           @RequestParam Double tiempoReal);
}
```

**Casos de uso:**
- Obtener datos del contenedor al asignar camión
- Actualizar estado a `EN_TRANSITO` al iniciar primer tramo
- Finalizar solicitud al completar todos los tramos

---

#### ms-logistica → ms-facturacion-v2

```java
@FeignClient(name = "MS-FACTURACION-V2")
public interface FacturacionClient {
    @PostMapping("/api/facturas/generar")
    FacturaDTO generarFactura(@RequestParam Long solicitudId);
    
    @PostMapping("/api/estadias/registrar-entrada")
    EstadiaResponseDTO registrarEntradaDeposito(@RequestBody EstadiaRequestDTO request);
    
    @PostMapping("/api/estadias/{id}/registrar-salida")
    EstadiaResponseDTO registrarSalidaDeposito(@PathVariable Long id);
}
```

**Casos de uso:**
- Generar factura automáticamente al finalizar último tramo
- Registrar entrada a depósito al finalizar tramo
- Registrar salida de depósito al iniciar tramo

---

#### ms-facturacion-v2 → ms-logistica

```java
@FeignClient(name = "MS-LOGISTICA")
public interface LogisticaClient {
    @GetMapping("/api/tramos/solicitud/{solicitudId}")
    List<TramoDTO> obtenerTramosPorSolicitud(@PathVariable Long solicitudId);
}
```

**Casos de uso:**
- Obtener tramos finalizados para calcular costo real

---

#### ms-facturacion-v2 → ms-solicitudes-v2

```java
@FeignClient(name = "MS-SOLICITUDES-V2")
public interface SolicitudClient {
    @GetMapping("/api/solicitudes/{id}")
    SolicitudDTO obtenerSolicitud(@PathVariable Long id);
}
```

**Casos de uso:**
- Obtener contenedorId para buscar estadías

---

### Eventos Automáticos en el Sistema

| Evento | Trigger | Acción Automática |
|--------|---------|-------------------|
| **Iniciar Primer Tramo** | `iniciarTramo(id)` | Solicitud → `EN_TRANSITO` |
| **Finalizar Tramo en Depósito** | `finalizarTramo(id)` con `destinoTipo=DEPOSITO` | Crear Estadía con estado `EN_CURSO` |
| **Iniciar Tramo desde Depósito** | `iniciarTramo(id)` con `origenTipo=DEPOSITO` | Finalizar Estadía, calcular costo |
| **Finalizar Último Tramo** | `finalizarTramo(id)` | 1. Solicitud → `ENTREGADA` <br> 2. Contenedor → `ENTREGADO` <br> 3. Generar Factura |
| **Finalizar Tramo** | `finalizarTramo(id)` | Camión → `DISPONIBLE` <br> Transportista → `DISPONIBLE` |

---

## 📝 Casos de Uso Detallados

### Caso 1: Flujo Completo con Ruta Directa (sin depósitos)

```
PASO 1: Cliente crea solicitud
────────────────────────────────
POST /api/solicitudes
Body: {
  "clienteNombre": "Juan Pérez",
  "clienteEmail": "juan@example.com",
  "clienteTelefono": "+54911234567",
  "origenDireccion": "Av. Corrientes 1000, CABA",
  "origenLat": -34.603722,
  "origenLon": -58.381592,
  "destinoDireccion": "Ruta 9 km 200, Rosario",
  "destinoLat": -32.946568,
  "destinoLon": -60.638818,
  "pesoKg": 2500.0,
  "volumenM3": 15.0
}

Response: {
  "id": 1,
  "estado": "BORRADOR",
  "contenedorId": 1,
  "contenedor": {
    "id": 1,
    "codigoContenedor": "CONT-20251124-0001",
    "estado": "PENDIENTE"
  }
}


PASO 2: Operador calcula rutas tentativas
────────────────────────────────────────────
POST /api/rutas/calcular?solicitudId=1

Sistema:
  1. Consulta Google Maps API
  2. Calcula distancia: 700 km
  3. Crea 1 tramo (ruta directa):
     - Tipo: ORIGEN_DESTINO
     - Estado: ESTIMADO
  4. Calcula costos estimados

Response: {
  "rutas": [{
    "tipoRuta": "DIRECTA",
    "cantidadTramos": 1,
    "distanciaTotalKm": 700.0,
    "tiempoEstimadoHoras": 8.75,
    "costoEstimado": 9500000.0,
    "tramos": [{
      "id": 1,
      "origenDireccion": "Av. Corrientes 1000, CABA",
      "destinoDireccion": "Ruta 9 km 200, Rosario",
      "distanciaKm": 700.0,
      "estado": "ESTIMADO"
    }]
  }]
}


PASO 3: Operador confirma solicitud
────────────────────────────────────
PATCH /api/solicitudes/1/confirmar

Response: {
  "id": 1,
  "estado": "PLANIFICADA"
}


PASO 4: Operador asigna camión al tramo
────────────────────────────────────────
POST /api/tramos/1/asignar?camionId=5&transportistaId=3

Sistema:
  1. Valida disponibilidad de camión y transportista
  2. Valida capacidad del camión (2500kg <= 5000kg ✓, 15m3 <= 25m3 ✓)
  3. Actualiza estados:
     - Tramo: ESTIMADO → ASIGNADO
     - Camión: DISPONIBLE → ASIGNADO
     - Transportista: DISPONIBLE → EN_USO

Response: {
  "id": 1,
  "estado": "ASIGNADO",
  "camionId": 5,
  "transportistaId": 3
}


PASO 5: Transportista inicia viaje
───────────────────────────────────
PATCH /api/tramos/1/iniciar

Sistema:
  1. Valida estado (debe ser ASIGNADO) ✓
  2. Actualiza estados:
     - Tramo: ASIGNADO → INICIADO
     - Camión: ASIGNADO → EN_USO
     - Solicitud: PLANIFICADA → EN_TRANSITO (es el 1er tramo)
  3. Registra fechaInicio: 2025-11-24 10:00:00

Response: {
  "id": 1,
  "estado": "INICIADO",
  "fechaInicio": "2025-11-24T10:00:00"
}


PASO 6: Transportista finaliza viaje
─────────────────────────────────────
PATCH /api/tramos/1/finalizar

Sistema:
  1. Valida estado (debe ser INICIADO) ✓
  2. Registra fechaFin: 2025-11-24 19:30:00
  3. Actualiza estados:
     - Tramo: INICIADO → FINALIZADO
     - Camión: EN_USO → DISPONIBLE
     - Transportista: EN_USO → DISPONIBLE
  4. Verifica: ¿Es el último tramo? SÍ
  5. Finaliza solicitud:
     - Calcula costoReal: $10,287,200
     - Calcula tiempoReal: 9.5 horas
     - Solicitud: EN_TRANSITO → ENTREGADA
     - Contenedor: EN_TRANSITO → ENTREGADO
  6. Genera factura automáticamente:
     - numeroFactura: FACT-20251124-0001
     - total: $12,445,712

Response: {
  "id": 1,
  "estado": "FINALIZADO",
  "fechaFin": "2025-11-24T19:30:00"
}


PASO 7: Sistema verifica factura generada
──────────────────────────────────────────
GET /api/facturas/solicitud/1

Response: {
  "id": 1,
  "numeroFactura": "FACT-20251124-0001",
  "cargoGestion": 60000.0,
  "costoTransporte": 10500000.0,
  "costoCombustible": 67200.0,
  "costoEstadias": 0.0,
  "subtotal": 10627200.0,
  "impuestos": 2231712.0,
  "total": 12858912.0,
  "estado": "GENERADA"
}
```

---

### Caso 2: Flujo Completo con Depósito Intermedio

```
PASO 1-3: (Igual que Caso 1, hasta confirmar solicitud)
────────────────────────────────────────────────────────

PASO 4: Sistema calcula ruta con depósito intermedio
─────────────────────────────────────────────────────
POST /api/rutas/calcular?solicitudId=2

Sistema detecta distancia > 600km → agrega depósito

Response: {
  "rutas": [{
    "tipoRuta": "CON_DEPOSITOS",
    "cantidadTramos": 2,
    "tramos": [
      {
        "id": 2,
        "tipoTramo": "ORIGEN_DEPOSITO",
        "origenDireccion": "Av. Corrientes 1000, CABA",
        "destinoDireccion": "Depósito Central Rosario",
        "destinoTipo": "DEPOSITO",
        "destinoId": 1,
        "distanciaKm": 300.0,
        "ordenTramo": 1,
        "estado": "ESTIMADO"
      },
      {
        "id": 3,
        "tipoTramo": "DEPOSITO_DESTINO",
        "origenDireccion": "Depósito Central Rosario",
        "origenTipo": "DEPOSITO",
        "origenId": 1,
        "destinoDireccion": "Ruta 9 km 500, Córdoba",
        "distanciaKm": 400.0,
        "ordenTramo": 2,
        "estado": "ESTIMADO"
      }
    ]
  }]
}


PASO 5: Asignar y ejecutar TRAMO 1 (Origen → Depósito)
───────────────────────────────────────────────────────
POST /api/tramos/2/asignar?camionId=5&transportistaId=3
PATCH /api/tramos/2/iniciar
  → Solicitud: PLANIFICADA → EN_TRANSITO

... viaje ...

PATCH /api/tramos/2/finalizar

Sistema:
  1. Registra fechaFin: 2025-11-24 14:30:00
  2. Detecta: destinoTipo = DEPOSITO
  3. REGISTRA ENTRADA A DEPÓSITO AUTOMÁTICAMENTE:
     POST (interno) /api/estadias/registrar-entrada
     Body: {
       "contenedorId": 2,
       "depositoId": 1,
       "costoDia": 50000.0
     }
     
     Crea EstadiaDeposito:
       - estado: EN_CURSO
       - fechaEntrada: 2025-11-24 14:30:00
  
  4. Libera recursos:
     - Camión 5: EN_USO → DISPONIBLE
     - Transportista 3: EN_USO → DISPONIBLE
  
  5. Contenedor: EN_TRANSITO → EN_DEPOSITO

Response: {
  "id": 2,
  "estado": "FINALIZADO",
  "message": "✅ Estadía registrada con ID: 1 | Costo por día: $50000"
}


PASO 6: Asignar TRAMO 2 (Depósito → Destino)
─────────────────────────────────────────────
POST /api/tramos/3/asignar?camionId=7&transportistaId=4

... 3 días después ...


PASO 7: Ejecutar TRAMO 2
─────────────────────────
PATCH /api/tramos/3/iniciar

Sistema:
  1. Detecta: origenTipo = DEPOSITO
  2. REGISTRA SALIDA DE DEPÓSITO AUTOMÁTICAMENTE:
     POST (interno) /api/estadias/1/registrar-salida
     
     Actualiza EstadiaDeposito:
       - fechaSalida: 2025-11-27 09:00:00
       - diasEstadia: 3 días
       - costoTotal: 3 × $50,000 = $150,000
       - estado: EN_CURSO → FINALIZADA
  
  3. Contenedor: EN_DEPOSITO → EN_TRANSITO
  4. Camión 7: ASIGNADO → EN_USO

Response: {
  "id": 3,
  "estado": "INICIADO",
  "message": "✅ Estadía registrada: 3 días | Costo: $150000"
}

... viaje ...

PATCH /api/tramos/3/finalizar

Sistema:
  1. Registra fechaFin: 2025-11-27 15:30:00
  2. Verifica: ¿Todos los tramos finalizados? SÍ (tramo 2 y 3)
  3. Finaliza solicitud:
     - Solicitud: EN_TRANSITO → ENTREGADA
     - Contenedor: EN_TRANSITO → ENTREGADO
  4. GENERA FACTURA AUTOMÁTICAMENTE:
     
     Cálculo:
     - Cargo Gestión: $50,000 + 2 tramos × $10,000 = $70,000
     - Costo Transporte: 
       * Tramo 1: 300km × $15,000 = $4,500,000
       * Tramo 2: 400km × $14,000 = $5,600,000
       Total: $10,100,000
     - Costo Combustible:
       * Tramo 1: 300km × 0.08L/km × $1,200 = $28,800
       * Tramo 2: 400km × 0.09L/km × $1,200 = $43,200
       Total: $72,000
     - Costo Estadías: $150,000 ← ¡INCLUIDO!
     
     Subtotal: $10,392,000
     IVA (21%): $2,182,320
     TOTAL: $12,574,320

Response: {
  "id": 3,
  "estado": "FINALIZADO",
  "message": "✅ Factura generada: FACT-20251127-0002 | Total: $12574320"
}


PASO 8: Verificar factura con estadías
───────────────────────────────────────
GET /api/facturas/solicitud/2

Response: {
  "numeroFactura": "FACT-20251127-0002",
  "cargoGestion": 70000.0,
  "costoTransporte": 10100000.0,
  "costoCombustible": 72000.0,
  "costoEstadias": 150000.0,  ← ¡CALCULADO AUTOMÁTICAMENTE!
  "subtotal": 10392000.0,
  "impuestos": 2182320.0,
  "total": 12574320.0
}


PASO 9: Verificar estadía registrada
─────────────────────────────────────
GET /api/estadias/contenedor/2

Response: [{
  "id": 1,
  "contenedorId": 2,
  "depositoId": 1,
  "fechaEntrada": "2025-11-24T14:30:00",
  "fechaSalida": "2025-11-27T09:00:00",
  "diasEstadia": 3,
  "costoDia": 50000.0,
  "costoTotal": 150000.0,
  "estado": "FINALIZADA"
}]
```

---

## 🎯 Validaciones y Reglas de Negocio

### Validaciones al Asignar Camión

```java
✓ Camión debe existir
✓ Transportista debe existir
✓ Camión debe estar DISPONIBLE
✓ Transportista debe estar DISPONIBLE
✓ Camión debe soportar peso del contenedor
✓ Camión debe soportar volumen del contenedor
✓ Tramo debe estar en estado ESTIMADO
```

### Validaciones al Iniciar Tramo

```java
✓ Tramo debe existir
✓ Tramo debe tener camión asignado
✓ Tramo debe tener transportista asignado
✓ Tramo debe estar en estado ASIGNADO
```

### Validaciones al Finalizar Tramo

```java
✓ Tramo debe existir
✓ Tramo debe estar en estado INICIADO
```

### Validaciones al Confirmar Solicitud

```java
✓ Solicitud debe estar en estado BORRADOR
✓ Solicitud debe tener ruta calculada (costoEstimado > 0)
```

### Validaciones al Generar Factura

```java
✓ Solicitud debe existir
✓ Todos los tramos deben estar FINALIZADOS
✓ No debe existir factura previa para la solicitud
✓ Debe existir tarifa vigente
```

---

## 📊 Tracking y Seguimiento

### Endpoint de Tracking

```
GET /api/tracking/contenedor/{codigoContenedor}
```

**Response:**
```json
{
  "contenedor": {
    "codigo": "CONT-20251124-0001",
    "estado": "EN_TRANSITO",
    "pesoKg": 2500.0,
    "volumenM3": 15.0
  },
  "solicitud": {
    "id": 1,
    "estado": "EN_TRANSITO",
    "origenDireccion": "Av. Corrientes 1000, CABA",
    "destinoDireccion": "Ruta 9 km 200, Rosario",
    "costoEstimado": 9500000.0,
    "tiempoEstimadoHoras": 8.75
  },
  "tramoActual": {
    "id": 1,
    "estado": "INICIADO",
    "origenDireccion": "Av. Corrientes 1000, CABA",
    "destinoDireccion": "Ruta 9 km 200, Rosario",
    "distanciaKm": 700.0,
    "fechaInicio": "2025-11-24T10:00:00",
    "camion": {
      "patente": "AA123BB",
      "marca": "Mercedes-Benz",
      "modelo": "Actros 2651"
    },
    "transportista": {
      "nombre": "Carlos Rodríguez",
      "telefono": "+54911555666"
    }
  },
  "historialTramos": [
    {
      "ordenTramo": 1,
      "estado": "INICIADO",
      "fechaInicio": "2025-11-24T10:00:00",
      "fechaFin": null
    }
  ]
}
```

---

## 🔐 Seguridad y Roles

### Roles Implementados

| Rol | Permisos |
|-----|----------|
| **CLIENTE** | - Crear solicitudes<br>- Consultar sus solicitudes<br>- Ver tracking de sus contenedores |
| **OPERADOR** | - Ver todas las solicitudes<br>- Calcular rutas<br>- Asignar camiones<br>- Confirmar solicitudes<br>- Gestionar camiones, depósitos, tarifas |
| **TRANSPORTISTA** | - Ver sus tramos asignados<br>- Iniciar tramos<br>- Finalizar tramos |
| **ADMIN** | - Acceso completo<br>- Gestionar tarifas<br>- Ver facturas<br>- Reportes |

---

## 📈 Reportes y Consultas Útiles

### 1. Solicitudes por Estado

```
GET /api/solicitudes/estado/{estado}
```

Estados: `BORRADOR`, `PLANIFICADA`, `EN_TRANSITO`, `ENTREGADA`

---

### 2. Camiones Disponibles con Capacidad

```
GET /api/camiones/disponibles?pesoMin=2000&volumenMin=15
```

---

### 3. Tramos de un Transportista

```
GET /api/tramos/transportista/{transportistaId}
```

---

### 4. Contenedores en Depósito

```
GET /api/contenedores/estado/EN_DEPOSITO
```

---

### 5. Estadías de un Contenedor

```
GET /api/estadias/contenedor/{contenedorId}
```

---

### 6. Factura de una Solicitud

```
GET /api/facturas/solicitud/{solicitudId}
```

---

## 🚀 Ejemplo de Prueba Completa (Postman)

### Colección: Flujo End-to-End

```javascript
// 1. Crear Solicitud
POST {{baseUrl}}/api/solicitudes
// Guardar {{solicitudId}}

// 2. Calcular Rutas
POST {{baseUrl}}/api/rutas/calcular?solicitudId={{solicitudId}}
// Guardar {{tramoId}}

// 3. Confirmar Solicitud
PATCH {{baseUrl}}/api/solicitudes/{{solicitudId}}/confirmar

// 4. Listar Camiones Disponibles
GET {{baseUrl}}/api/camiones/disponibles?pesoMin=2500&volumenMin=15
// Seleccionar {{camionId}}

// 5. Listar Transportistas Disponibles
GET {{baseUrl}}/api/transportistas/disponibles
// Seleccionar {{transportistaId}}

// 6. Asignar Camión al Tramo
POST {{baseUrl}}/api/tramos/{{tramoId}}/asignar?camionId={{camionId}}&transportistaId={{transportistaId}}

// 7. Iniciar Tramo
PATCH {{baseUrl}}/api/tramos/{{tramoId}}/iniciar

// 8. Verificar Tracking
GET {{baseUrl}}/api/tracking/contenedor/CONT-20251124-0001

// 9. Finalizar Tramo
PATCH {{baseUrl}}/api/tramos/{{tramoId}}/finalizar

// 10. Verificar Factura Generada
GET {{baseUrl}}/api/facturas/solicitud/{{solicitudId}}
```

---

## 📦 Resumen de Endpoints por Microservicio

### MS-SOLICITUDES-V2 (Puerto 8081)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/solicitudes` | Crear solicitud |
| GET | `/api/solicitudes` | Listar solicitudes |
| GET | `/api/solicitudes/{id}` | Obtener solicitud |
| PATCH | `/api/solicitudes/{id}/confirmar` | Confirmar solicitud |
| GET | `/api/solicitudes/estado/{estado}` | Filtrar por estado |
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/contenedores` | Listar contenedores |

---

### MS-LOGISTICA (Puerto 8082)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/rutas/calcular` | Calcular rutas tentativas |
| POST | `/api/tramos/{id}/asignar` | Asignar camión/transportista |
| PATCH | `/api/tramos/{id}/iniciar` | Iniciar tramo |
| PATCH | `/api/tramos/{id}/finalizar` | Finalizar tramo |
| GET | `/api/tramos/solicitud/{id}` | Listar tramos de solicitud |
| GET | `/api/camiones` | Listar camiones |
| GET | `/api/camiones/disponibles` | Camiones disponibles |
| POST | `/api/depositos` | Crear depósito |
| GET | `/api/transportistas` | Listar transportistas |

---

### MS-FACTURACION-V2 (Puerto 8084)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/facturas/generar` | Generar factura |
| GET | `/api/facturas` | Listar facturas |
| GET | `/api/facturas/solicitud/{id}` | Obtener factura por solicitud |
| GET | `/api/tarifas/vigente` | Obtener tarifa vigente |
| POST | `/api/tarifas` | Crear tarifa |
| GET | `/api/estadias` | Listar estadías |
| GET | `/api/estadias/contenedor/{id}` | Estadías de contenedor |

---

### MS-TRACKING-V2 (Puerto 8083)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/tracking/contenedor/{codigo}` | Tracking completo |
| GET | `/api/tracking/solicitud/{id}` | Tracking por solicitud |

---

## 🎓 Conclusión

Este documento describe la **lógica de negocio completa** del Sistema de Transporte de Contenedores, incluyendo:

✅ **Arquitectura de microservicios** con responsabilidades claras  
✅ **Flujo completo** desde creación hasta facturación  
✅ **Máquina de estados** de cada entidad (Solicitud, Tramo, Camión, etc.)  
✅ **Cálculos detallados** de costos, distancias y estadías  
✅ **Integración automática** entre microservicios vía Feign  
✅ **Eventos automáticos** (estadías, facturación, estados)  
✅ **Validaciones** y reglas de negocio  
✅ **Casos de uso completos** con ejemplos reales  

El sistema implementa un flujo robusto que automatiza:
- 🔄 Transiciones de estado
- 📊 Registro de estadías en depósitos
- 💰 Generación de facturas con costos reales
- 🚚 Liberación automática de recursos
- 📍 Tracking en tiempo real

---

**Fecha:** 24 de Noviembre de 2025  
**Versión:** 2.0  
**Sistema:** Backend de Transporte de Contenedores - TPI 2025
