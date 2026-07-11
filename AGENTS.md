# AGENTS.md

## Propósito

Este archivo contiene instrucciones persistentes para Codex y otros agentes que trabajen en este repositorio.

Antes de modificar código:

1. Leer `README.md`.
2. Leer `docs/PRODUCT_REQUIREMENTS.md`.
3. Leer `docs/ARCHITECTURE.md`.
4. Leer `docs/DECISIONS.md`.
5. Revisar el código actual y confirmar las rutas reales; la documentación puede quedar desactualizada.
6. Resumir el alcance de la tarea y los archivos que probablemente cambiarán.

## Reglas obligatorias

- Trabajar únicamente en el alcance solicitado.
- No copiar código, recursos, iconos, textos ni diseño exacto de TiviMate, Netflix u otra aplicación propietaria.
- No incluir canales, listas, credenciales ni URLs IPTV reales.
- No asumir que una API del proveedor es estable; aislarla detrás de interfaces.
- No publicar claves API, tokens, contraseñas, certificados ni URLs privadas.
- No enviar telemetría al proyecto upstream.
- No conservar tokens hardcodeados heredados del fork.
- No activar actualización automática contra releases del upstream.
- No registrar credenciales, query strings sensibles, MAC, PIN o historial de reproducción.
- No usar migración destructiva de Room salvo en tests desechables.
- No cambiar `applicationId`, esquema de DB o firma sin documentar impacto.
- No hacer refactors grandes no relacionados.
- No introducir dependencias de producción sin justificar necesidad, licencia y tamaño.
- No ejecutar cambios destructivos sobre Git sin autorización explícita.
- No hacer commit, push, release o pull request salvo que la tarea lo pida.

## Base técnica

El objetivo es una aplicación nativa Android TV:

- Kotlin.
- Jetpack Compose.
- Compose for TV.
- Media3/ExoPlayer.
- Room.
- Hilt.
- WorkManager.
- Coil.
- Coroutines/Flow.
- JDK 17.

La implementación activa esperada está bajo `android-native/`. Verificarlo antes de trabajar.

## Orden de prioridad

1. Seguridad y privacidad.
2. Estabilidad de reproducción.
3. Navegación D-pad y foco.
4. Integridad de datos y migraciones.
5. Rendimiento.
6. Correctitud funcional.
7. Diseño visual.
8. Funciones nuevas.

Una función visualmente atractiva no está terminada si rompe foco, playback, memoria o migraciones.

## Arquitectura

Preferir flujo por capas:

```text
UI
→ ViewModel
→ Use case
→ Repository interface
→ Data source / DAO / API
```

Evitar:

- Consultas HTTP desde Composables.
- Acceso directo a DAO desde UI.
- Estado global mutable.
- Clases “Manager” sin responsabilidad clara.
- Lógica de zona horaria en la capa de presentación.
- Lógica de recomendaciones mezclada con composición de UI.
- Claves o credenciales dentro de código fuente.

## Compose y Android TV

- Toda pantalla debe ser utilizable solo con control remoto.
- Definir orden de foco intencional.
- Restaurar foco al volver.
- Evitar que el foco desaparezca durante recarga.
- No usar componentes móviles sin validar comportamiento en TV.
- Reducir recomposiciones.
- Usar keys estables en listas.
- Cargar imágenes de manera cancelable y con placeholders.
- No iniciar reproducción automática solo por mover el foco, salvo diseño explícito y probado.
- Validar Back, OK/Center, flechas y long press cuando aplique.
- Probar con escalas de fuente y overscan razonables.
- Mantener legibilidad a distancia.

## Media3 / reproducción

- No crear múltiples instancias de ExoPlayer sin necesidad.
- Liberar recursos siguiendo el ciclo de vida.
- Manejar audio focus.
- Manejar cambios de pista, errores de codec y reconexión.
- No ocultar errores; devolver un motivo útil y sanitizado.
- Probar HLS, DASH, MPEG-TS y MP4 cuando se cambie la capa de playback.
- No considerar suficiente el emulador para cambios de codec.
- Conservar fallback a reproductor externo mientras mejore compatibilidad.
- Medir tiempo hasta primer frame en cambios relevantes.

## Room

- Toda modificación de esquema requiere:
  - Incremento de versión.
  - Migration explícita.
  - Prueba de migración.
  - Documentación.
- Usar índices para claves de consulta frecuentes.
- Evitar cargar catálogos completos en memoria.
- Usar Paging o consultas limitadas.
- Las relaciones por perfil deben incluir `profileId` en índices y claves únicas.
- Conservar el dato original del proveedor aunque exista una versión enriquecida.

## Perfiles

Toda información personalizable debe evaluarse para separación por perfil:

- Favoritos.
- Historial.
- Progreso.
- Calificaciones.
- Recomendaciones.
- Último proveedor.
- Último canal.
- Preferencias.
- Restricciones parentales.

No migrar automáticamente datos existentes a todos los perfiles. Crear una estrategia explícita para un “Perfil principal” al introducir perfiles.

## EPG y zonas horarias

Regla obligatoria:

- Parsear timestamps con offset a `Instant`.
- Almacenar `startInstant` y `endInstant`.
- Mostrar usando `ZoneId` del perfil o dispositivo.
- No guardar la hora mostrada como la verdad canónica.
- Si no existe offset de origen, usar `epgSourceZoneId` del proveedor.
- La zona del usuario no reemplaza la zona de origen.
- Mantener un offset manual adicional por proveedor o canal.
- Usar IDs IANA; no reemplazar con offsets fijos.
- Añadir pruebas para cambio de horario de verano.
- No aplicar inferencias de baja confianza sin revisión.

## Metadatos

- Encapsular TMDB detrás de `MetadataProvider`.
- Nunca llamar TMDB desde un Composable.
- Normalizar títulos con pruebas basadas en fixtures.
- Mantener:
  - título original,
  - título normalizado,
  - tags de idioma,
  - tags de calidad,
  - resultado externo,
  - confianza,
  - corrección manual.
- Priorizar IDs directos.
- No asignar coincidencias ambiguas.
- Implementar límites, backoff y caché.
- No escanear miles de títulos en foreground.
- Usar WorkManager con lotes.
- Respetar atribución y licencia aplicables.

## Recomendaciones

MVP local y explicable:

- Algoritmo determinista.
- Pesos configurables.
- Motivo almacenado.
- Exclusión de vistos.
- No me gusta y no me interesa como señales negativas.
- Pruebas para orden y filtros.
- No introducir IA remota ni collaborative filtering sin decisión arquitectónica nueva.
- No enviar historial a un backend por defecto.

## Seguridad

- Usar Android Keystore para credenciales.
- Usar almacenamiento con salt y KDF apropiada para PIN.
- Sanitizar excepciones antes de telemetría.
- Telemetría desactivada por defecto hasta implementar consentimiento.
- No exportar secretos sin cifrado.
- No usar `ANDROID_ID` como credencial de autenticación fuerte.
- Revisar cualquier servidor o Worker heredado del upstream.
- Eliminar URLs y tokens upstream de builds propias.
- Revisar dependencias y licencias.

## Calidad

Antes de finalizar una tarea, desde `android-native/`:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

En Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

Cuando la tarea afecte UI y exista emulador/dispositivo:

```bash
./gradlew connectedDebugAndroidTest
```

Si un comando no puede ejecutarse:

- Explicar el motivo.
- Mostrar el error relevante.
- No afirmar que pasó.
- Indicar qué se verificó por otra vía.

## Tests mínimos por área

### EPG

- Timestamp con offset positivo.
- Timestamp con offset negativo.
- Timestamp sin offset y con zona de proveedor.
- Conversión entre zonas.
- DST spring-forward.
- DST fall-back.
- Offset manual adicional.
- Prioridad canal > proveedor > inferencia.

### Normalización

- Calidad.
- Idioma.
- Año.
- Película con números en el título.
- Serie SxxExx.
- Serie 2x04.
- Título traducido.
- Caracteres Unicode.
- Ambigüedad.

### Recomendaciones

- Excluir visto.
- Incluir en volver a ver.
- Señal de favorito.
- Señal negativa.
- Votos insuficientes.
- Diversidad.
- Perfil independiente.

### Base de datos

- Migración desde versión anterior.
- Índices.
- Restricciones únicas.
- Cascadas intencionales.
- Datos de perfiles no mezclados.

## Formato de entrega del agente

Al terminar, responder con:

1. Resumen de lo realizado.
2. Archivos principales modificados.
3. Decisiones y supuestos.
4. Pruebas ejecutadas y resultados reales.
5. Riesgos o deuda técnica.
6. Próximo paso recomendado.

No declarar una tarea completa si quedan errores de build, lint o tests relacionados.

## Criterio para dividir trabajo

Crear tareas pequeñas y secuenciales:

- Una migración de DB y sus pruebas.
- Un flujo de perfiles.
- Un parser EPG.
- Un cliente de metadatos.
- Un motor de recomendación.

No pedir a un solo agente que implemente perfiles, EPG, TMDB, recomendaciones y rediseño completo en un único cambio.

## Sincronización cloud

- Leer `docs/SYNC_SPEC.md` antes de modificar sync.
- Room es la fuente inmediata de UI.
- No realizar llamadas cloud desde Composables.
- Toda mutación sincronizable genera una operación idempotente en outbox.
- No confiar en la hora del cliente para ordenar conflictos.
- Usar `householdId` en toda autorización cloud.
- No sincronizar caché regenerable.
- No sincronizar credenciales IPTV en el MVP.
- Proteger tokens mediante Android Keystore.
- Toda eliminación sincronizable requiere tombstone.
- Un dispositivo revocado debe detener trabajos y limpiar tokens.
- No acoplar el dominio directamente a Firebase o Supabase.
- Añadir pruebas de offline, reintento, duplicados, revocación y conflictos.
