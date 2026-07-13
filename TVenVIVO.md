# Prompt para reproducir fielmente el módulo de TV en vivo de la referencia

Quiero rehacer completamente el módulo de TV en vivo actual.

El resultado existente no cumple con la referencia visual ni con el comportamiento esperado. No quiero una reinterpretación, una variante creativa, una aproximación general ni un diseño inspirado libremente.

Quiero reproducir con máxima fidelidad los comportamientos observables, la estructura visual, el flujo de navegación, la disposición de elementos, el manejo del foco, las transiciones y las funciones que aparecen en el siguiente material de referencia:

**Video de referencia:**
`https://youtu.be/cY3k7Z-mrGU?si=me0TW1hErBF-YNNf`

A partir de ahora, denomina este producto únicamente como:

**“la aplicación de referencia”**

No menciones marcas comerciales dentro del código, documentación, nombres de clases, comentarios, recursos, pruebas ni interfaz.

No utilices código, recursos gráficos, logotipos, iconos, tipografías propietarias ni archivos extraídos de terceros. La implementación debe ser propia, pero debe reproducir fielmente la experiencia observable descrita y mostrada en el video.

---

# 1. Regla principal

No rediseñar.

No modernizar.

No simplificar.

No sustituir los flujos por componentes genéricos.

No cambiar la posición de paneles.

No convertir la interfaz en una aplicación móvil ampliada.

No inventar nuevas interacciones.

No aplicar patrones de navegación que no aparezcan en la referencia.

No entregar una aproximación visual.

El objetivo es reproducir fielmente:

* La composición de pantalla.
* El orden de las capas.
* El comportamiento del reproductor.
* La posición relativa de cada panel.
* El flujo entre reproducción, información, canales, categorías y guía.
* La navegación con control remoto.
* La restauración del foco.
* La velocidad de las transiciones.
* La persistencia del video detrás de los menús.
* Los estados visibles del canal activo, fila enfocada y categoría seleccionada.
* El comportamiento del botón Back.
* La respuesta a pulsación corta y pulsación larga de OK.

Cuando exista una diferencia entre una decisión convencional de Android TV y lo observado en la referencia, priorizar lo observado en la referencia.

---

# 2. Trabajo previo obligatorio

Antes de modificar código:

1. Reproduce y analiza completamente el video de referencia.
2. Divide el video en escenas funcionales.
3. Identifica cada vista, overlay, panel, menú y transición.
4. Registra el momento aproximado en que aparece cada interacción.
5. Crea una tabla de estados.
6. Crea un mapa de navegación con el control remoto.
7. Identifica qué elementos permanecen fijos y cuáles se desplazan.
8. Identifica qué panel aparece desde cada dirección.
9. Identifica qué elemento recupera el foco al cerrar cada capa.
10. Compara la implementación actual con la referencia.
11. Enumera todas las diferencias encontradas.
12. Propón los archivos que deben ser reemplazados, no solo retocados.

No empieces a implementar hasta completar este análisis.

---

# 3. Evidencia requerida antes de programar

Entrega primero un documento de análisis con esta estructura:

## 3.1 Escenas observadas

Para cada escena del video:

* Marca de tiempo aproximada.
* Estado inicial.
* Botón presionado.
* Elementos visibles.
* Posición de cada elemento.
* Elemento con foco.
* Transición ejecutada.
* Estado final.
* Acción de Back.
* Acción de OK.
* Acción de pulsación larga de OK.
* Comportamiento de arriba, abajo, izquierda y derecha.

## 3.2 Comparación con la implementación actual

Crear una tabla con:

| Elemento | Referencia | Implementación actual | Diferencia | Acción requerida |
| -------- | ---------- | --------------------- | ---------- | ---------------- |

Incluir al menos:

* Reproductor.
* Overlay de información.
* Lista de canales.
* Categorías.
* Guía EPG.
* Panel de acciones.
* Canales recientes.
* Indicadores de foco.
* Gradientes.
* Tamaños.
* Espaciados.
* Animaciones.
* Manejo de Back.
* Manejo de OK.
* Foco al abrir.
* Foco al cerrar.
* Persistencia del video.
* Cambio de canal.
* Buffering.
* Estados sin EPG.

## 3.3 Matriz de entradas del control remoto

Crear una tabla por cada estado:

| Estado | Arriba | Abajo | Izquierda | Derecha | OK | OK largo | Back |
| ------ | ------ | ----- | --------- | ------- | -- | -------- | ---- |

No asumir que una tecla hace lo mismo en todas las vistas.

---

# 4. Arquitectura de la pantalla

La pantalla de TV en vivo debe construirse como una sola superficie persistente compuesta por capas.

Orden obligatorio:

1. Superficie del reproductor.
2. Capa de oscurecimiento contextual.
3. Overlay informativo inferior.
4. Lista o guía.
5. Panel de categorías.
6. Panel contextual.
7. Diálogos.
8. Mensajes temporales.
9. Indicadores de buffering o error.

El reproductor debe permanecer montado mientras se abren o cierran overlays.

No navegar a una nueva pantalla para mostrar:

* Información del canal.
* Lista rápida.
* Categorías.
* Guía.
* Menú contextual.
* Canales recientes.

Estas funciones deben aparecer sobre el reproductor.

No recrear el reproductor al cambiar de overlay.

No reiniciar el stream al abrir o cerrar menús.

---

# 5. Pantalla completa

Al entrar a TV en vivo:

* Mostrar el último canal reproducido.
* Mantener el video a pantalla completa.
* No dejar barras permanentes.
* No mostrar encabezados generales de la aplicación.
* No mostrar navegación inferior.
* No mostrar un panel lateral hasta que el usuario lo solicite.
* Aplicar modo inmersivo.
* Mostrar información temporal del canal al iniciar reproducción.
* Ocultar automáticamente esa información.
* Dejar únicamente el video visible.

El primer frame disponible debe reemplazar el fondo de carga sin cambiar de pantalla.

---

# 6. Overlay informativo inferior

Al presionar OK en reproducción limpia, mostrar el overlay inferior de información.

Debe reproducir fielmente la composición de la referencia:

* Panel ancho en la parte inferior.
* Video visible por encima.
* Oscurecimiento gradual desde abajo.
* Información alineada horizontalmente.
* Logotipo del canal.
* Número y nombre del canal.
* Programa actual.
* Horario.
* Programa siguiente.
* Barra de progreso.
* Hora actual.
* Indicadores secundarios.

El overlay no debe sentirse como una tarjeta flotante independiente.

Debe integrarse con un gradiente inferior.

No utilizar un diálogo centrado.

No reducir el tamaño del reproductor.

No pausar el canal.

## Comportamiento

* OK desde pantalla limpia: abre overlay.
* Back: cierra overlay.
* Inactividad: cierra overlay.
* Cualquier tecla válida reinicia el temporizador.
* El cierre debe restaurar reproducción completamente limpia.
* El foco inicial debe coincidir con el comportamiento de la referencia.

---

# 7. Lista de canales

La lista de canales debe aparecer como una capa vertical sobre el lado izquierdo.

No debe ocupar toda la pantalla.

Debe mantener una parte importante del video visible.

Debe incluir:

* Categoría activa.
* Filas de canales.
* Logotipos.
* Número.
* Nombre.
* Programa actual.
* Horario.
* Progreso.
* Estado favorito.
* Estado Catch-up cuando exista.
* Identificación separada de:

  * Canal actualmente reproducido.
  * Canal actualmente enfocado.

Estos dos estados no deben confundirse.

## Foco

La fila enfocada debe reproducir fielmente:

* Fondo de selección.
* Contraste.
* Tamaño.
* Espaciado.
* Alineación.
* Elementos adicionales visibles.
* Transición de entrada y salida del foco.

No usar un borde genérico grueso si no aparece así en la referencia.

No usar escalado excesivo.

No mover el video.

## Navegación

* Arriba y abajo: cambian de fila.
* OK: reproduce el canal seleccionado.
* Izquierda: abre categorías cuando corresponda.
* Derecha: realiza la acción observada en la referencia.
* OK largo: abre menú contextual.
* Back: cierra la lista.
* Al cerrar, restaurar reproducción limpia.
* Al volver a abrir, enfocar el canal activo o la posición observada en la referencia.

No cambiar de canal solamente por mover el foco.

El cambio de canal ocurre al confirmar con OK, salvo que el video demuestre un comportamiento diferente.

---

# 8. Panel de categorías

El panel de categorías debe entrar desde la izquierda.

Debe aparecer como una columna adicional y modificar la composición de la lista de canales de la misma manera que la referencia.

No reemplazar la lista completa.

No abrir una pantalla independiente.

Debe mostrar:

* Favoritos.
* Todos los canales.
* Recientes.
* Categorías de la fuente.
* Otras agrupaciones disponibles.

## Estados distintos

Diferenciar visualmente:

* Categoría activa.
* Categoría enfocada.
* Categoría deshabilitada.
* Categoría sin canales.

## Navegación

* Arriba y abajo: cambian categoría enfocada.
* OK: activa la categoría.
* Derecha: devuelve el foco a la lista de canales.
* Back: cierra el panel de categorías.
* Al cerrar, la lista debe recuperar exactamente su posición y foco anterior.

Al cambiar categoría:

* No mostrar una pantalla negra.
* No cerrar todo.
* No perder el canal activo.
* No reiniciar el reproductor.
* Actualizar la lista con la misma transición observada en la referencia.

---

# 9. Guía EPG

La guía debe replicar la estructura observable de la aplicación de referencia.

Debe incluir:

* Columna fija de canales.
* Encabezado fijo de tiempo.
* Celdas de programas.
* Horas proporcionales.
* Programa actual.
* Programas anteriores.
* Programas futuros.
* Línea de hora actual.
* Información ampliada del elemento enfocado.
* Indicadores de Catch-up cuando existan.
* Canal activo claramente visible.
* Programa enfocado claramente visible.

## Desplazamiento

* La columna de canales debe permanecer fija.
* El encabezado temporal debe permanecer fijo.
* La cuadrícula debe desplazarse horizontalmente.
* Las filas deben desplazarse verticalmente.
* El movimiento debe ser progresivo y suave.
* El foco debe permanecer visible.
* Al cambiar verticalmente de canal, conservar la posición temporal.
* Al cambiar horizontalmente, desplazar solo lo necesario.

No usar una cuadrícula genérica que salte una pantalla completa por cada pulsación.

No centrar siempre el programa enfocado si la referencia no lo hace.

## Acciones

* Programa en vivo + OK: reproducir.
* Programa pasado con Catch-up + OK: reproducir desde archivo o abrir acción correspondiente.
* Programa futuro + OK: mostrar acciones aplicables.
* OK largo: menú contextual.
* Back: regresar al nivel anterior sin cerrar más capas de las necesarias.

---

# 10. Canales recientes

Implementar la fila horizontal de canales recientes como aparece en la referencia.

Debe mostrarse sobre el video.

Cada elemento debe incluir los datos visibles en la referencia.

La tarjeta enfocada debe distinguirse mediante:

* Cambio de fondo.
* Cambio de tamaño controlado.
* Contraste.
* Información adicional cuando corresponda.
* Transición corta.

Navegación:

* Izquierda y derecha: recorren recientes.
* OK: reproduce.
* Back: cierra.
* El canal actual debe estar identificado.
* Al seleccionar otro canal, cerrar la fila y mostrar información del nuevo canal.

---

# 11. Menú contextual

El menú contextual debe aparecer desde el lado correspondiente observado en la referencia.

No usar un diálogo centrado.

No utilizar un menú emergente pequeño.

Debe ser un panel vertical de altura completa o casi completa, según la referencia.

Mostrar únicamente acciones válidas.

Posibles acciones:

* Reproducir.
* Favorito.
* Quitar favorito.
* Información.
* Guía del canal.
* Catch-up.
* Grabar.
* Recordatorio.
* Opciones del canal.
* Ocultar.
* Bloquear.
* Asignar EPG.
* Buscar contenido relacionado.

## Comportamiento

* Arriba y abajo: navegan acciones.
* OK: ejecuta.
* Back: cierra únicamente el menú.
* Al cerrar, restaurar el foco exacto que lo abrió.
* No cerrar la lista o la guía detrás.
* No reiniciar el video.

---

# 12. Transiciones

Las transiciones deben medirse y reproducirse de forma consistente.

No usar animaciones predeterminadas sin ajustar.

No usar rebotes.

No usar elasticidad.

No usar escalados exagerados.

No aplicar animaciones decorativas que no aparezcan en la referencia.

## Paneles izquierdos

* Entrada horizontal desde la izquierda.
* Aceleración inicial rápida.
* Desaceleración al final.
* Scrim progresivo.
* Video fijo.
* Duración corta.

## Paneles derechos

* Entrada horizontal desde la derecha.
* Oscurecimiento gradual del contenido posterior.
* No desplazar el reproductor.
* No cerrar el panel anterior.

## Overlay inferior

* Entrada desde abajo.
* Fundido simultáneo.
* Gradiente progresivo.
* Salida hacia abajo con fundido.

## Foco

* Cambio inmediato pero suavizado.
* Duración breve.
* Sin retraso perceptible.
* No bloquear pulsaciones repetidas.
* Mantener respuesta rápida del D-pad.

## Cambio de canal

* No ejecutar una transición de pantalla completa.
* No navegar a otra actividad.
* Conservar el reproductor.
* Mostrar estado de carga discreto.
* Sustituir el stream.
* Mostrar información del nuevo canal.
* Evitar parpadeo negro innecesario.

---

# 13. Medición visual

No uses valores arbitrarios.

Extrae del video y de las capturas:

* Porcentaje de pantalla ocupado por la lista.
* Porcentaje ocupado por categorías.
* Altura del overlay inferior.
* Altura de filas.
* Tamaño relativo de logotipos.
* Separación entre columnas.
* Márgenes externos.
* Posición del título.
* Posición de horarios.
* Grosor de la barra de progreso.
* Intensidad del oscurecimiento.
* Distancia de desplazamiento de paneles.
* Tamaño del foco.
* Cantidad de elementos visibles.

Documentar cada medida como:

* Valor observado aproximado.
* Valor implementado.
* Justificación.
* Captura de comparación.

Usar proporciones de pantalla, `dp` y `sp`, pero preservar la composición relativa.

---

# 14. Comparación visual obligatoria

Después de implementar cada vista:

1. Captura una imagen de la aplicación.
2. Captura el fotograma equivalente de la referencia.
3. Colócalas lado a lado.
4. Identifica diferencias.
5. Corrige:

   * Posición.
   * Tamaño.
   * Espaciado.
   * Opacidad.
   * Tipografía.
   * Alineación.
   * Foco.
   * Estado activo.
   * Gradientes.
   * Animación.
6. Repite hasta que las diferencias sean mínimas.

No considerar terminada una vista solo porque contiene los mismos elementos.

La composición y el comportamiento también deben coincidir.

---

# 15. Estado de interfaz

Utilizar una máquina de estados explícita.

Estados mínimos:

* `FullscreenPlayback`
* `ProgramInfoVisible`
* `RecentChannelsVisible`
* `ChannelListVisible`
* `CategoryPanelVisible`
* `EpgVisible`
* `ProgramDetailsVisible`
* `ContextMenuVisible`
* `Buffering`
* `PlaybackError`

Además, guardar:

* Canal activo.
* Canal enfocado.
* Categoría activa.
* Categoría enfocada.
* Programa enfocado.
* Vista anterior.
* Elemento que abrió la capa actual.
* Foco que debe restaurarse al cerrar.
* Posición vertical de cada categoría.
* Posición horizontal de la guía.
* Temporizador del overlay.
* Estado de pulsación larga.

No implementar la navegación mediante muchos booleanos independientes sin jerarquía.

---

# 16. Manejo del botón Back

Back debe cerrar una sola capa por pulsación.

Orden esperado:

1. Diálogo.
2. Menú contextual.
3. Panel de categorías.
4. Detalles del programa.
5. Guía o lista.
6. Recientes.
7. Overlay informativo.
8. Reproducción limpia.
9. Salir del módulo únicamente desde reproducción limpia.

No cerrar varias capas a la vez.

No salir accidentalmente.

No perder el foco previo.

---

# 17. Rendimiento

La implementación debe sentirse inmediata en dispositivos Android TV de gama media.

Requisitos:

* Una sola instancia principal del reproductor.
* Overlays montados sobre la misma pantalla.
* Listas virtualizadas.
* Guía EPG virtualizada.
* Caché de logotipos.
* Precarga de información del canal anterior y siguiente.
* Debounce para cambios rápidos.
* No reiniciar reproducción por cambios de foco.
* No reconstruir toda la guía al actualizar la hora.
* No bloquear el hilo principal.
* Animaciones fluidas.
* Sin parpadeos.
* Sin pérdida del foco por recomposición.

---

# 18. Prohibiciones técnicas

No entregar:

* Una pantalla estática.
* Un prototipo sin conexión al reproductor.
* Una guía falsa.
* Datos hardcoded como solución final.
* Navegación táctil adaptada.
* Componentes móviles sin comportamiento TV.
* Una lista genérica de Compose sin control de foco.
* Un reproductor que se recree al abrir paneles.
* Navegación entre actividades para cada overlay.
* Animaciones predeterminadas sin calibrar.
* Una interfaz “parecida” pero con otro flujo.
* Un diseño alternativo que el agente considere mejor.
* Cambios visuales no solicitados.
* Menús permanentes.
* Encabezados adicionales.
* Botones visibles que no existan en la referencia.
* Colores creativos.
* Gradientes distintos.
* Tarjetas excesivamente redondeadas.
* Sombras o escalas exageradas.

---

# 19. Criterios de aceptación

La tarea no se considera completada hasta verificar lo siguiente:

* El reproductor permanece visible y activo detrás de los overlays.
* La lista de canales aparece en la misma zona y proporción que la referencia.
* Las categorías aparecen desde la dirección correcta.
* La guía conserva la estructura visual observada.
* La información inferior ocupa una proporción equivalente.
* El canal activo y el canal enfocado están claramente diferenciados.
* El foco inicial de cada vista es correcto.
* El foco se restaura correctamente al cerrar.
* Back cierra exactamente una capa.
* OK ejecuta la acción correcta según el estado.
* OK largo abre el menú contextual.
* Las transiciones tienen dirección, duración y curva coherentes con la referencia.
* No hay saltos visuales.
* No hay pantallas negras innecesarias.
* No se recrea el reproductor.
* No cambia el canal al mover solamente el foco.
* Los canales recientes funcionan.
* Favoritos funcionan.
* La guía EPG funciona.
* Catch-up funciona cuando el proveedor lo permite.
* El progreso de programas se actualiza.
* La línea de tiempo actual se actualiza.
* La interfaz funciona completamente con D-pad.
* La implementación visual fue comparada con fotogramas equivalentes.
* Se corrigieron las diferencias principales antes de cerrar la tarea.

---

# 20. Forma de trabajo requerida

Ejecuta el trabajo por etapas:

## Etapa 1: auditoría

* Analizar referencia.
* Analizar implementación actual.
* Documentar diferencias.
* Definir máquina de estados.
* Definir mapa del control remoto.

## Etapa 2: estructura

* Corregir arquitectura.
* Garantizar una sola instancia del reproductor.
* Implementar jerarquía de capas.
* Implementar restauración de foco.

## Etapa 3: reproducción e información

* Pantalla completa.
* Overlay inferior.
* Cambio de canal.
* Buffering.
* Errores.

## Etapa 4: canales y categorías

* Lista de canales.
* Panel de categorías.
* Foco.
* Navegación.
* Canal activo frente a canal enfocado.

## Etapa 5: guía

* Columna de canales.
* Línea de tiempo.
* Celdas.
* Desplazamiento.
* Detalles.
* Catch-up.

## Etapa 6: recientes y menú contextual

* Canales recientes.
* Favoritos.
* Menú de acciones.
* Restauración de foco.

## Etapa 7: comparación visual

* Capturas.
* Comparaciones.
* Corrección de medidas.
* Corrección de transiciones.
* Corrección de estados.

No avances a la siguiente etapa si la anterior no funciona correctamente.

---

# 21. Entrega final

La entrega debe incluir:

* Código funcional.
* Lista de archivos modificados.
* Explicación de la máquina de estados.
* Mapa final del control remoto.
* Tabla de transiciones.
* Capturas comparativas.
* Lista de diferencias pendientes.
* Pruebas ejecutadas.
* Pruebas de navegación con D-pad.
* Pruebas de restauración de foco.
* Pruebas de cambio rápido de canales.
* Pruebas de guía EPG con muchos canales.
* Pruebas de rendimiento.
* Confirmación de que el reproductor no se recrea al abrir overlays.

No marcar la tarea como completada si todavía existen diferencias visibles importantes respecto al material de referencia.
