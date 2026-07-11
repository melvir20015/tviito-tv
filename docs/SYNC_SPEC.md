# Cloud Sync Specification

## Objetivo

Sincronizar opcionalmente perfiles y guardados entre varios Android TV, Google TV o Fire TV sin impedir el uso sin Internet.

```text
Cuenta
└── Hogar
    ├── Dispositivos autorizados
    ├── Perfiles
    ├── Favoritos
    ├── Historial y progreso
    ├── Feedback
    ├── Preferencias
    ├── Reglas parentales
    ├── Ajustes EPG
    └── Correcciones manuales de metadatos
```

## Experiencia de vinculación

En el primer dispositivo, el usuario activa la sincronización e inicia sesión. En el segundo:

1. Selecciona **Vincular dispositivo**.
2. La TV muestra un QR y un código temporal.
3. El usuario lo aprueba desde un dispositivo autorizado o una página segura.
4. El nuevo dispositivo descarga perfiles y guardados.

El código debe ser de un solo uso, expirar rápidamente, tener rate limiting y no autorizar nada hasta ser aprobado.

## Datos que se sincronizan en el MVP

- Perfiles, nombres y avatares.
- Proveedores permitidos por perfil mediante IDs estables.
- Favoritos.
- Historial.
- Progreso de películas, series y episodios.
- Me gusta, no me gusta y no me interesa.
- Preferencias y reglas parentales.
- Zona horaria de presentación.
- Ajustes EPG manuales confirmados.
- Correcciones manuales de metadatos.
- Dispositivos autorizados y revocados.

## Datos que se regeneran localmente

- Pósteres y backdrops.
- Caché de imágenes.
- Catálogo completo del proveedor.
- Filas completas del EPG.
- Respuestas crudas de TMDB.
- Buffers, temporales y logs.
- Grabaciones locales.

## Credenciales IPTV

### MVP recomendado

No sincronizar usuario, contraseña, tokens ni URLs privadas. Cada dispositivo solicita esas credenciales una vez.

### Fase avanzada

Agregar una opción separada de sincronización cifrada de extremo a extremo:

- Cifrar antes de subir.
- El servidor nunca ve secretos en texto plano.
- Clave maestra generada en el dispositivo.
- Recuperación mediante frase o clave independiente.
- Revocación de dispositivos.
- No reutilizar el PIN parental como clave criptográfica.

## Arquitectura offline-first

Room sigue siendo la fuente inmediata de la interfaz:

```text
UI
 ↓
Room
 ↓
Sync Outbox
 ↓
WorkManager
 ↓
CloudSyncGateway
 ↓
Backend
```

Descarga:

```text
Backend change feed
 ↓
Cursor incremental
 ↓
Validación y resolución de conflictos
 ↓
Transacción Room
 ↓
UI observa Flow
```

La aplicación nunca bloquea una acción local esperando al servidor.

## Tablas locales

```text
SyncAccount
- accountId
- householdId
- lastServerCursor
- lastSyncAt

RegisteredDevice
- deviceId
- displayName
- publicKey
- createdAt
- lastSeenAt
- revokedAt

SyncOutbox
- changeId
- entityType
- entityId
- operation
- localRevision
- encryptedPayload
- createdAt
- attemptCount

SyncState
- entityType
- entityId
- serverRevision
- dirty
- lastSyncedAt

SyncTombstone
- entityType
- entityId
- deletedAt
- expiresAt
```

Cada `changeId` debe ser UUID y cada operación idempotente.

## Modelo de servidor

```text
accounts
households
household_members
devices
profiles
profile_providers
favorites
watch_progress
watch_events
user_feedback
profile_preferences
parental_rules
epg_time_rules
metadata_overrides
changes
```

Toda fila sincronizable incluye `household_id`, `entity_id`, `revision`, `updated_at` del servidor y `updated_by_device_id`.

## Protocolo

### Push

1. Leer un lote del outbox.
2. Enviar cambios con `changeId`.
3. Validar cuenta, hogar y dispositivo.
4. Aplicar de forma idempotente.
5. Devolver revisión de servidor.
6. Confirmar y retirar el cambio local.

### Pull

1. Enviar el último cursor.
2. Descargar solo cambios posteriores.
3. Validar el esquema.
4. Resolver conflictos.
5. Aplicar en una transacción Room.
6. Guardar el nuevo cursor.

La primera sincronización usa un snapshot paginado; las siguientes son incrementales.

## Conflictos

### Perfiles y preferencias

Última modificación válida por campo, usando revisión y hora del servidor.

### Favoritos

Conjunto con operaciones `ADD` y `REMOVE`. Las eliminaciones usan tombstones para que un dispositivo atrasado no reactive un favorito eliminado.

### Feedback

La acción explícita más reciente gana: Like, Dislike, Not interested o Clear.

### Progreso

No utilizar simplemente la posición máxima:

1. Dentro de la misma sesión, conservar el progreso más avanzado.
2. Una acción explícita de reiniciar crea una sesión nueva y puede tener una posición menor.
3. `completed=true` se conserva hasta que exista una nueva sesión explícita.
4. En conflicto simultáneo gana la revisión aceptada más recientemente por el servidor.

### Correcciones manuales

`userLocked=true` tiene prioridad sobre enriquecimiento automático o inferencias EPG.

## Recomendaciones

Sincronizar las señales fuente:

- Eventos de reproducción.
- Progreso.
- Favoritos.
- Feedback.
- Correcciones manuales.

Cada dispositivo regenera recomendaciones localmente. No es necesario sincronizar cada carrusel recomendado.

## Background sync

Trabajos únicos:

```text
sync_push_<accountId>
sync_pull_<accountId>
sync_periodic_<accountId>
```

Disparadores:

- Inicio de la aplicación.
- Cambio local importante.
- Recuperación de conectividad.
- Inicio de sesión.
- Vinculación de dispositivo.
- Ejecución periódica razonable.

Requisitos:

- Reintento exponencial.
- Sin trabajos duplicados.
- No afectar reproducción.
- Botón **Sincronizar ahora**.
- Mostrar última sincronización y cambios pendientes.

## Seguridad

- Autenticación obligatoria.
- Autorización por `householdId`.
- Tokens protegidos mediante Android Keystore.
- HTTPS.
- Rate limiting.
- Device ID aleatorio.
- Revocación y cierre remoto.
- No confiar en timestamps del cliente.
- No usar `ANDROID_ID` como secreto.
- PIN parental nunca en texto plano.
- Logs sanitizados.

## Backend intercambiable

```kotlin
interface CloudSyncGateway {
    suspend fun push(changes: List<OutgoingChange>): PushResult
    suspend fun pull(cursor: String?): PullResult
    suspend fun registerDevice(request: DeviceRegistration): RegisteredDevice
    suspend fun revokeDevice(deviceId: String)
}
```

Implementaciones posibles:

- Firebase Authentication + Firestore/Functions.
- Supabase Auth + Postgres/Edge Functions.
- Backend propio con API y base SQL.

Room y el dominio no deben depender directamente de un proveedor cloud.

## Orden de implementación

1. Modo local completo.
2. Contrato `CloudSyncGateway`.
3. Cuenta, hogar y dispositivos.
4. Sincronización de perfiles.
5. Favoritos y feedback.
6. Progreso e historial.
7. Preferencias, controles parentales y ajustes EPG.
8. Credenciales E2EE solo después de una revisión de seguridad.

## Criterios de aceptación

- Dos dispositivos muestran los mismos perfiles.
- Un favorito agregado offline aparece al reconectar.
- El progreso no retrocede por una sincronización vieja.
- Reiniciar una película funciona aunque exista un progreso terminado.
- Una eliminación no reaparece.
- Un perfil infantil conserva restricciones.
- Un dispositivo revocado deja de sincronizar.
- La aplicación continúa funcionando sin conexión.
- Las operaciones son idempotentes.
- No se sincronizan credenciales IPTV en el MVP.
