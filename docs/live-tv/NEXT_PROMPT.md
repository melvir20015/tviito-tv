# Prompt para continuar el rediseño de TV en vivo

Copia y pega este prompt en el siguiente chat:

```text
Continúa el rediseño del módulo de TV en vivo desde el workspace actual. Usa `TVenVIVO.md` como fuente prioritaria frente a cualquier contradicción y no menciones marcas; usa siempre “la aplicación de referencia”.

Estado actual ya implementado:
- Se creó `docs/live-tv/REDESIGN_AUDIT.md` con la auditoría de etapa 1, escenas objetivo, comparación contra la implementación actual y matriz de control remoto.
- Se actualizó `LiveTvReducer.kt` para introducir estados objetivo explícitos: `FULLSCREEN_PLAYBACK`, `PROGRAM_INFO_VISIBLE`, `RECENT_CHANNELS_VISIBLE`, `CHANNEL_LIST_VISIBLE`, `CATEGORY_PANEL_VISIBLE`, `EPG_VISIBLE`, `PROGRAM_DETAILS_VISIBLE`, `CONTEXT_MENU_VISIBLE`, `BUFFERING` y `PLAYBACK_ERROR`.
- Se mantuvieron alias legados mientras `LiveScreen.kt` se migra por etapas.
- Se actualizó `RemoteActionMapper.kt` para mapear reproducción limpia a lista/guía nuevas.
- Se actualizaron tests unitarios de reducer y mapper para cubrir Back por una sola capa, OK desde fullscreen, cambio de canal con buffering y restauración del menú contextual.
- Los comandos `./gradlew test`, `./gradlew lint` y `./gradlew assembleDebug` no pudieron ejecutarse por falta de SDK Android (`ANDROID_HOME` o `local.properties`).

Siguiente paso recomendado:
Implementa la etapa 2 y parte de la etapa 3 sin hacer una reescritura masiva: conecta `LiveScreen.kt` al reducer como única máquina de estados visible, elimina booleanos independientes para overlays principales, conserva una sola instancia de `LiveBackgroundPlayer`, separa `ChannelListVisible` y `CategoryPanelVisible`, y convierte los menús contextuales centrados en paneles laterales sobre el reproductor. Añade o ajusta tests unitarios para validar Back de una sola capa y que mover foco no cambia `playingChannelId`.

Antes de editar:
1. Lee `README.md`, `docs/PRODUCT_REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md`, `TVenVIVO.md`, `AGENTS.md` y `docs/live-tv/REDESIGN_AUDIT.md`.
2. Revisa el estado actual de `LiveScreen.kt`, `LiveTvReducer.kt`, `RemoteActionMapper.kt` y tests.
3. Resume el alcance y archivos a tocar.

No introduzcas listas IPTV reales, credenciales, telemetría ni copias de recursos de terceros.
```
