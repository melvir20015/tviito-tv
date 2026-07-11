# Tviito TV

> Nombre provisional para un reproductor IPTV nativo orientado a Android TV y Google TV.

Tviito TV será un cliente IPTV local-first, diseñado para navegación con control remoto, con televisión en vivo, guía EPG, películas, series, múltiples proveedores y perfiles independientes. El objetivo es lograr una experiencia estable y completa comparable en funcionalidad general a los mejores reproductores IPTV, sin copiar código, marca, recursos visuales ni una interfaz exacta de aplicaciones propietarias.

## Estado

Proyecto en planificación e inicio técnico.

La estrategia recomendada es crear un **fork de Ultra TV** y trabajar únicamente sobre su implementación nativa ubicada en `android-native/`. Esa base ya utiliza Kotlin, Jetpack Compose/Compose for TV, Media3/ExoPlayer, Room, Hilt, Coil y WorkManager, además de admitir Xtream Codes, M3U/M3U8 y Stalker Portal.

Antes de implementar funciones nuevas se debe:

1. Confirmar que `android-native/` compila sin modificaciones.
2. Crear una línea base de pruebas.
3. Desactivar o sustituir cualquier telemetría, token, URL de Worker y actualizador que apunten al proyecto original.
4. Cambiar nombre, package/application ID, logotipo, textos y configuración de actualización.
5. Conservar el aviso de licencia MIT y el crédito requerido al proyecto base.
6. Documentar el comportamiento existente antes de modificar la base de datos.

## Visión del producto

Una sola instalación podrá contener varias listas o proveedores IPTV y varios perfiles de usuario.

Flujo principal:

```text
Abrir aplicación
    ↓
Seleccionar perfil
    ↓
Seleccionar proveedor o usar el último proveedor del perfil
    ↓
Inicio personalizado
    ├── Televisión en vivo
    ├── Guía EPG
    ├── Películas
    ├── Series
    ├── Continuar viendo
    ├── Favoritos
    └── Recomendaciones
```

Cada perfil mantendrá separados:

- Favoritos.
- Historial.
- Progreso de películas y episodios.
- Último canal visto.
- Último proveedor utilizado.
- Configuración de reproducción.
- Categorías visibles u ocultas.
- Contenido bloqueado.
- Calificaciones y señales de preferencia.
- Recomendaciones.
- Idioma y zona horaria de presentación, cuando se configure de manera individual.

## Principios

- **TV-first:** todas las pantallas deben funcionar completamente con D-pad, botón central y Back.
- **Local-first:** perfiles, historial, favoritos y recomendaciones iniciales se procesan localmente.
- **Privacidad:** no enviar historial, credenciales ni datos del catálogo a servicios propios sin consentimiento explícito.
- **Legalidad:** la aplicación no incluye, vende, aloja ni distribuye canales o contenido.
- **Estabilidad antes que cantidad:** no agregar funciones que degraden el cambio de canal, la reproducción o la navegación.
- **Diseño original:** se puede ofrecer funcionalidad comparable, pero no copiar exactamente pantallas, iconos, textos, marca o recursos de TiviMate, Netflix u otra aplicación.
- **Código comprobable:** toda función crítica debe incluir pruebas o una estrategia reproducible de verificación.
- **Migraciones seguras:** no usar migraciones destructivas de Room para usuarios existentes.

## Alcance del MVP

El primer MVP funcional debe incluir:

1. Compilación estable de la base nativa.
2. Rebranding completo.
3. Proveedores Xtream Codes y M3U.
4. Perfiles locales.
5. Asignación de proveedores por perfil.
6. Selección de proveedor al entrar.
7. Favoritos e historial separados por perfil.
8. Progreso independiente de películas y series.
9. TV en vivo con vista previa y cambio rápido.
10. EPG convertido automáticamente a la zona horaria del usuario.
11. Ajuste manual de EPG por proveedor y por canal.
12. Catálogo de películas y series con póster, fondo, sinopsis y créditos cuando existan.
13. Enriquecimiento opcional de metadatos mediante TMDB.
14. Recomendaciones locales explicables.
15. Controles parentales básicos.
16. Copia de seguridad y restauración local.
17. Sincronización opcional entre dispositivos.

Quedan fuera del primer MVP:

- Recomendaciones colaborativas entre todos los usuarios.
- Sincronización completa en la nube.
- Pagos y suscripciones.
- Tienda propia de listas o contenido.
- DRM propietario.
- Aplicaciones para iOS, web o escritorio.
- Copia exacta del diseño de otra aplicación.
- Multi-view, salvo que las métricas y pruebas posteriores justifiquen su regreso.
- Inteligencia artificial remota para analizar hábitos.
- Grabación avanzada de televisión en vivo y timeshift, hasta validar almacenamiento, permisos y compatibilidad.

## Funciones principales

### 1. Perfiles

Pantalla inicial tipo selector de perfiles, con diseño original:

- Nombre.
- Avatar.
- PIN opcional.
- Perfil infantil.
- Idioma.
- Proveedores permitidos.
- Preguntar siempre qué proveedor usar.
- Entrar al último proveedor.
- Bloqueo de creación, edición o eliminación mediante PIN administrador.

Entidades mínimas:

```text
Profile
ProfileProvider
ProfilePreference
ProfileParentalRule
```

### 2. Varios proveedores

Tipos iniciales:

- Xtream Codes.
- M3U/M3U8 por URL.
- M3U local.
- Stalker Portal como compatibilidad posterior o heredada de la base.

Cada proveedor debe tener:

- ID local estable.
- Nombre visible.
- Tipo.
- URL normalizada.
- Credenciales cifradas.
- EPG asociado.
- Zona horaria de origen del EPG.
- Prioridad.
- Fecha y estado de última sincronización.
- Política de actualización.
- Estado activo/inactivo.

Nunca registrar contraseñas, tokens ni URLs completas con credenciales.

### 3. Televisión en vivo

Vista TV-first con:

- Categorías.
- Lista de canales.
- Logotipo.
- Programa actual y siguiente.
- Barra de progreso.
- Vista previa.
- Canal anterior/siguiente.
- Cambio rápido.
- Favoritos.
- Recientes.
- Categorías ocultas.
- Bloqueo parental.
- Selector de audio y subtítulos.
- Relación de aspecto.
- Decodificación y fallback a reproductor externo cuando sea necesario.
- Estadísticas técnicas opcionales.

### 4. Guía EPG

La guía debe manejar correctamente **dos conceptos distintos**:

1. Zona horaria de origen del EPG.
2. Zona horaria de visualización del usuario.

Regla principal:

```text
Fecha/hora del EPG + zona/offset de origen
    → convertir a Instant/UTC
    → almacenar como instante universal
    → mostrar mediante la zona del dispositivo o la zona elegida por el perfil
```

Comportamiento:

- Usar `ZoneId.systemDefault()` por defecto.
- Permitir que el usuario seleccione otra zona IANA, por ejemplo `America/Los_Angeles`.
- Respetar offsets incluidos en XMLTV.
- Aplicar automáticamente horario de verano mediante reglas IANA.
- Si el EPG no incluye offset, usar la zona configurada en el proveedor.
- Inferir la zona de origen solo cuando exista una señal confiable.
- Conservar ajuste manual en horas y minutos:
  - Global por proveedor.
  - Por grupo.
  - Por canal.
- Permitir intervalos de 15 minutos.
- No sustituir silenciosamente una configuración manual confirmada.
- Guardar la razón y confianza de cualquier inferencia automática.

La zona del usuario por sí sola no puede corregir un EPG cuyo origen sea desconocido. El sistema debe conocer o inferir primero la zona de origen.

### 5. Películas y series

Presentación cinematográfica original:

- Póster.
- Backdrop.
- Gradiente para legibilidad.
- Título.
- Año.
- Duración.
- Géneros.
- Puntuación y cantidad de votos.
- Sinopsis.
- Reparto.
- Director o creadores.
- Clasificación por edad.
- Idiomas y calidad entregados por el proveedor.
- Temporadas y episodios.
- Continuar viendo.
- Reproducir siguiente episodio.
- Favorito.
- Me gusta, no me gusta y no me interesa.

El foco sobre un elemento no debe iniciar consultas de red costosas. Las imágenes y metadatos deben venir de caché o cargarse de forma cancelable.

### 6. Enriquecimiento de metadatos

Objetivo: completar catálogos donde el proveedor solo entrega un título o datos incompletos.

Flujo:

```text
Dato original del proveedor
    ↓
Normalización del título
    ↓
Detección de película/serie/episodio
    ↓
Búsqueda por ID externo o título + año
    ↓
Cálculo de confianza
    ↓
Asignación automática o revisión
    ↓
Persistencia local y caché
```

Ejemplo:

```text
Entrada:
JOHN.WICK.4.2023.2160P.LATINO

Resultado:
Título: John Wick: Chapter 4
Año: 2023
Calidad: 4K
Idioma: Español latino
```

Reglas:

- Priorizar `tmdb_id` o `imdb_id` cuando el proveedor los incluya.
- Separar etiquetas de calidad, idioma y fuente sin perderlas.
- No sobrescribir el dato original.
- Guardar proveedor de metadatos, versión, fecha y confianza.
- Coincidencia automática recomendada con confianza >= 0.90.
- Entre 0.75 y 0.89: revisión o aceptación diferida.
- Menor de 0.75: no asignar.
- Permitir corregir y bloquear manualmente una coincidencia.
- Unificar duplicados entre proveedores mediante un `ContentIdentity`.
- No publicar claves API en el repositorio.
- Usar un adaptador `MetadataProvider` para no depender permanentemente de TMDB.

Información deseada:

- Título oficial y original.
- Año y fecha de estreno.
- Sinopsis.
- Póster y backdrop.
- Géneros y palabras clave.
- Reparto.
- Director/creadores.
- Duración.
- Puntuación y cantidad de votos.
- Clasificación por edad.
- Colección o saga.
- Temporadas y episodios.
- IDs externos.

### 7. Recomendaciones

El MVP utilizará un algoritmo local, explicable y basado en reglas. No se requiere machine learning en la primera versión.

Señales:

- Porcentaje reproducido.
- Finalización.
- Repetición.
- Favorito.
- Me gusta.
- No me gusta.
- No me interesa.
- Abandono temprano.
- Géneros.
- Actores.
- Director/creador.
- Palabras clave.
- Saga.
- Idioma.
- Puntuación ponderada por cantidad de votos.
- Actividad reciente del perfil.

Ejemplo de pesos iniciales:

```text
30 % géneros
20 % actores
10 % director o creador
15 % palabras clave o temática
10 % puntuación ponderada
 5 % popularidad
10 % comportamiento reciente del perfil
```

Modificadores:

```text
+ saga que el usuario sigue
+ actor con afinidad alta
+ coincide con la intención de la sesión
- ya visto, excepto en "Volver a ver"
- marcado "No me interesa"
- marcado "No me gusta"
- abandonado de inmediato
```

Filas iniciales:

- Porque viste…
- Películas con actores que te gustan.
- Series parecidas a tus favoritas.
- Continuar viendo.
- Siguiente episodio.
- Volver a ver.
- Alta puntuación dentro de tus géneros.
- Descubrimientos para este perfil.
- Contenido familiar, para perfiles autorizados.

Cada recomendación debe poder explicar el motivo:

```text
Recomendada porque viste John Wick y Nobody.
Coincidencias: Acción · Thriller · Crimen.
```

No mezclar títulos ya vistos con recomendaciones nuevas, salvo en una fila explícita de “Volver a ver”.

### 8. Seguridad y privacidad

- Cifrar credenciales usando Android Keystore.
- No colocar tokens en `BuildConfig` de una compilación pública si pueden extraerse y abusarse.
- Mantener claves de desarrollo en `local.properties` o variables de entorno.
- No enviar historial o perfiles sin consentimiento.
- Hacer telemetría opt-in.
- Sanitizar logs.
- No registrar URLs con usuario, contraseña, token o MAC.
- Hashear PIN con una función apropiada y salt; no usar SHA-256 simple como almacenamiento final del PIN.
- Separar PIN parental y PIN administrador.
- Añadir exportación y restauración cifrada en una fase posterior.
- Revisar política de TMDB antes de una distribución comercial.

## Arquitectura propuesta

Estructura inicial, manteniendo un solo módulo Android mientras el proyecto madura:

```text
android-native/app/src/main/kotlin/<nuevo_paquete>/
├── app/
├── core/
│   ├── database/
│   ├── network/
│   ├── security/
│   ├── time/
│   ├── logging/
│   └── common/
├── data/
│   ├── provider/
│   ├── catalog/
│   ├── epg/
│   ├── metadata/
│   ├── recommendation/
│   └── profile/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── playback/
├── sync/
└── ui/
    ├── onboarding/
    ├── profiles/
    ├── providerpicker/
    ├── home/
    ├── live/
    ├── guide/
    ├── movies/
    ├── series/
    ├── details/
    ├── player/
    ├── recommendations/
    └── settings/
```

No realizar una modularización Gradle grande durante la primera fase. Primero estabilizar límites de dominio y pruebas; modularizar cuando los límites sean claros.

## Modelo de datos mínimo

```text
Profile
- id
- name
- avatarKey
- pinCredentialId
- isKids
- preferredLanguage
- displayZoneId
- createdAt
- updatedAt

Provider
- id
- name
- type
- baseUrl
- encryptedCredentialRef
- epgUrl
- epgSourceZoneId
- epgManualOffsetMinutes
- enabled
- lastSyncAt

ProfileProvider
- profileId
- providerId
- enabled
- priority
- isDefault

ContentIdentity
- id
- type
- tmdbId
- imdbId
- normalizedTitle
- releaseYear

ProviderContent
- id
- providerId
- providerItemId
- contentIdentityId
- originalTitle
- normalizedTitle
- streamDescriptor
- languageTags
- qualityTags

MetadataRecord
- contentIdentityId
- source
- sourceId
- title
- originalTitle
- overview
- posterPath
- backdropPath
- runtimeMinutes
- voteAverage
- voteCount
- confidence
- lockedByUser
- fetchedAt

Favorite
- profileId
- providerContentId
- createdAt

WatchProgress
- profileId
- providerContentId
- seasonNumber
- episodeNumber
- positionMs
- durationMs
- completed
- updatedAt

WatchEvent
- id
- profileId
- providerContentId
- type
- positionMs
- durationMs
- occurredAt

UserFeedback
- profileId
- contentIdentityId
- value
- createdAt

EpgProgram
- id
- providerId
- channelId
- startInstant
- endInstant
- originalStartText
- title
- description
- sourceZoneId
- appliedOffsetMinutes

ChannelTimeRule
- providerId
- channelId
- sourceZoneId
- manualOffsetMinutes
- detectionConfidence
- userLocked

Recommendation
- profileId
- contentIdentityId
- score
- reasonCode
- explanation
- generatedAt
```

## Tecnologías

Base:

- Kotlin.
- Jetpack Compose.
- Compose for TV.
- AndroidX Media3 / ExoPlayer.
- Room.
- Hilt.
- WorkManager.
- Coil.
- Retrofit/OkHttp o el cliente ya presente en la base.
- Kotlin Coroutines y Flow.
- Java Time (`Instant`, `ZoneId`, `ZonedDateTime`).
- JDK 17.

Servicios opcionales:

- TMDB para metadatos.
- Backend propio únicamente para configuración remota, sincronización opcional o telemetría consentida.

## Variables locales

Crear un archivo no versionado para secretos de desarrollo:

```properties
# local.properties
TMDB_BEARER_TOKEN=replace_me
```

Agregar una muestra sin secretos:

```properties
# local.properties.example
TMDB_BEARER_TOKEN=
```

Nunca incluir credenciales IPTV reales en fixtures, capturas, tickets, commits o logs.

## Inicio del desarrollo

### Opción recomendada: fork

```bash
git clone https://github.com/khalilbenaz/ultra-tv.git
cd ultra-tv
git remote rename origin upstream
git remote add origin <URL_DE_TU_REPOSITORIO>
git checkout -b develop
```

Compilar la base:

```bash
cd android-native
./gradlew clean test lint assembleDebug
```

En Windows PowerShell:

```powershell
cd android-native
.\gradlew.bat clean test lint assembleDebug
```

APK:

```text
android-native/app/build/outputs/apk/debug/app-debug.apk
```

### Primer objetivo

No comenzar implementando recomendaciones, TMDB o perfiles.

El primer pull request debe limitarse a:

1. Auditoría reproducible de la base.
2. Compilación limpia.
3. Rebranding provisional.
4. Eliminación o aislamiento de telemetría y actualizaciones del upstream.
5. Configuración segura de secretos.
6. Pruebas mínimas de humo.
7. Documentación de la base de datos y navegación actuales.

## Comandos de calidad

Desde `android-native/`:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Cuando exista un emulador o dispositivo:

```bash
./gradlew connectedDebugAndroidTest
```

Antes de considerar completa una tarea:

- Compila.
- Lint no introduce errores nuevos.
- Pruebas relevantes pasan.
- No hay secretos.
- No se rompió navegación por D-pad.
- No se rompieron migraciones.
- Se documentaron cambios visibles.
- Se probó al menos en emulador Android TV.
- Para reproducción, se realizó prueba en hardware real cuando el cambio afecte códecs o decodificación.

## Estrategia de ramas

```text
main       releases estables
develop    integración
feature/*  funciones
fix/*      correcciones
chore/*    mantenimiento
```

Los cambios grandes deben dividirse en pull requests revisables. No crear un solo PR para toda la aplicación.

## Criterios de aceptación del MVP

- Un usuario puede crear al menos cuatro perfiles.
- Cada perfil mantiene favoritos e historial independientes.
- Un perfil puede tener acceso a uno o varios proveedores.
- Al entrar se puede preguntar qué proveedor usar o abrir el último.
- El EPG se presenta en la zona horaria local del usuario.
- Los cambios de horario de verano no requieren un ajuste manual fijo.
- El usuario puede corregir un proveedor o canal con offset manual.
- El catálogo puede enriquecerse sin modificar el título original del proveedor.
- Las coincidencias inciertas no se asignan silenciosamente.
- Los duplicados entre listas pueden compartir una identidad de contenido.
- Las recomendaciones excluyen lo ya visto, salvo “Volver a ver”.
- Cada recomendación tiene una explicación.
- La aplicación funciona sin cuenta en la nube.
- Las credenciales no aparecen en logs ni exportaciones sin cifrar.
- Todas las pantallas principales son operables con D-pad.
- La reproducción existente no empeora frente a la línea base medida.

## Métricas técnicas

Registrar localmente, sin contenido sensible:

- Tiempo de inicio.
- Tiempo hasta mostrar catálogo.
- Tiempo hasta primer frame.
- Porcentaje de fallos de reproducción.
- Reconexiones.
- Tasa de coincidencia de metadatos.
- Coincidencias corregidas manualmente.
- Tiempo de generación de recomendaciones.
- Uso de memoria en catálogos grandes.
- Fluidez de scroll y cambios de foco.
- Errores de parsing EPG.
- Canales con zona inferida y confianza.

## Reglas legales y de distribución

- Mantener la licencia MIT y atribución del código base.
- No usar el nombre TiviMate, Netflix ni logotipos de terceros en la aplicación.
- No presentar la app como afiliada o aprobada por dichos servicios.
- No distribuir listas, credenciales, enlaces o contenido.
- Mostrar un aviso: el usuario debe usar fuentes para las cuales tenga autorización.
- Revisar los requisitos de atribución y uso comercial del proveedor de metadatos antes de publicar.
- Revisar las políticas de Google Play aplicables a contenido, propiedad intelectual y credenciales.

## Documentos del repositorio

- `README.md`: visión, alcance e instalación.
- `AGENTS.md`: instrucciones obligatorias para Codex.
- `docs/PRODUCT_REQUIREMENTS.md`: requisitos detallados.
- `docs/ARCHITECTURE.md`: arquitectura y decisiones técnicas.
- `docs/ROADMAP.md`: fases y backlog.
- `docs/DECISIONS.md`: decisiones que no deben reinterpretarse sin discusión.
- `docs/CODEX_START_PROMPT.md`: primera tarea lista para Codex.
- `docs/SYNC_SPEC.md`: cuenta, vinculación, offline-first, conflictos y seguridad.

## Próximo paso

Abrir el repositorio en Codex y ejecutar únicamente la tarea contenida en:

```text
docs/CODEX_START_PROMPT.md
```

## Sincronización entre dispositivos

La sincronización será opcional y offline-first:

- Room seguirá siendo la base local.
- El usuario podrá crear una cuenta y un hogar.
- Otro televisor se vinculará mediante QR o código temporal.
- Se sincronizarán perfiles, favoritos, historial, progreso, feedback, preferencias, controles parentales, ajustes EPG y correcciones manuales.
- Cachés, imágenes, catálogo completo y filas EPG se regenerarán localmente.
- Las credenciales IPTV no se sincronizarán en el MVP.
- Una fase posterior podrá sincronizarlas con cifrado de extremo a extremo.
- El backend se abstraerá mediante `CloudSyncGateway`.
- Los conflictos se resolverán según el tipo de dato.

Especificación: `docs/SYNC_SPEC.md`.
