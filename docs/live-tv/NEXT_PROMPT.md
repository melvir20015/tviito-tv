# Prompt de continuidad — módulo de TV en vivo

Usa este archivo como punto de partida para continuar el módulo de TV en vivo en una ventana limpia. La fuente de verdad siempre es el estado actual del workspace; si este documento contradice el código, prevalece el código. No menciones marcas ni copies código, recursos, iconos, textos o diseño exacto de aplicaciones propietarias; usa siempre “la aplicación de referencia” cuando necesites comparar comportamientos.

## Estado confirmado

- La auditoría funcional de la etapa 1 está documentada en `docs/live-tv/REDESIGN_AUDIT.md`.
- La implementación activa esperada sigue bajo `android-native/`.
- `LiveTvReducer.kt` ya define modos explícitos para reproducción a pantalla completa, información inferior, recientes, lista, categorías, guía, detalles, menú contextual, buffering y error.
- `RemoteActionMapper.kt` es la ruta central para traducir teclas del control remoto a acciones del reducer.
- `LiveScreen.kt` ya usa `LiveTvUiState.mode` como máquina de estados visible principal y mantiene `LiveBackgroundPlayer` como superficie persistente detrás de overlays.
- `LiveScreen.kt` ya separa visualmente `CHANNEL_LIST_VISIBLE` de `CATEGORY_PANEL_VISIBLE` y muestra menús contextuales mediante `CONTEXT_MENU_VISIBLE`.
- Existen `FocusRequester` para categorías, canales y guía, pero la restauración exacta del foco por capa todavía debe validarse y completarse.
- La guía EPG visible en `LiveScreen.kt` continúa siendo una guía simplificada de ahora/siguiente; la cuadrícula temporal real debe implementarse de forma incremental.
- En este entorno los comandos Gradle pueden fallar si no hay SDK Android configurado (`ANDROID_HOME` o `local.properties` con `sdk.dir`). No afirmar que pasan si el entorno no permite ejecutarlos.

## Alcance recomendado para el siguiente paso

Completar una etapa pequeña y verificable enfocada en navegación y overlays antes de rediseñar toda la guía EPG:

1. Validar el estado actual del código y compilar en un entorno con SDK Android.
2. Corregir errores menores de Kotlin/Compose que aparezcan al compilar.
3. Completar la restauración de foco por capa:
   - recordar el último foco de canales, categorías y guía;
   - restaurar foco al cerrar menú contextual;
   - evitar que el foco desaparezca durante recargas;
   - mantener Back cerrando una sola capa por vez.
4. Unificar el menú contextual de canal y programa como panel lateral coherente, sin diálogo centrado y sin crear otro reproductor.
5. Añadir `RECENT_CHANNELS_VISIBLE` como fila horizontal sobre el video, activada por acción explícita, con reproducción solo al pulsar OK.
6. Añadir o ajustar pruebas del reducer para:
   - Back desde menú contextual restaura la capa origen;
   - mover foco no cambia `playingChannelId`;
   - OK en lista/guía/recientes sí selecciona canal;
   - Back cierra solo una capa.

## Trabajo que NO debe hacerse en este paso

- No implementar perfiles, EPG completo, TMDB, recomendaciones, sincronización cloud ni rediseño global en el mismo cambio.
- No cambiar `applicationId`, esquema de base de datos, firma ni migraciones Room.
- No introducir dependencias de producción sin justificar necesidad, licencia y tamaño.
- No agregar listas IPTV reales, credenciales, tokens, URLs privadas ni telemetría.
- No crear reproducción automática por solo mover foco.
- No crear una segunda superficie visible de reproductor para previews.
- No copiar diseño exacto de terceros.

## Verificación mínima esperada

Desde `android-native/`, ejecutar cuando el entorno tenga SDK Android:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Si afecta comportamiento visible y existe emulador/dispositivo Android TV, ejecutar además:

```bash
./gradlew connectedDebugAndroidTest
```

Si algún comando no puede ejecutarse, documentar el motivo y el error relevante.

## Prompt para copiar y pegar en el siguiente paso

```text
Continúa el módulo de TV en vivo usando como fuente de verdad el workspace actual y `docs/live-tv/NEXT_PROMPT.md`.

Objetivo: completar una etapa pequeña y verificable de navegación y overlays de TV en vivo antes de abordar la guía EPG temporal completa.

Instrucciones obligatorias:
1. Lee `AGENTS.md`, `README.md`, `docs/PRODUCT_REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md` y `docs/live-tv/NEXT_PROMPT.md`.
2. Si existe `codex.md`, léelo también y respeta sus instrucciones no globales del proyecto.
3. Revisa el código actual en `android-native/app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/` antes de editar; no asumas versiones anteriores.
4. Resume primero el alcance y los archivos que probablemente cambiarán.
5. Implementa solo el siguiente alcance:
   - restauración de foco por capa en TV en vivo;
   - menú contextual lateral unificado para canal/programa, restaurando la capa origen;
   - fila `RECENT_CHANNELS_VISIBLE` sobre el video con reproducción solo por OK;
   - pruebas del reducer para Back de una sola capa, restauración de origen del menú, foco sin cambio de reproducción y OK explícito.
6. Mantén una sola superficie persistente de reproductor; no crees previews con un segundo player visible.
7. No agregues IPTV real, credenciales, URLs privadas, telemetría ni dependencias de producción innecesarias.
8. No cambies `applicationId`, Room schema, firma ni migraciones.
9. Ejecuta desde `android-native/`: `./gradlew test`, `./gradlew lint` y `./gradlew assembleDebug`. Si el SDK Android no está configurado, muestra el error real y no digas que pasó.
10. Entrega el resultado en español con resumen, archivos modificados, decisiones/supuestos, pruebas reales, riesgos/deuda técnica y próximo paso recomendado.
```
