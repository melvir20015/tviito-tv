# Architecture

## 1. Estrategia

Usar el fork nativo como monolito modular inicialmente. La prioridad es obtener límites claros de dominio, no multiplicar módulos Gradle prematuramente.

## 2. Capas

```text
Presentation
- Compose TV
- ViewModels
- UI state

Domain
- Models
- Use cases
- Repository contracts
- Scoring policies

Data
- Room
- Provider APIs
- XMLTV
- TMDB adapter
- WorkManager
- Preferences/Keystore

Platform
- Media3
- Time zone database
- Android lifecycle
- Storage
```

## 3. Componentes

### ProfileRepository

Responsable de:

- CRUD de perfiles.
- Perfil activo.
- Proveedores autorizados.
- Preferencias.
- Migración de datos previos a perfiles.

### ProviderRepository

Responsable de:

- Proveedores.
- Credenciales.
- Sincronización de catálogo.
- Disponibilidad.
- Resolución de streams.

### EpgRepository

Responsable de:

- Descarga.
- Parseo.
- Conversión a Instant.
- Persistencia.
- Reglas de zona.
- Offset manual.
- Consultas now/next y timeline.

### MetadataRepository

Responsable de:

- Normalización.
- Identidad de contenido.
- Adaptadores externos.
- Caché.
- Confianza.
- Revisión manual.

### RecommendationRepository

Responsable de:

- Perfil de afinidad.
- Candidatos.
- Scoring.
- Diversidad.
- Exclusiones.
- Explicaciones.
- Persistencia/caché.

### PlaybackCoordinator

Responsable de:

- Instancia y ciclo de vida del player.
- Resolver URL.
- Tracks.
- Progreso.
- Eventos de reproducción.
- Fallback.
- Métricas técnicas.

## 4. EPG

### Tipos canónicos

```kotlin
data class ParsedEpgTimestamp(
    val localDateTime: LocalDateTime,
    val explicitOffset: ZoneOffset?,
    val originalText: String
)

data class EpgTimeResolution(
    val instant: Instant,
    val sourceZoneId: ZoneId?,
    val appliedManualOffset: Duration,
    val resolutionSource: ResolutionSource,
    val confidence: Double?
)
```

### Prioridad de resolución

1. Offset explícito en XMLTV.
2. Regla manual bloqueada del canal.
3. Zona IANA del canal.
4. Regla manual bloqueada del proveedor.
5. Zona IANA del proveedor.
6. Inferencia de alta confianza.
7. Estado sin resolver.

El offset manual adicional se aplica después de resolver la hora base.

### Presentación

```kotlin
fun Instant.toDisplayTime(zoneId: ZoneId): ZonedDateTime =
    atZone(zoneId)
```

No persistir texto de hora local como fuente canónica.

### Pruebas DST

Incluir fixtures para:

- `America/Los_Angeles`.
- `America/New_York`.
- `Europe/Madrid`.
- `America/Tegucigalpa`, sin DST.
- Zona con offset de media hora si se admite.

## 5. Metadata

### Normalización

Pipeline:

```text
Unicode normalize
→ separators to spaces
→ detect year
→ detect season/episode
→ extract quality
→ extract language
→ remove release tags
→ trim
→ title case only for display, not matching
```

No eliminar números legítimos de títulos.

### Identidad

```text
ContentIdentity
    1 ───── N ProviderContent
    1 ───── 1..N MetadataRecord
```

Una identidad representa la obra; `ProviderContent` representa una fuente reproducible.

### Matching

Puntuación sugerida:

```text
55 title similarity
20 year
10 content type
 5 runtime
 5 language/country context
 5 popularity sanity
```

Aplicar penalizaciones fuertes por:

- Tipo incorrecto.
- Año muy distante.
- Serie vs película.
- Temporada/episodio incompatible.

### Background jobs

Cadena:

```text
CatalogSyncWorker
→ IdentityLinkWorker
→ MetadataEnrichmentWorker
→ RecommendationRefreshWorker
```

Usar unique work por proveedor y políticas para no duplicar ejecuciones.

## 6. Recomendaciones

### Perfil de afinidad

Guardar scores agregados por:

- Género.
- Persona.
- Keyword.
- Década.
- Idioma.
- Tipo.
- Duración.

Las señales deben decaer con el tiempo de forma configurable.

### Scoring

```text
candidateScore =
    contentAffinity
  + qualityScore
  + sessionIntent
  + novelty
  + diversityBoost
  - negativeSignals
  - watchedPenalty
```

### Calidad ponderada

Usar una media bayesiana u otra aproximación que reduzca títulos con pocos votos:

```text
weighted = (v / (v + m)) * R + (m / (v + m)) * C
```

- `R`: puntuación del título.
- `v`: cantidad de votos.
- `m`: mínimo de votos.
- `C`: promedio global del catálogo.

### Diversidad

Después del ranking inicial aplicar re-ranking para evitar:

- Mismo actor repetido excesivamente.
- Mismo género en toda la fila.
- Misma saga ocupando toda la pantalla.

### Explicación

Guardar códigos:

```text
BECAUSE_WATCHED
ACTOR_AFFINITY
GENRE_AFFINITY
DIRECTOR_AFFINITY
HIGH_QUALITY
SAME_COLLECTION
CONTINUE_SERIES
REWATCH
DISCOVERY
```

## 7. Seguridad

### Credenciales

- Modelo guarda referencia cifrada, no contraseña plana.
- Keystore genera/protege la clave.
- DAO no expone credencial a UI.
- Logs usan `ProviderSafeSummary`.

### PIN

- Salt individual.
- KDF apropiada.
- Rate limit.
- Bloqueo temporal.
- Nunca guardar PIN reversible.

### Telemetría

Fase inicial:

```text
No network telemetry
Local structured logs only
Manual export with redaction
```

Más adelante:

- Consentimiento.
- Endpoint propio.
- Token rotatable.
- Sin URLs o títulos sensibles.
- Opt-out.

## 8. Migración a perfiles

Al introducir perfiles en una base que ya tiene favoritos/historial:

1. Crear `Profile principal`.
2. Crear perfil antes de migrar relaciones.
3. Asociar datos existentes solo a ese perfil.
4. Validar conteos.
5. Confirmar migración.
6. No duplicar datos en todos los perfiles.

## 9. Rendimiento

- Room Paging.
- Índices compuestos.
- Inserts en lotes.
- Images: caché y tamaños.
- EPG: consultas por ventana temporal.
- Recomendaciones precalculadas cuando dispositivo está inactivo.
- No recalcular catálogo completo en cada apertura.
- No mantener miles de objetos Compose activos.
- Profile switch debe invalidar solo datos dependientes del perfil.

## 10. Observabilidad local

Eventos técnicos sanitizados:

```text
AppStart
ProviderSyncStarted/Completed/Failed
EpgParseSummary
EpgTimeResolutionSummary
PlaybackStarted/FirstFrame/Failed
MetadataBatchSummary
RecommendationRefreshSummary
RoomMigrationResult
```

No incluir credenciales, títulos sensibles o URLs completas.

## 11. Decisiones abiertas

- Application ID final.
- Nombre comercial final.
- API mínima.
- Soporte comercial de TMDB.
- Backend opcional.
- Fire TV como objetivo oficial.
- Grabación de live TV.
- Timeshift.
- Cast.
- Distribución Play Store vs sideload.

## 12. Cloud Sync

Componentes:

```text
SyncCoordinator
OutboxRepository
ChangeApplier
ConflictResolver
DeviceRepository
CloudSyncGateway
SyncWorker
```

Room sigue siendo el origen inmediato de UI. El backend es una réplica sincronizada.

```kotlin
interface CloudSyncGateway {
    suspend fun push(changes: List<OutgoingChange>): PushResult
    suspend fun pull(cursor: String?): PullResult
    suspend fun registerDevice(request: DeviceRegistration): RegisteredDevice
    suspend fun revokeDevice(deviceId: String)
}
```

Requisitos:

- Autorización por hogar.
- Tokens protegidos.
- Dispositivos revocables.
- Operaciones idempotentes.
- Hora del servidor para orden definitivo.
- E2EE antes de sincronizar credenciales.

Ver `docs/SYNC_SPEC.md`.
