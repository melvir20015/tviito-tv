# Baseline Audit — Fase 0

Fecha: 2026-07-11.

## Alcance

Auditoría inicial de la base Android nativa activa. No se implementaron perfiles, TMDB, recomendaciones, rediseño ni sincronización cloud nueva.

## Implementación activa

La implementación Android nativa activa está en `android-native/`. Existen otras bases (`web/`, `android-app/`, `electron/` y `cloudflare-config/`), pero la app objetivo de Android TV usa Gradle/Kotlin en `android-native/settings.gradle.kts` con el módulo `:app`.

## Versiones reales auditadas

| Elemento | Versión/configuración |
| --- | --- |
| Gradle wrapper | 8.12 (`gradle-8.12-bin.zip`) |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.12.01 |
| Compose for TV | `tv-foundation` 1.0.0, `tv-material` 1.0.0 |
| Media3 | 1.5.0 |
| Room | 2.6.1 |
| Hilt | 2.52 |
| WorkManager | 2.10.0 |
| minSdk | 28 |
| targetSdk | 35 |
| compileSdk | 35 |
| JDK requerido por Gradle | 17 configurado en `compileOptions`/`kotlinOptions`; el contenedor tenía Java 21 por defecto y JDK 17 instalado vía mise |

## Inventario del código actual

### Room

Entidades principales: `ProviderEntity`, `ChannelEntity`, `CategoryEntity`, `MovieEntity`, `SeriesEntity`, `EpisodeEntity`, `FavoriteEntity`, `WatchHistoryEntity`, `RecordingEntity`, `EpgEntity` y `ReminderEntity`.

DAOs: `ProviderDao`, `ChannelDao`, `MovieDao`, `SeriesDao`, `EpisodeDao`, `CategoryDao`, `FavoriteDao`, `WatchHistoryDao`, `RecordingDao`, `EpgDao` y `ReminderDao`.

Base de datos: `UltraDb`, versión 10. No hay migraciones explícitas registradas para la versión actual; existe `fallbackToDestructiveMigrationFrom(1..9)` para esquemas anteriores sin exportación histórica.

### Repositorios y fuentes de datos

Repositorios/clases de datos principales: `ProviderRepository`, `CatalogRepository`, `ChannelRepository`, `HistoryRepository`, `BackupRepository`, `RecordingRepository`, `PlaybackContext`, `LivePlaybackQueue`, `Catchup`, `LocalLogos`, `SyncStatusBus`, `RemoteConfigImporter`, `XtreamClient`, `StalkerClient`, `M3uParser` y `XmltvParser`.

### Workers

- `SyncWorker`: sincronización periódica/local de proveedores según preferencias.
- `RecordingWorker`: grabación HLS programada.

### Navegación y pantallas

Rutas declaradas: `home`, `live`, `movies`, `movies/{id}`, `series`, `series/{id}`, `search`, `guide`, `favorites`, `settings`, `player?url={url}&title={title}`, además de rutas literales `categories`, `locked-channels` y `recordings` en el grafo.

Pantallas principales: Home, Live, Movies, MovieDetail, Series, SeriesDetail, Search, GuideGrid, Guide, Categories, Favorites, Settings, Player, Recordings y LockedChannels.

### Playback

Playback se concentra en `PlayerScreen`, `PlaybackContext`, `LivePlaybackQueue`, componentes de drawer de live TV y dependencias Media3/ExoPlayer con HLS, DASH, RTMP, sesión, UI y Cast. También existe fallback a reproductor externo vía preferencias.

## Hallazgos de seguridad y conexiones remotas

### Antes del cambio

- Telemetría remota (`RemoteLog`) enviaba eventos, crashes y ANR a un Cloudflare Worker hardcodeado por defecto.
- Existía un token de crash/log hardcodeado como valor por defecto en Gradle.
- La preferencia `telemetryEnabled` era `true` por defecto.
- `MainActivity` consultaba automáticamente GitHub Releases del repositorio upstream `khalilbenaz/ultra-tv` al arrancar y podía descargar/instalar `UltraTV-debug.apk`.
- `SettingsViewModel` ofrecía un Worker Cloudflare upstream como URL por defecto para sincronización/configuración por MAC.
- `RemoteConfigImporter` permite importar configuración desde una URL introducida por el usuario; esta funcionalidad se conserva, pero no debe incluir una URL por defecto del upstream.

### Cambio seguro aplicado

- Telemetría desactivada por defecto: `UserPrefs.telemetryEnabled=false` y `RemoteLog.telemetryEnabled=false`.
- `RemoteLog` no envía nada si `LOG_URL` o `LOG_TOKEN` están vacíos.
- Gradle ya no incluye URL/token upstream por defecto; solo permite opt-in con propiedades/variables de entorno propias.
- Auto-update queda desactivado por defecto y sin repositorio/APK configurado; `UpdateChecker` retorna `null` si no hay opt-in explícito.
- `MainActivity` no inicia el flujo de actualización si `UpdateChecker` no está configurado.
- La URL Cloudflare de configuración remota queda vacía por defecto; el usuario debe pegar su Worker propio.
- Se añadió prueba de humo JVM para validar defaults seguros de telemetría y actualización.

## Comandos ejecutados y resultados reales

| Comando | Resultado |
| --- | --- |
| `find .. -name AGENTS.md -print` | Encontró `../tviito-tv/AGENTS.md`. |
| `find .. -name codex.md -print` | No encontró `codex.md` en el workspace auditado. |
| `sed -n ... README.md AGENTS.md docs/...` | Lectura de documentación base completada. |
| `find . -maxdepth 2 -type f` y `find . -maxdepth 4 ... build.gradle...` | Confirmó `android-native/` como implementación nativa activa. |
| `java -version` | Java 21 por defecto en el contenedor. |
| `mise ls java` | JDK 17 disponible en `/root/.local/share/mise/installs/java/17.0.2`. |
| `JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew test --no-daemon` | Falló antes de compilar por ausencia de SDK Android: `SDK location not found`. |
| `JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew lint --no-daemon` | Falló antes de compilar por ausencia de SDK Android: `SDK location not found`. |
| `JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew assembleDebug --no-daemon` | Falló antes de compilar por ausencia de SDK Android: `SDK location not found`. |

## Estado de build/test/lint/assemble

No se pudo completar `test`, `lint` ni `assembleDebug` porque el contenedor no tenía `ANDROID_HOME`/`ANDROID_SDK_ROOT` ni `android-native/local.properties` apuntando a un SDK Android válido. La validación pendiente debe ejecutarse en una máquina con Android SDK instalado y JDK 17:

```bash
cd android-native
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew test
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew lint
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew assembleDebug
```

## Diagrama simple de arquitectura actual

```text
Compose TV UI / Navigation
  → ViewModels
  → Repositories / importers / playback context
  → Room DAOs + provider clients + XMLTV/M3U parsers + WorkManager
  → Media3/ExoPlayer, OkHttp, DataStore, Coil, Cast SDK
```

## Riesgos y deuda técnica

- Package/applicationId aún conserva identidad `com.ultratv.tv.nativeapp`; corresponde a Fase 1.
- Room usa fallback destructivo desde versiones 1..9; cualquier versión futura requiere migración explícita.
- Las credenciales de proveedores todavía aparecen como campos de entidades/importación; se debe auditar cifrado/Keystore en Fase 1/3.
- `DeviceMac` usa pseudo-MAC/identificador local para remote config; debe revisarse privacidad antes de cualquier cloud sync.
- No se midieron APK, memoria, inicio ni tiempo al primer frame por falta de SDK/dispositivo.
- No se ejecutaron pruebas instrumentadas ni screenshot porque no hubo emulador/dispositivo.

## Recomendación para Fase 1

Realizar rebranding seguro y reproducible: `applicationId` propio, namespace/package planificado, nombre/iconos/banner/textos propios, About con atribución MIT, configuración local sin secretos, CI mínimo con JDK 17 + Android SDK, y mantener telemetría/updater desactivados salvo opt-in explícito con infraestructura propia.
