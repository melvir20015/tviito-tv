# Módulo de TV en Vivo

## 1. Objetivo general

Implementar en la aplicación Android TV una experiencia completa de reproducción y navegación de TV en vivo basada en una interfaz moderna, rápida, oscura, limpia y controlada principalmente con el D-pad del control remoto.

La idea central del módulo es la siguiente:

> El canal en reproducción debe permanecer activo como fondo mientras el usuario abre listas, guía, información, categorías y menús.

No se debe reemplazar abruptamente el reproductor por pantallas completamente separadas. La mayoría de las funciones deben aparecer como capas superpuestas animadas sobre el video en vivo.

El módulo debe incluir:

- Reproducción a pantalla completa.
- Barra de información del canal y programa.
- Lista rápida de canales.
- Canales recientes.
- Guía electrónica de programación.
- Navegación por categorías o grupos.
- Información del programa actual y siguiente.
- Menús contextuales.
- Favoritos.
- Historial o canales vistos recientemente.
- Funciones de Catch-up cuando estén disponibles.
- Indicadores de progreso de los programas.
- Navegación completamente optimizada para control remoto.
- Transiciones rápidas, suaves y consistentes.
- Persistencia del canal que se está reproduciendo mientras se navega.

La interfaz debe priorizar la televisión. El contenido en reproducción es siempre el elemento principal y los controles deben desaparecer automáticamente cuando el usuario deja de interactuar.

---

## 2. Plataforma y orientación

Implementar para:

- Android TV.
- Google TV.
- Fire TV cuando la arquitectura lo permita.
- Orientación exclusivamente horizontal.
- Resoluciones principales:
  - 1280 × 720.
  - 1920 × 1080.
  - 3840 × 2160.
- Escalado proporcional mediante `dp`, `sp` y márgenes seguros.

Toda la interfaz debe poder controlarse sin pantalla táctil mediante:

- D-pad arriba.
- D-pad abajo.
- D-pad izquierda.
- D-pad derecha.
- Botón OK o Enter.
- Botón Back.
- Pulsación larga de OK.
- Botones multimedia cuando estén disponibles.

---

## 3. Lenguaje visual general

Utilizar una estética oscura y minimalista.

### 3.1 Fondo

El reproductor de video debe ocupar siempre el 100 % de la pantalla.

Cuando aparezca una capa:

- Mantener el video visible.
- Aplicar una capa oscura semitransparente únicamente donde sea necesario.
- No desenfocar excesivamente el video.
- El contenido debe continuar reproduciéndose sin pausas.
- Evitar fondos sólidos que oculten por completo el canal, excepto en ajustes o pantallas donde sea obligatorio.

### 3.2 Colores

Usar como base:

- Negro profundo para fondos: `#08090B`.
- Gris muy oscuro para paneles: `#14161A`.
- Gris secundario: `#22252B`.
- Blanco para texto principal: `#F4F5F7`.
- Gris claro para texto secundario: `#AEB3BC`.
- Gris tenue para información deshabilitada: `#6F747C`.
- Color de enfoque principal configurable, con un azul frío como valor inicial.
- Rojo únicamente para grabación, errores o acciones destructivas.
- Verde únicamente para estados correctos o reproducción activa cuando sea necesario.

Los paneles superpuestos deben usar entre 88 % y 96 % de opacidad.

### 3.3 Tipografía

Utilizar una tipografía sans-serif clara y legible a distancia.

Jerarquía recomendada para 1080p:

- Título de programa: 24–28 sp.
- Nombre del canal: 20–24 sp.
- Texto principal de listas: 18–22 sp.
- Horarios: 15–18 sp.
- Descripciones: 16–18 sp.
- Información secundaria: 14–16 sp.

No utilizar textos demasiado pequeños.

### 3.4 Bordes y formas

- Esquinas ligeramente redondeadas.
- Radio aproximado: 4–8 dp.
- Sombras discretas.
- No abusar de tarjetas individuales.
- Las filas deben sentirse como parte de una lista continua.
- El foco debe ser evidente mediante:
  - Fondo iluminado.
  - Cambio de contraste.
  - Escala mínima.
  - Borde o indicador lateral.
- No depender únicamente del color.

---

## 4. Jerarquía visual y capas

Organizar las capas en el siguiente orden:

1. Reproductor de video.
2. Scrim o sombreado contextual.
3. Barra de información del programa.
4. Lista rápida o guía.
5. Panel de categorías.
6. Menú contextual.
7. Diálogos de confirmación.
8. Mensajes temporales y errores.

Cada capa debe cerrarse en orden inverso al presionar Back.

Ejemplo:

- Si el menú contextual está abierto, Back cierra solo el menú contextual.
- Otro Back cierra la guía o lista.
- Otro Back vuelve a reproducción limpia.
- No salir de la aplicación accidentalmente desde una capa interna.

---

## 5. Estado principal: reproducción a pantalla completa

Al abrir TV en vivo:

- Restaurar el último canal reproducido.
- Mostrar inmediatamente el reproductor.
- No mostrar menús permanentes.
- Mantener una pantalla limpia.
- Ocultar cualquier barra del sistema.
- Usar modo inmersivo.
- Mantener el aspecto original del video.
- Permitir configurar:
  - Ajustar.
  - Rellenar.
  - Estirar.
  - Zoom.
  - Original.

Después de cargar el canal:

- Mostrar brevemente la barra de información.
- Ocultarla automáticamente después de aproximadamente 4–6 segundos.
- Mantener únicamente el video.

Cuando el usuario cambia de canal:

- Cambiar el stream sin abandonar el reproductor.
- Mostrar la información del nuevo canal.
- Mostrar un indicador de carga discreto si el stream tarda.
- No usar una pantalla negra completa salvo que no exista ningún frame disponible.
- Conservar el último frame válido cuando técnicamente sea posible.

---

## 6. Barra de información del canal

Al presionar OK durante la reproducción, mostrar una barra informativa grande en la parte inferior.

Debe incluir:

- Logotipo del canal.
- Número del canal.
- Nombre del canal.
- Nombre del programa actual.
- Hora de inicio.
- Hora de finalización.
- Tiempo actual.
- Barra de progreso del programa.
- Porcentaje o progreso visual.
- Nombre del siguiente programa.
- Horario del siguiente programa.
- Indicadores opcionales:
  - Favorito.
  - Grabación.
  - Catch-up.
  - Subtítulos.
  - Resolución.
  - Calidad del stream.
  - Audio alternativo.
  - Bloqueo parental.

### 6.1 Diseño de la barra

- Debe entrar desde la parte inferior.
- Ocupar aproximadamente entre 25 % y 34 % de la altura.
- Fondo oscuro semitransparente.
- Gradiente superior para integrarse con el video.
- Logotipo a la izquierda.
- Información principal en el centro.
- Hora y estados a la derecha.
- Barra de progreso fina pero visible.
- El título actual debe tener mayor peso visual que el resto.

### 6.2 Comportamiento

- OK abre la barra.
- Otro OK puede abrir la fila de canales recientes o ejecutar la acción configurada.
- Back cierra la barra.
- La barra se cierra automáticamente por inactividad.
- Cualquier movimiento del D-pad reinicia el temporizador de ocultamiento.

---

## 7. Fila de canales recientes

Desde la reproducción o desde la barra de información, mostrar una fila horizontal de canales vistos recientemente.

Debe aparecer sobre la zona inferior del video.

Cada elemento debe contener:

- Logotipo del canal.
- Nombre corto.
- Indicador de canal actualmente activo.
- Programa actual opcional.
- Barra de progreso opcional.

### 7.1 Diseño

- Tarjetas horizontales compactas.
- Entre 5 y 8 elementos visibles en 1080p.
- La tarjeta enfocada debe:
  - Aumentar ligeramente de tamaño.
  - Elevarse visualmente.
  - Cambiar el fondo.
  - Mostrar texto adicional.
- Las tarjetas no enfocadas deben permanecer visibles pero con menor contraste.

### 7.2 Navegación

- Izquierda y derecha recorren canales recientes.
- OK reproduce el canal seleccionado.
- Arriba puede abrir acciones relacionadas.
- Abajo puede regresar a la barra de información.
- Back vuelve al video.

Al seleccionar otro canal:

- Cerrar la fila.
- Cambiar el stream.
- Mostrar la barra informativa del nuevo canal.
- No cambiar de pantalla ni reiniciar toda la actividad.

---

## 8. Lista rápida de canales

Implementar una lista vertical superpuesta para navegar canales sin abrir toda la guía.

### 8.1 Posición

- Panel lateral izquierdo.
- Aproximadamente 35–45 % del ancho.
- El resto de la pantalla conserva el video visible.
- Agregar un degradado de oscuro a transparente hacia el centro.

### 8.2 Encabezado

Mostrar:

- Nombre de la categoría actual.
- Nombre de la lista o fuente, si existen varias.
- Cantidad de canales.
- Hora actual.
- Icono para cambiar de categoría.

### 8.3 Filas

Cada canal debe mostrar:

- Número.
- Logotipo.
- Nombre.
- Programa actual.
- Horario actual.
- Progreso del programa.
- Icono de favorito.
- Indicador Catch-up.
- Indicador de reproducción si es el canal activo.

### 8.4 Foco

La fila seleccionada debe:

- Tener un fondo claramente iluminado.
- Mostrar texto completo.
- Mostrar el programa siguiente o información adicional.
- Permanecer centrada verticalmente cuando sea posible.
- Desplazar la lista de manera suave.

### 8.5 Comportamiento

- Arriba y abajo cambian la selección.
- OK reproduce el canal seleccionado.
- Izquierda abre categorías.
- Derecha puede mostrar más información del programa o cerrar la lista, según el contexto.
- Pulsación larga de OK abre el menú contextual.
- Back cierra la lista y regresa al video.
- Mantener visible cuál canal está reproduciéndose aunque el foco esté en otro.

### 8.6 Vista previa

Mientras el usuario solo navega por la lista:

- No cambiar automáticamente el canal con cada movimiento.
- Mantener el canal actual reproduciéndose.
- Cambiar de canal únicamente al presionar OK.
- Actualizar la sección informativa según la fila enfocada.

---

## 9. Panel de categorías o grupos

Al presionar izquierda desde la lista o guía, abrir un panel adicional desde el borde izquierdo.

Debe mostrar categorías como:

- Favoritos.
- Todos los canales.
- Canales recientes.
- Historial.
- Categorías proporcionadas por la lista.
- Fuentes o playlists, cuando haya varias.
- Categorías personalizadas.

### 9.1 Diseño

- Panel vertical más angosto.
- Fondo casi opaco.
- Icono y nombre de cada categoría.
- Contador opcional de canales.
- Separadores discretos.
- Categoría activa claramente identificada.
- Foco independiente del canal seleccionado.

### 9.2 Transición

Al abrirlo:

- El panel de categorías entra desde la izquierda.
- La lista de canales se desplaza ligeramente hacia la derecha.
- No debe aparecer abruptamente.
- El video permanece visible en la zona restante.
- Duración aproximada: 180–240 ms.

Al cambiar de categoría:

- Actualizar la lista sin pantalla de carga completa.
- Mantener el foco en una posición lógica.
- Usar un fundido o reemplazo suave de filas.
- Mostrar un indicador pequeño únicamente si la carga tarda.

Al cerrar:

- El panel se desliza hacia la izquierda.
- La lista recupera su posición original.

---

## 10. Guía electrónica de programación

Implementar una guía de programación completa en forma de cuadrícula.

Debe conservar el video en reproducción mediante una de estas composiciones:

- Video como fondo parcialmente visible.
- Ventana de previsualización en la parte superior.
- Guía ocupando la mayor parte de la pantalla con una capa oscura.

La navegación por programación debe sentirse integrada con la reproducción y no como una sección completamente desconectada.

### 10.1 Estructura

La guía debe contener:

#### Columna fija izquierda

- Número del canal.
- Logotipo.
- Nombre.
- Indicador de favorito.
- Indicador del canal activo.

#### Encabezado horizontal

- Fecha actual.
- Día.
- Franja horaria.
- Marcas cada 30 minutos.
- Hora actual.
- Posibilidad de navegar a días anteriores y siguientes cuando exista información.

#### Cuadrícula de programas

Cada programa debe mostrar:

- Título.
- Hora de inicio.
- Hora de finalización.
- Estado:
  - En vivo.
  - Futuro.
  - Finalizado.
  - Disponible mediante Catch-up.
  - Programado para grabar.
  - Recordatorio configurado.

#### Línea de hora actual

- Dibujar una línea vertical claramente visible.
- Debe cruzar todas las filas.
- Actualizarla en tiempo real.
- El programa actual debe mostrar progreso proporcional.

### 10.2 Dimensiones

Para 1080p:

- Filas de 64–82 dp.
- Columna de canal de 260–340 dp.
- Escala horizontal proporcional al tiempo.
- Los programas cortos deben conservar una anchura mínima que permita enfocarlos.
- Evitar superposición de textos.

### 10.3 Navegación

- Arriba y abajo cambian de canal.
- Izquierda y derecha cambian de programa.
- OK:
  - Reproduce si el programa está en vivo.
  - Abre detalles o Catch-up si ya terminó.
  - Abre opciones si es futuro.
- Pulsación larga de OK abre acciones contextuales.
- Back vuelve al nivel anterior.
- Mantener siempre visible el programa enfocado.

### 10.4 Seguimiento del foco

Cuando el foco se mueve:

- La cuadrícula debe desplazarse suavemente.
- La columna de canales permanece fija.
- El encabezado de horarios permanece fijo.
- La vista debe adelantar o retroceder horizontalmente solo lo necesario.
- Evitar saltos bruscos.
- Conservar la posición de tiempo al cambiar verticalmente de canal.

---

## 11. Información ampliada del programa

Cuando un programa está enfocado, mostrar una zona de información ampliada.

Puede aparecer:

- En la parte superior.
- En la parte inferior.
- En un panel lateral.
- Integrada dentro de la guía.

Debe contener:

- Título.
- Canal.
- Horario.
- Duración.
- Descripción.
- Género.
- Año.
- Clasificación.
- Temporada y episodio cuando existan.
- Iconos de Catch-up, grabación o recordatorio.
- Progreso del programa.
- Imagen de fondo o póster cuando exista, sin desplazar la información esencial.

No mostrar campos vacíos. La distribución debe reorganizarse cuando falten datos.

---

## 12. Menú contextual

Al mantener presionado OK sobre un canal o programa, abrir un menú contextual.

### 12.1 Posición

- Panel vertical desde el lado derecho.
- Video o guía visible detrás.
- Fondo oscuro casi opaco.
- Ancho aproximado de 28–36 %.

### 12.2 Acciones posibles

Mostrar solamente acciones válidas para el elemento seleccionado:

- Reproducir.
- Añadir a favoritos.
- Quitar de favoritos.
- Ver desde el inicio.
- Ver Catch-up.
- Grabar.
- Programar grabación.
- Crear recordatorio.
- Cancelar recordatorio.
- Información.
- Buscar programas relacionados.
- Ocultar canal.
- Bloquear canal.
- Cambiar nombre visible.
- Asignar información EPG.
- Abrir opciones del canal.
- Abrir guía del canal.
- Ver programas futuros.

### 12.3 Interacción

- Arriba y abajo recorren acciones.
- OK ejecuta la acción.
- Back cierra el menú.
- El foco debe iniciar en la acción más probable.
- Las acciones destructivas deben requerir confirmación.
- No cerrar la guía completa al cerrar el menú.

### 12.4 Animación

- Entrada desde la derecha.
- Duración: 180–240 ms.
- Fondo general ligeramente oscurecido.
- Salida hacia la derecha.
- Sin rebotes exagerados.

---

## 13. Cambio de canal directo

Permitir cambiar canales desde reproducción con D-pad arriba y abajo.

### 13.1 Comportamiento sugerido

- Arriba: canal siguiente.
- Abajo: canal anterior.
- Mostrar una tarjeta informativa breve.
- Aplicar un retraso corto para evitar múltiples cargas si el usuario pulsa rápidamente.
- Si el usuario recorre varios canales rápidamente:
  - Actualizar la información visual inmediatamente.
  - Iniciar la reproducción únicamente cuando deje de pulsar durante aproximadamente 250–450 ms.
- Evitar cargar varios streams distintos durante una navegación rápida.

### 13.2 Transición visual

- No usar animaciones llamativas entre streams.
- Aplicar un fundido muy breve.
- Mantener el fondo oscuro solo mientras llega el primer frame.
- Mostrar spinner después de un pequeño retraso, no instantáneamente.
- Si el nuevo canal falla, volver al anterior cuando sea apropiado.

---

## 14. Favoritos

Permitir marcar o desmarcar canales como favoritos desde:

- Lista rápida.
- Guía.
- Menú contextual.
- Barra de información.

Al cambiar el estado:

- Actualizar el icono inmediatamente.
- Usar una microanimación de escala o relleno.
- No mostrar un diálogo modal innecesario.
- Mostrar un mensaje breve:
  - “Añadido a favoritos”.
  - “Eliminado de favoritos”.
- Mantener la posición del foco.

La categoría Favoritos debe actualizarse en tiempo real.

---

## 15. Historial y canales recientes

Guardar localmente:

- Últimos canales reproducidos.
- Fecha y hora.
- Duración aproximada de visualización.
- Último programa visto.
- Última posición cuando aplique.
- Origen o playlist.
- Categoría.

Diferenciar:

- **Recientes:** últimos canales abiertos.
- **Historial:** registro cronológico más amplio.

Evitar duplicados consecutivos. Si un canal ya existe en recientes, moverlo al inicio.

---

## 16. Catch-up

Cuando un programa anterior esté disponible:

- Mostrar un icono claramente reconocible.
- Permitir seleccionarlo desde la guía.
- Al presionar OK:
  - Abrir directamente la reproducción, o
  - Mostrar opciones si existen varias acciones.
- Mostrar el programa como contenido reproducible, no deshabilitado.
- Permitir:
  - Reproducir.
  - Pausar.
  - Adelantar.
  - Retroceder.
  - Volver a TV en vivo.

Al regresar a TV en vivo:

- Restaurar el canal correspondiente.
- Saltar al punto en directo.
- Mostrar la barra de información.

---

## 17. Indicadores de estado

Incluir indicadores discretos para:

- Canal actualmente reproduciéndose.
- Programa en vivo.
- Programa futuro.
- Programa finalizado.
- Catch-up.
- Favorito.
- Grabación activa.
- Grabación programada.
- Recordatorio.
- Canal bloqueado.
- Error de stream.
- EPG no disponible.
- Cargando.

Los indicadores no deben saturar la interfaz. Mostrar primero la información esencial.

---

## 18. Transiciones y animaciones

Las transiciones son una parte fundamental del diseño.

Todas deben ser rápidas y consistentes.

### 18.1 Duraciones

Usar aproximadamente:

- Microinteracciones: 90–140 ms.
- Movimiento de foco: 100–160 ms.
- Paneles laterales: 180–240 ms.
- Barra inferior: 180–260 ms.
- Cambio entre vistas grandes: 220–320 ms.
- Ocultamiento automático: fundido de 140–220 ms.

### 18.2 Curvas

Usar curvas suaves:

- Entrada rápida y desaceleración al final.
- Salida ligeramente más rápida.
- Evitar rebotes.
- Evitar animaciones elásticas.
- Evitar movimientos largos.

### 18.3 Barra de información

Al aparecer:

- Deslizar ligeramente desde abajo.
- Fundir la opacidad de 0 a 1.
- Aplicar gradiente sobre el video.
- No mover ni redimensionar el reproductor.

Al desaparecer:

- Fundido primero.
- Desplazamiento leve hacia abajo.
- Restaurar video completamente limpio.

### 18.4 Lista lateral

Al aparecer:

- Deslizar desde la izquierda.
- Incrementar simultáneamente el scrim.
- Mantener el video sin cambios de tamaño.

### 18.5 Guía

Al cambiar de lista rápida a guía:

- Expandir el área de información.
- Reemplazar progresivamente filas simples por la cuadrícula.
- Evitar un corte negro.
- Mantener continuidad del canal activo y foco.

### 18.6 Menú contextual

- Deslizar desde la derecha.
- Oscurecer ligeramente el resto.
- No desplazar la guía completa.

### 18.7 Movimiento de foco

Cada cambio de foco debe usar:

- Cambio de fondo.
- Ligera escala entre 1.00 y 1.03.
- Transición de texto secundario.
- Elevación mínima.
- Duración rápida.

No aplicar escala excesiva en listas densas.

---

## 19. Reglas de enfoque para control remoto

El foco debe ser completamente determinista.

Nunca permitir:

- Foco perdido.
- Foco invisible.
- Salto a un elemento inesperado.
- Selección detrás de un panel.
- Foco en elementos deshabilitados sin explicación.

### 19.1 Restauración

Al cerrar una capa:

- Restaurar el foco al elemento que la abrió.

Al volver a una categoría:

- Restaurar el último canal seleccionado.

Al abrir la guía:

- Enfocar el programa actual del canal activo.

Al abrir la lista rápida:

- Enfocar el canal actualmente reproducido.

Al abrir recientes:

- Enfocar el canal anterior o el primero de la lista según el contexto.

---

## 20. Comportamiento del botón Back

Implementar una jerarquía clara:

1. Cerrar diálogo.
2. Cerrar menú contextual.
3. Cerrar panel de categorías.
4. Cerrar guía o lista.
5. Cerrar barra de información.
6. Volver a reproducción limpia.
7. Solo después considerar salir de TV en vivo o de la aplicación.

Cuando esté reproduciendo a pantalla completa:

- Una pulsación de Back puede abrir la vista anterior configurada o solicitar salida según la navegación general.
- Evitar salir accidentalmente.
- Considerar pulsación doble para salir si esa es la convención general del proyecto.

---

## 21. Carga, buffering y errores

### 21.1 Carga inicial

- Fondo oscuro.
- Logotipo del canal cuando exista.
- Indicador de carga centrado.
- Nombre del canal.
- No mostrar mensajes técnicos.

### 21.2 Buffering durante reproducción

- Mostrar spinner pequeño en el centro.
- Conservar el último frame.
- No abrir automáticamente menús.
- Ocultar el spinner al recuperar reproducción.

### 21.3 Error

Mostrar una capa discreta con:

- “No se pudo reproducir el canal”.
- Acción Reintentar.
- Acción Canal anterior.
- Detalle técnico únicamente en modo diagnóstico.
- Código interno registrable para soporte.

### 21.4 EPG ausente

Mostrar:

- “Sin información”.
- No dejar espacios rotos.
- Mantener el canal seleccionable.
- No inventar horarios.

---

## 22. Rendimiento

La experiencia debe sentirse instantánea.

Requisitos:

- Mantener una sola instancia principal del reproductor.
- No recrear el reproductor al abrir o cerrar overlays.
- No reiniciar el stream al navegar.
- Precargar logotipos.
- Usar caché de imágenes.
- Paginar o virtualizar listas grandes.
- Renderizar únicamente filas visibles de la guía.
- Evitar recomposición completa por cada actualización del reloj.
- Actualizar la línea de hora actual eficientemente.
- Mantener animaciones fluidas a 60 fps cuando el dispositivo lo permita.
- Reducir automáticamente efectos en hardware limitado.
- Evitar fugas de memoria al cambiar repetidamente de canal.

---

## 23. Persistencia

Guardar:

- Último canal.
- Última categoría.
- Última fuente o playlist.
- Última vista utilizada.
- Canales favoritos.
- Historial.
- Canales recientes.
- Configuración de relación de aspecto.
- Preferencia de subtítulos.
- Pista de audio seleccionada.
- Orden de categorías.
- Posición de navegación cuando sea apropiado.

Después de reiniciar la aplicación:

- Restaurar la experiencia de manera coherente.
- No restaurar menús contextuales abiertos.
- Sí restaurar el último canal y categoría.
- Permitir configurar si la reproducción comienza automáticamente.

---

## 24. Componentes sugeridos

Separar la interfaz en componentes reutilizables:

- `LivePlayerScreen`
- `LivePlayerSurface`
- `ProgramInfoOverlay`
- `RecentChannelsRow`
- `QuickChannelList`
- `ChannelRow`
- `CategorySidebar`
- `ElectronicProgramGuide`
- `EpgChannelColumn`
- `EpgTimelineHeader`
- `EpgProgramCell`
- `ProgramDetailsPanel`
- `ContextActionPanel`
- `PlaybackStatusOverlay`
- `LoadingOverlay`
- `PlaybackErrorOverlay`
- `ToastMessage`
- `RemoteFocusManager`

Separar claramente:

- Estado del reproductor.
- Estado de navegación.
- Estado de overlays.
- Datos EPG.
- Canales.
- Categorías.
- Historial.
- Favoritos.

---

## 25. Modelo de estados de interfaz

Crear un estado explícito para las capas:

- `FULLSCREEN`
- `PROGRAM_INFO`
- `RECENT_CHANNELS`
- `QUICK_CHANNEL_LIST`
- `CATEGORY_LIST`
- `EPG_GUIDE`
- `PROGRAM_DETAILS`
- `CONTEXT_MENU`
- `PLAYBACK_ERROR`
- `LOADING`

No controlar toda la interfaz mediante múltiples booleanos independientes que puedan producir combinaciones inválidas.

Permitir estados compuestos solo cuando sean necesarios, por ejemplo:

- Guía + categorías.
- Guía + menú contextual.
- Reproductor + barra de información.
- Reproductor + recientes.

---

## 26. Flujo principal esperado

### 26.1 Abrir TV en vivo

1. Cargar último canal.
2. Iniciar stream.
3. Mostrar información breve.
4. Ocultar información.
5. Dejar video limpio.

### 26.2 Consultar información

1. Presionar OK.
2. Barra inferior entra suavemente.
3. Mostrar programa actual, siguiente y progreso.
4. Ocultarla después de inactividad.

### 26.3 Ver canales recientes

1. Abrir barra.
2. Abrir fila de recientes.
3. Navegar horizontalmente.
4. Presionar OK.
5. Cambiar canal.
6. Cerrar fila.
7. Mostrar información del nuevo canal.

### 26.4 Abrir lista de canales

1. Ejecutar acción asignada.
2. Panel entra desde la izquierda.
3. Enfocar canal activo.
4. Navegar sin cambiar la reproducción.
5. OK confirma el nuevo canal.
6. Panel se cierra.
7. Reproducción cambia.

### 26.5 Abrir categorías

1. Desde la lista, presionar izquierda.
2. Panel de categorías entra.
3. Seleccionar categoría.
4. Actualizar canales.
5. Derecha vuelve a lista.
6. Back cierra categorías.

### 26.6 Abrir guía

1. Ejecutar acción de guía.
2. Mostrar cuadrícula.
3. Enfocar programa actual del canal activo.
4. Navegar vertical y horizontalmente.
5. OK reproduce o abre opciones.
6. Back regresa al video.

### 26.7 Abrir menú contextual

1. Mantener OK.
2. Panel derecho entra.
3. Mostrar acciones válidas.
4. Ejecutar acción.
5. Cerrar panel.
6. Mantener el foco y la vista anterior.

---

## 27. Criterios de aceptación

La implementación se considera correcta cuando:

- El video continúa reproduciéndose al abrir listas y paneles.
- La aplicación puede operarse completamente con un control remoto.
- El foco nunca se pierde.
- La lista rápida abre sobre el video.
- Las categorías entran desde la izquierda.
- El menú contextual entra desde la derecha.
- La barra informativa entra desde abajo.
- La guía muestra canales, horarios y programas en una cuadrícula.
- La línea de hora actual se actualiza correctamente.
- El canal activo está siempre identificado.
- El programa actual muestra progreso.
- Los programas anteriores con Catch-up pueden reproducirse.
- Los favoritos se actualizan inmediatamente.
- Los canales recientes conservan el orden correcto.
- Back cierra una capa a la vez.
- El cambio de canal no recrea toda la pantalla.
- La interfaz no parpadea al cambiar entre overlays.
- Las transiciones mantienen una duración uniforme.
- El rendimiento permanece fluido con listas grandes.
- Los controles se ocultan automáticamente durante la reproducción.
- La experiencia visual mantiene el video como elemento principal en todo momento.

---

## 28. Entrega requerida

Antes de modificar código:

1. Revisar la arquitectura actual del módulo de TV en vivo.
2. Identificar qué componentes ya existen.
3. Crear un plan de implementación por fases.
4. Indicar qué archivos serán creados o modificados.
5. Definir el modelo de estados.
6. Definir el flujo de eventos del control remoto.
7. Definir cómo se conservará una sola instancia del reproductor.
8. Explicar cómo se conectará la guía EPG con canales y programas.
9. Explicar cómo se implementarán las animaciones.
10. Señalar riesgos de rendimiento o conflictos con el código actual.

Después, implementar en fases:

### Fase 1

Reproductor, pantalla completa y barra de información.

### Fase 2

Lista rápida, categorías y navegación con D-pad.

### Fase 3

Canales recientes, favoritos e historial.

### Fase 4

Guía EPG completa y línea de hora actual.

### Fase 5

Menús contextuales, detalles y Catch-up.

### Fase 6

Pulido de animaciones, rendimiento, errores y persistencia.

No entregar únicamente una maqueta estática. Todos los elementos deben quedar conectados a estados y eventos reales, preparados para recibir información del proveedor, EPG y reproductor.
