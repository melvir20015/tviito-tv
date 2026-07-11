# Roadmap

## Fase 0 — Auditoría y línea base

- [ ] Fork del repositorio.
- [ ] Confirmar licencia.
- [ ] Compilar `android-native/`.
- [ ] Registrar versiones de Gradle, AGP, Kotlin, Media3, Room y Compose.
- [ ] Ejecutar test, lint y assemble.
- [ ] Crear smoke test de lanzamiento.
- [ ] Inventariar entidades Room.
- [ ] Inventariar rutas de navegación.
- [ ] Identificar telemetría, Worker, tokens y self-update.
- [ ] Desactivar conexiones al upstream.
- [ ] Medir APK, inicio, memoria y tiempo al primer frame.
- [ ] Crear fixtures legales para pruebas.

**Salida:** baseline reproducible y segura.

## Fase 1 — Identidad propia

- [x] Nombre provisional: Tviito TV.
- [x] `applicationId` propio: `com.tviito.tv`.
- [x] Package Kotlin propio o plan gradual: namespace Kotlin heredado conservado temporalmente; plan gradual documentado.
- [x] Icono y banner originales.
- [x] About con atribución MIT.
- [x] Actualizador propio desactivado o configurado solo por opt-in `TVIITO_*`.
- [x] Telemetría desactivada por defecto.
- [x] Configuración de secretos con prefijo propio `TVIITO_*`.
- [x] CI básico con JDK 17 y Android SDK.

**Salida:** build propio sin dependencia operativa del upstream. Ver `docs/PHASE_1_IDENTITY.md`.

## Fase 2 — Perfiles

- [ ] Modelo Profile.
- [ ] Migración Room.
- [ ] Perfil principal para datos existentes.
- [ ] Selector de perfiles.
- [ ] CRUD.
- [ ] Avatar.
- [ ] PIN administrador.
- [ ] Perfil infantil.
- [ ] Perfil activo.
- [ ] Pruebas de aislamiento.

**Salida:** favoritos e historial independientes.

## Fase 3 — Proveedores por perfil

- [ ] `ProfileProvider`.
- [ ] Selector al entrar.
- [ ] Último proveedor.
- [ ] Preguntar siempre.
- [ ] Prioridad.
- [ ] Proveedor oculto o desactivado.
- [ ] Credenciales con Keystore.
- [ ] Sanitización de logs.

**Salida:** cada perfil ve solo sus listas autorizadas.

## Fase 4 — EPG y zona horaria

- [ ] Auditar parser actual.
- [ ] Persistir `Instant`.
- [ ] Zona de visualización.
- [ ] Zona de origen por proveedor.
- [ ] Zona por canal.
- [ ] Offset manual.
- [ ] UI de configuración.
- [ ] DST tests.
- [ ] XMLTV sin offset.
- [ ] Estado sin resolver.
- [ ] Inferencia opcional con confianza.
- [ ] Guía 7 días con windowing.

**Salida:** EPG correcto en la zona del usuario.

## Fase 5 — Identidad y metadatos

- [ ] Normalizador.
- [ ] Tags de idioma/calidad.
- [ ] Detector película/serie/episodio.
- [ ] ContentIdentity.
- [ ] MetadataProvider.
- [ ] Cliente TMDB.
- [ ] Caché.
- [ ] WorkManager por lotes.
- [ ] Confianza.
- [ ] Revisión manual.
- [ ] Corrección bloqueada.
- [ ] Duplicados entre listas.
- [ ] Atribución.

**Salida:** fichas completas y confiables.

## Fase 6 — Experiencia cinematográfica

- [ ] Hero/backdrop.
- [ ] Gradientes.
- [ ] Póster.
- [ ] Detalle.
- [ ] Reparto.
- [ ] Temporadas/episodios.
- [ ] Carruseles.
- [ ] Preloading limitado.
- [ ] Focus restore.
- [ ] Pruebas de rendimiento.

**Salida:** VOD atractivo y fluido.

## Fase 7 — Recomendaciones

- [ ] WatchEvent.
- [ ] Feedback.
- [ ] Afinidad por género/persona/keyword.
- [ ] Puntuación ponderada.
- [ ] Scoring local.
- [ ] Exclusión de vistos.
- [ ] Volver a ver.
- [ ] Explicaciones.
- [ ] Diversidad.
- [ ] Cold start.
- [ ] Perfil infantil.
- [ ] Pruebas deterministas.

**Salida:** recomendaciones útiles por perfil.

## Fase 8 — Estabilidad de reproducción

- [ ] Medición de first frame.
- [ ] Reconexión.
- [ ] Retry policy.
- [ ] Errores de codec.
- [ ] Audio/subtítulos.
- [ ] Aspect ratio.
- [ ] External player.
- [ ] Estado de progreso.
- [ ] Hardware matrix.
- [ ] Fire TV tests.
- [ ] Memoria y ANR.

**Salida:** playback estable en hardware real.

## Fase 9 — Cuenta y sincronización opcional

- [ ] `CloudSyncGateway`.
- [ ] Cuenta y hogar.
- [ ] Registro y revocación de dispositivos.
- [ ] Vinculación mediante QR/código.
- [ ] Outbox local.
- [ ] Cursor de cambios.
- [ ] Push/pull idempotente.
- [ ] Perfiles.
- [ ] Favoritos y feedback.
- [ ] Progreso e historial.
- [ ] Preferencias y reglas parentales.
- [ ] Ajustes EPG y correcciones manuales.
- [ ] Conflictos por entidad.
- [ ] Export e import local.
- [ ] Política de privacidad.
- [ ] Evaluación de credenciales E2EE.

**Salida:** perfiles y guardados disponibles en varios dispositivos, manteniendo funcionamiento offline.

## Orden de ejecución inmediato

1. Fase 0.
2. Fase 1.
3. Fase 2.
4. Fase 3.
5. Fase 4.

No iniciar TMDB ni recomendaciones antes de que perfiles, proveedores y zonas horarias tengan modelos de datos estables.
