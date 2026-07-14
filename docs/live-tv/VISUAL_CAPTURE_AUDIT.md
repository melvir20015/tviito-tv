# Auditoría visual pendiente — TV en vivo

> Fuente prioritaria: `TVenVIVO.md`. Este documento usa “la aplicación de referencia” y no copia recursos, textos propietarios, logotipos ni marcas dentro de código o UI.

## Alcance de esta verificación

La solicitud exige abrir el módulo de TV en vivo bajo `android-native/` en un entorno con emulador o dispositivo Android TV, capturar estados principales y comparar cada captura con el fotograma equivalente del video indicado en `TVenVIVO.md`.

En este workspace no se pudo completar la captura visual porque el entorno no expone las herramientas Android necesarias:

- `adb` no está disponible.
- `emulator` no está disponible.
- No hay un dispositivo Android TV conectado detectable desde la terminal.
- No se generaron capturas reales de la app ni del video de referencia.

Por esta razón, no se modificó `LiveScreen.kt`: `TVenVIVO.md` exige evidencia visual antes de programar, y sin capturas equivalentes cualquier ajuste sería especulativo.

## Evidencia técnica disponible

| Verificación | Resultado | Impacto |
| --- | --- | --- |
| Búsqueda de `TVenVIVO.md` | Existe en la raíz del repo | Permite conocer los estados y criterios visuales esperados. |
| Búsqueda de implementación activa | `android-native/app/src/main/kotlin/com/ultratv/tv/nativeapp/ui/live/LiveScreen.kt` existe | Confirma la ruta principal del módulo de TV en vivo. |
| Búsqueda de documentación previa | `docs/live-tv/REDESIGN_AUDIT.md` y `docs/live-tv/NEXT_PROMPT.md` existen | Ya hay una auditoría funcional previa, pero no sustituye capturas reales. |
| `adb devices` | Falla: comando no encontrado | No se puede instalar, abrir ni capturar la app en dispositivo/emulador. |
| `emulator -list-avds` | Falla: comando no encontrado | No se puede iniciar AVD desde este entorno. |

## Estados principales que deben capturarse

Las capturas deben obtenerse en un entorno Android TV real o emulado, con contenido de prueba propio o sintético. No usar listas, canales, credenciales ni URLs IPTV reales dentro del repositorio.

| Estado | Captura requerida | Fotograma equivalente | Evidencia que debe medirse |
| --- | --- | --- | --- |
| Reproducción limpia | Video a pantalla completa, sin barras permanentes | Escena de reproducción sin overlays | Porcentaje de pantalla ocupado por video, ausencia de paneles, estado inmersivo, indicadores visibles. |
| Overlay inferior | Panel inferior con información del canal y EPG | Escena donde OK muestra información inferior | Altura del overlay, inicio/fin del gradiente, opacidad, posición de logo, número, nombre, programa actual, siguiente y progreso. |
| Lista de canales | Panel vertical izquierdo sobre video persistente | Escena de lista rápida de canales | Ancho del panel, filas visibles, alto de fila, margen izquierdo, separación logo/texto, diferencia entre canal activo y enfocado. |
| Categorías | Columna de categorías separada de lista | Escena de navegación por grupos/categorías | Ancho de columna, relación con lista, foco seleccionado, transición lateral y restauración al cerrar. |
| Guía EPG | Guía con canales y timeline | Escena de guía completa | Ancho de columna fija, alto de filas, duración visible, línea de ahora, proporción de celdas por tiempo, scroll horizontal/vertical. |
| Recientes | Fila horizontal sobre video | Escena de canales recientes, si aparece | Posición vertical, ancho de tarjetas, cantidad visible, foco horizontal, reproducción solo con OK. |
| Menú contextual | Panel lateral de acciones | Escena de menú por canal/programa | Lado de entrada, ancho, opacidad del scrim, orden de acciones, foco inicial y Back. |
| Buffering | Indicador de carga durante cambio o reconexión | Escena de espera/buffering | Tamaño, posición, si bloquea foco, si conserva overlay previo y video detrás. |
| Error | Mensaje sanitizado de fallo de reproducción | Escena de error o fallo simulado equivalente | Texto sin credenciales, ubicación, acción de reintento, Back, persistencia de foco. |

## Plantilla de medición por captura

Usar esta plantilla por cada estado capturado. Las medidas deben expresarse en porcentaje de pantalla y en píxeles reales de la captura.

| Campo | Valor |
| --- | --- |
| Estado |  |
| Archivo de captura |  |
| Resolución |  |
| Fotograma equivalente de la aplicación de referencia |  |
| Video visible |  |
| Panel principal: x/y/ancho/alto |  |
| Panel principal: porcentaje de pantalla |  |
| Alto de fila |  |
| Márgenes externos |  |
| Márgenes internos |  |
| Opacidad de fondo/scrim estimada |  |
| Gradiente: dirección e inicio/fin |  |
| Elemento con foco |  |
| Indicador de canal activo |  |
| Indicador de elemento enfocado |  |
| Animación observada |  |
| Duración estimada de animación |  |
| Acción Back |  |
| Acción OK |  |
| Acción OK largo/Menu |  |
| Diferencias frente a referencia |  |
| Acción requerida |  |

## Comparación preliminar basada solo en código actual

Esta tabla no reemplaza la comparación visual. Solo identifica puntos que deben confirmarse con capturas antes de ajustar código.

| Elemento | Implementación actual observable en código | Riesgo visual a validar | Ajuste permitido ahora |
| --- | --- | --- | --- |
| Reproductor | `LiveBackgroundPlayer` ocupa `fillMaxSize()` detrás de las capas | Confirmar que no se recrea al cambiar overlays y que no aparecen controles nativos | Ninguno sin captura. |
| Overlay inferior | `LiveChannelInfoBar` ocupa `fillMaxHeight(0.32f)` con gradiente vertical | Puede ser demasiado alto o con gradiente demasiado opaco frente a referencia | Ninguno sin captura. |
| Lista de canales | Panel izquierdo de `520.dp` con padding y panel de información a la derecha | Debe medirse si ocupa más pantalla que la referencia | Ninguno sin captura. |
| Categorías | Panel de `330.dp` junto a lista de `520.dp` | Debe verificarse relación de columnas, entrada lateral y foco inicial | Ninguno sin captura. |
| Guía EPG | `EpgOverlayGuide` usa un contenedor casi completo y `TimelineEpgGuide` | Debe verificarse si la guía conserva suficiente video visible y si la timeline coincide | Ninguno sin captura. |
| Recientes | `RecentChannelsOverlay` aparece abajo con gradiente y tarjetas horizontales | Debe confirmarse que la referencia usa una fila equivalente y no otro patrón | Ninguno sin captura. |
| Menú contextual | `LiveContextMenu` entra como panel al lado derecho | Debe medirse ancho, opacidad, foco inicial y animación | Ninguno sin captura. |
| Buffering/error | El estado existe en reducer y en textos de preview heredado, pero no hay captura real | Debe simularse red lenta/error y comprobar que no filtra URLs ni credenciales | Ninguno sin captura. |

## Protocolo recomendado para la siguiente ejecución con Android TV

1. Preparar un emulador o dispositivo Android TV con JDK 17, SDK Android y `adb` disponibles.
2. Compilar desde `android-native/` con `./gradlew assembleDebug`.
3. Instalar el APK debug en el dispositivo/emulador.
4. Cargar datos de prueba sintéticos, sin URLs reales versionadas.
5. Abrir el módulo de TV en vivo.
6. Capturar los estados solicitados con `adb exec-out screencap -p` o herramienta equivalente.
7. Tomar fotogramas equivalentes del video indicado en `TVenVIVO.md`, usándolos solo como referencia interna de medición.
8. Registrar cada medición en la plantilla anterior.
9. Solo después de completar la tabla, ajustar `LiveScreen.kt` y componentes relacionados.
10. Adjuntar las diferencias documentadas y los comandos reales usados.

## Comandos sugeridos para capturas futuras

```bash
adb devices
adb install -r android-native/app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.ultratv.tv.nativeapp 1
adb exec-out screencap -p > docs/live-tv/captures/live-clean.png
```

Los nombres concretos de paquete y APK deben validarse con el workspace actual antes de ejecutar. No guardar capturas con contenido sensible, URLs visibles, credenciales o datos personales.

## Decisión de implementación

No se aplicaron cambios de UI en `LiveScreen.kt` ni componentes relacionados durante esta etapa. La razón es deliberada: el requisito pide ajustar solo después de evidencia visual clara, y el entorno actual no permitió generar esa evidencia.
