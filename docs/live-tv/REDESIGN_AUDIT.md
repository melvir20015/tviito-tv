# Rediseño de TV en vivo — auditoría de etapa 1

> Fuente prioritaria: `TVenVIVO.md`. Este documento no menciona marcas y usa “la aplicación de referencia”.

## Alcance aplicado en este parche

Este parche inicia el rediseño por la base exigida antes de reescribir la UI completa: auditoría funcional, mapa de estados, matriz de control remoto y criterios para reemplazar vistas. No incluye capturas comparativas porque el entorno actual no expone un emulador/dispositivo Android TV ni reproducción local del video; por tanto, las medidas visuales quedan como trabajo pendiente de etapa 7.

## Escenas observadas y estados objetivo

| Escena | Estado inicial | Entrada | Elementos visibles | Transición | Estado final | Back | OK | OK largo | D-pad |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Entrada a TV en vivo | Módulo abierto | Automática | Reproductor a pantalla completa, sin barras permanentes | Primer frame sustituye carga | `FullscreenPlayback` | Sale del módulo | Abre información inferior | Abre acciones del canal activo | Arriba/abajo zapeo; izquierda lista; derecha guía/acción observada |
| Información inferior | `FullscreenPlayback` | OK | Video visible, gradiente inferior, logo, número/nombre, programa actual, siguiente, progreso, hora | Entrada desde abajo + fundido | `ProgramInfoVisible` | Cierra solo overlay | Mantiene/ejecuta acción observada | Menú contextual | Entradas válidas reinician temporizador |
| Lista de canales | Reproducción limpia | Izquierda | Panel vertical izquierdo, video persistente, canal activo y enfocado diferenciados | Entrada desde izquierda con scrim | `ChannelListVisible` | Cierra lista | Reproduce canal enfocado | Menú contextual | Arriba/abajo foco; izquierda categorías; derecha guía/acción observada |
| Categorías | `ChannelListVisible` | Izquierda | Columna adicional desde la izquierda, lista conserva posición | Desplazamiento lateral corto | `CategoryPanelVisible` | Cierra categorías | Activa categoría | Sin acción especial | Arriba/abajo categoría; derecha vuelve a lista |
| Guía EPG | Lista o reproducción | Derecha / acceso directo | Columna fija de canales, encabezado de tiempo, celdas proporcionales, línea de ahora | Panel sobre reproductor | `EpgVisible` | Vuelve al nivel anterior | Reproduce si es programa en vivo | Menú de programa | Vertical conserva hora; horizontal desplaza tiempo |
| Recientes | Reproducción limpia | Acción dedicada | Fila horizontal sobre video | Entrada breve sobre video | `RecentChannelsVisible` | Cierra recientes | Reproduce reciente | Menú contextual | Izquierda/derecha recorren recientes |
| Menú contextual | Lista/guía/info | OK largo/Menu | Panel vertical lateral, no diálogo centrado | Entrada desde lado observado | `ContextMenuVisible` | Cierra solo menú y restaura foco | Ejecuta acción | Sin cambio | Arriba/abajo acciones |
| Buffering/error | Cambio de canal | OK/canal +/- | Video persistente, indicador discreto o error sanitizado | Sin navegación de pantalla | `Buffering` / `PlaybackError` | Vuelve a reproducción limpia o capa previa | Reintenta si aplica | Acciones si aplica | No pierde foco |

## Comparación con implementación actual

| Elemento | Referencia requerida | Implementación actual | Diferencia | Acción requerida |
| --- | --- | --- | --- | --- |
| Reproductor | Una superficie persistente detrás de todas las capas | `LiveBackgroundPlayer` se mantiene en `LiveScreen`, pero hay código legado de preview adicional | Riesgo de múltiples superficies y estados duplicados | Consolidar en un coordinador único de reproducción y eliminar preview heredado no usado |
| Overlay de información | Gradiente inferior integrado, temporizado | Existe `LiveChannelInfoBar` inferior | Base útil, falta máquina de estados explícita y control remoto centralizado | Conectar a `ProgramInfoVisible` y reiniciar temporizador por entrada válida |
| Lista de canales | Panel lateral izquierdo sin ocupar toda la pantalla | Existe panel izquierdo con categorías/lista/now panel | Se muestran categorías siempre junto a lista; debe abrirse como capa adicional cuando corresponda | Separar `ChannelListVisible` y `CategoryPanelVisible` |
| Categorías | Columna adicional desde izquierda | `PlaylistPanel` aparece dentro del overlay de canales | No respeta apertura/cierre independiente ni restauración exacta de foco | Convertir a capa con foco previo persistente |
| Guía EPG | Columna fija, encabezado fijo, desplazamiento horizontal proporcional | `EpgOverlayGuide` solo muestra ahora/siguiente | No hay grid temporal real | Reemplazar por guía virtualizada con eje horizontal |
| Panel de acciones | Panel lateral, no diálogo centrado | `ChannelContextMenu` y `ProgramContextMenu` están centrados | Contradice `TVenVIVO.md` | Rehacer como panel lateral con restauración de foco |
| Recientes | Fila horizontal sobre video | No existe capa dedicada | Función pendiente | Implementar `RecentChannelsVisible` |
| Indicadores de foco | Diferenciar activo/enfocado sin escalado exagerado | Hay borde activo/foco y escala leve | Base aceptable, necesita calibración visual con captura | Ajustar después de comparación visual |
| Back | Cierra una sola capa en orden definido | BackHandler manual cierra capas, pero no usa jerarquía completa | Falta orden formal y recientes/detalles | Centralizar en reducer |
| OK largo | Abre menú contextual | Soportado por mapper/reducer; UI usa `Key.Menu` en filas | Falta propagación uniforme de long press | Unificar con `RemoteActionMapper` |
| Persistencia del video | Nunca recrear por overlays | Reproductor recordado en pantalla | Debe verificarse con prueba/instrumentación | Añadir prueba manual/instrumental cuando haya dispositivo |

## Matriz de entradas del control remoto

| Estado | Arriba | Abajo | Izquierda | Derecha | OK | OK largo | Back |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `FullscreenPlayback` | Canal anterior | Canal siguiente | Abre lista | Abre guía/acción observada | Abre info inferior | Menú canal activo | Sale del módulo |
| `ProgramInfoVisible` | Canal anterior | Canal siguiente | Abre lista | Abre guía | Reinicia/ejecuta acción observada | Menú canal activo | Cierra info |
| `ChannelListVisible` | Foco canal anterior | Foco canal siguiente | Abre categorías | Abre guía/acción observada | Reproduce enfocado | Menú canal enfocado | Cierra lista |
| `CategoryPanelVisible` | Categoría anterior | Categoría siguiente | Permanece/cierra según borde | Vuelve a lista | Activa categoría | Menú si aplica | Cierra categorías |
| `EpgVisible` | Canal anterior conservando hora | Canal siguiente conservando hora | Programa anterior/retrocede tiempo | Programa siguiente/avanza tiempo | Reproduce/acciones según programa | Menú programa | Vuelve a nivel anterior |
| `RecentChannelsVisible` | Sin cambio | Sin cambio | Reciente anterior | Reciente siguiente | Reproduce reciente | Menú canal | Cierra recientes |
| `ContextMenuVisible` | Acción anterior | Acción siguiente | Sin cambio | Sin cambio | Ejecuta acción | Sin cambio | Cierra menú |
| `Buffering` | No cambia foco | No cambia foco | No cambia capa | No cambia capa | Sin acción | Menú si hay canal | Vuelve a fullscreen/capa previa |
| `PlaybackError` | No cambia foco | No cambia foco | Abre lista si aplica | Abre guía si aplica | Reintenta si aplica | Menú si hay canal | Vuelve a fullscreen/capa previa |

## Archivos que deben reemplazarse o reestructurarse

- `android-native/app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/LiveScreen.kt`: dividir en superficie persistente, capas, paneles y guía.
- `android-native/app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/LiveTvReducer.kt`: ya iniciado en este parche con estados objetivo explícitos; debe seguir controlando todas las capas.
- `android-native/app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/remote/RemoteActionMapper.kt`: debe seguir siendo la única traducción de teclas a acciones.
- `android-native/app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/guide/EpgGuide.kt`: reemplazar por grid virtualizado real con cabecera y columna fijas.

## Diferencias pendientes principales

1. Falta reproducir y medir fotogramas equivalentes en un entorno con acceso visual a la referencia.
2. Falta reemplazar menús centrados por paneles laterales.
3. Falta separar categorías como capa independiente.
4. Falta implementar recientes.
5. Falta guía EPG temporal proporcional real.
6. Falta prueba instrumental de foco, Back de una sola capa y no recreación del reproductor.

## Seguimiento de ejecución — 2026-07-14

### Estado real confirmado

- `android-native/` sigue siendo la implementación activa revisada para TV en vivo: la navegación principal llama a `LiveScreen` desde `MainActivity` y el código del módulo está bajo `app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/`.
- El módulo ya tiene una superficie persistente principal en `LiveBackgroundPlayer`, montada detrás de las capas de lista, categorías, guía, recientes, información inferior y menú contextual.
- La guía visible usa `TimelineEpgGuide` con columna de canales, encabezado horario, celdas por duración y línea de ahora; requiere validación visual real antes de ajustes finos.
- No se detectó emulador/dispositivo Android TV disponible desde esta ejecución, por lo que la fase visual sigue bloqueada por entorno.

### Fases

| Fase | Estado | Evidencia / motivo |
| --- | --- | --- |
| A — Auditoría de estado real | Parcial | Código y documentación revisados; falta captura real del video/dispositivo para cerrar la comparación visual. |
| B — Diagnóstico de pantalla negra | Parcial | Se añadieron estados visibles seguros para proveedor ausente, lista vacía, carga, resolución, buffering, error sanitizado y canal bloqueado. Falta prueba en dispositivo. |
| C — Reproductor persistente e integración | Parcial | `LiveScreen` reproduce en sitio con una superficie persistente; se eliminó el callback de navegación `onPlay` para evitar integración muerta. Falta instrumentación visual de no recreación. |
| D — Máquina de estados y D-pad | Parcial | Reducer y mapper ya cubren capas principales; pruebas existentes validan Back de una capa, OK explícito y foco sin reproducción. |
| E — Restauración de foco | Parcial | Existe memoria de canal por capa en reducer; falta foco específico por categoría/programa/acción de menú con prueba instrumental. |
| F — Overlays visuales | Parcial | Capas principales existen; no se hicieron ajustes visuales finos sin capturas. |
| G — Guía EPG | Parcial | Hay timeline incremental; quedan acciones y conservación temporal avanzada por validar visualmente. |
| H — Canales recientes | Parcial | Existe capa de recientes y filtrado por historial/últimos canales; falta ampliar pruebas de historial real de repositorio. |
| I — Categorías/favoritos/bloqueos/orden | Parcial | Filtros y orden existen en ViewModel; se reforzó estado visible de canal bloqueado. |
| J — Error y seguridad | Parcial | Se sanitizan mensajes visibles de reproducción para ocultar URLs y parámetros sensibles; falta auditoría completa de logs ajenos a Live. |
| K — Evidencia visual | Bloqueada | Requiere SDK/adb/emulador o dispositivo Android TV disponible. |
| L — Documentación de seguimiento | Terminada en esta ejecución | Esta sección registra el estado y próximos pasos. |

### Próximo paso recomendado

Ejecutar la app en un emulador o dispositivo Android TV con datos sintéticos, capturar los estados definidos en `VISUAL_CAPTURE_AUDIT.md` y validar que los nuevos estados de error/carga no dejan la pantalla negra ni exponen datos sensibles.
