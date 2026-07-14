# Prompt de continuidad — rediseño de TV en vivo

Usa `TVenVIVO.md` como fuente prioritaria frente a cualquier contradicción y no menciones marcas; usa siempre “la aplicación de referencia”. No copies código, recursos, iconos, textos ni diseño exacto de terceros.

## Estado actual

- La etapa 1 quedó documentada en `docs/live-tv/REDESIGN_AUDIT.md`.
- `LiveTvReducer.kt` contiene estados objetivo explícitos para reproducción, información, lista, categorías, guía, detalles, menús contextuales, buffering y error.
- `RemoteActionMapper.kt` traduce teclas del control remoto a acciones del reducer.
- `LiveScreen.kt` ya empezó la etapa 2: usa `LiveTvUiState.mode` como máquina de estados visible principal, conserva una sola superficie `LiveBackgroundPlayer`, separa `CHANNEL_LIST_VISIBLE` de `CATEGORY_PANEL_VISIBLE`, y los menús contextuales pasan a mostrarse como paneles laterales sobre el reproductor.
- Los tests de reducer cubren Back de una sola capa y que mover foco no cambia `playingChannelId`.
- En este entorno los comandos Gradle siguen bloqueados por falta de SDK Android (`ANDROID_HOME` o `local.properties` con `sdk.dir`).

## Próximos pasos recomendados

1. Validar compilación en un entorno con SDK Android instalado:
   - `./gradlew test`
   - `./gradlew lint`
   - `./gradlew assembleDebug`
2. Revisar `LiveScreen.kt` con el compilador y corregir cualquier ajuste menor de Compose/Kotlin que no pueda detectarse sin SDK.
3. Completar la etapa 3 sin reescritura masiva:
   - persistir/restaurar foco real al cerrar cada capa;
   - hacer que el menú contextual restaure exactamente la capa de origen;
   - conectar acciones de programa y canal a un único modelo de menú lateral;
   - añadir recientes (`RECENT_CHANNELS_VISIBLE`) como fila horizontal sobre video.
4. Avanzar la guía EPG hacia una cuadrícula temporal virtualizada con columna de canal fija, encabezado de tiempo fijo y línea de “ahora”.
5. Añadir pruebas instrumentales o de UI cuando exista dispositivo/emulador Android TV para validar D-pad, OK, OK largo, Back y persistencia del reproductor.

## Restricciones

- No introducir listas IPTV reales, credenciales, telemetría ni URLs privadas.
- No crear otra instancia visible de reproductor para previews durante overlays.
- No iniciar reproducción solo por mover foco; la reproducción debe cambiar por OK, zapeo o acción explícita.
- Mantener cambios pequeños, secuenciales y verificables.
