# Product Requirements

## 1. Resumen

Tviito TV es un cliente IPTV nativo para Android TV y Google TV. Se diferencia por integrar:

- Múltiples perfiles.
- Múltiples proveedores.
- Experiencia de TV en vivo estable.
- EPG ajustado automáticamente a la zona del usuario.
- Catálogo enriquecido.
- Recomendaciones locales por perfil.
- Diseño original optimizado para control remoto.

## 2. Usuarios

### Administrador del dispositivo

Puede:

- Crear y eliminar perfiles.
- Agregar proveedores.
- Asignar proveedores a perfiles.
- Configurar EPG.
- Gestionar PIN y restricciones.
- Configurar metadatos.
- Realizar backup y restore.

### Perfil adulto

Puede:

- Ver proveedores autorizados.
- Mantener historial y favoritos propios.
- Dar feedback.
- Recibir recomendaciones.
- Cambiar preferencias permitidas.

### Perfil infantil

Puede:

- Acceder solo a contenido autorizado.
- No modificar proveedores.
- No abrir configuración protegida.
- Recibir recomendaciones dentro de límites de edad y categorías.

## 3. Historias de usuario prioritarias

### Perfiles

- Como usuario, quiero seleccionar mi perfil al abrir la app para conservar mis favoritos e historial.
- Como administrador, quiero asignar diferentes listas a cada perfil.
- Como usuario, quiero entrar automáticamente al último proveedor o elegir uno cada vez.
- Como administrador, quiero proteger perfiles y ajustes mediante PIN.

### Live TV

- Como usuario, quiero cambiar de canal rápidamente sin volver al inicio.
- Como usuario, quiero ver programa actual y siguiente.
- Como usuario, quiero abrir una guía completa y navegarla con D-pad.
- Como usuario, quiero seleccionar audio, subtítulos y relación de aspecto.

### EPG

- Como usuario, quiero ver la programación en mi hora local.
- Como usuario, quiero que el horario de verano se aplique solo.
- Como administrador, quiero indicar la zona de origen cuando el proveedor no la incluya.
- Como administrador, quiero aplicar un ajuste manual por lista o canal.
- Como usuario, quiero saber cuándo una hora fue inferida y su nivel de confianza.

### VOD

- Como usuario, quiero ver póster, fondo, sinopsis, reparto, director, duración, géneros y puntuación.
- Como usuario, quiero continuar una película o episodio.
- Como usuario, quiero ver temporadas y episodios.
- Como usuario, quiero que títulos pobres del proveedor se completen automáticamente.
- Como administrador, quiero corregir una coincidencia externa equivocada.

### Recomendaciones

- Como usuario, quiero recibir contenido relacionado con lo que me gusta.
- Como usuario, quiero excluir contenido ya visto.
- Como usuario, quiero marcar “No me interesa”.
- Como usuario, quiero saber por qué se recomendó un título.
- Como usuario, quiero una fila separada para volver a ver.

## 4. Requisitos funcionales

### RF-001 Selector de perfiles

Al iniciar, si existe más de un perfil activo, mostrar selector. Si existe uno y la opción de selector está desactivada, abrirlo directamente.

### RF-002 Aislamiento de perfil

Favoritos, progreso, historial, feedback, recomendaciones, restricciones y preferencias no deben mezclarse entre perfiles.

### RF-003 Selector de proveedor

Después de seleccionar perfil:

- Mostrar proveedores autorizados si `alwaysAskProvider=true`.
- Abrir último proveedor válido si `alwaysAskProvider=false`.
- Abrir directamente si solo existe un proveedor autorizado.

### RF-004 Conversión EPG

Los programas con offset deben convertirse a `Instant`. La hora visible debe calcularse usando la zona del perfil o dispositivo.

### RF-005 Origen EPG sin offset

Si no existe offset:

1. Regla del canal.
2. Regla del proveedor.
3. Inferencia de alta confianza.
4. Solicitud de configuración.
5. Nunca asumir silenciosamente que la hora pertenece a la zona del usuario.

### RF-006 Offset manual

Permitir offset adicional entre -14:00 y +14:00, en pasos de 15 minutos, por proveedor y canal.

### RF-007 Enriquecimiento

Permitir escaneo automático y manual. Procesar en background, por lotes, con estado y cancelación.

### RF-008 Coincidencia

Guardar confianza y no asignar automáticamente resultados bajo el umbral establecido.

### RF-009 Datos originales

No borrar título, etiquetas o identificadores entregados por el proveedor.

### RF-010 Duplicados

Agrupar entradas con identidad externa común. Conservar cada fuente de reproducción.

### RF-011 Recomendaciones

Generar filas por perfil usando historial, feedback y metadatos. Excluir vistos de filas nuevas.

### RF-012 Explicabilidad

Toda recomendación debe tener uno o varios `reasonCode`.

### RF-013 Puntuación de calidad

No ordenar solo por `voteAverage`. Considerar `voteCount` mediante puntuación ponderada.

### RF-014 Privacidad

Telemetría e historial remoto deben estar desactivados por defecto.

### RF-015 Credenciales

Guardar credenciales cifradas y sanitizar logs.

## 5. Requisitos no funcionales

### Rendimiento

- Catálogos grandes no deben cargarse completos en memoria.
- Primer render del catálogo debe usar datos locales.
- Scroll y foco deben permanecer fluidos.
- Enriquecimiento no debe ejecutarse durante playback si afecta rendimiento.
- Cambio de canal no debe esperar consultas de metadatos.

### Confiabilidad

- Reintentos con backoff.
- Restaurar estado después de cierre.
- Sincronización transaccional.
- No borrar catálogo útil si una sincronización falla.
- Migraciones de DB comprobadas.

### Accesibilidad y TV

- D-pad completo.
- Contraste suficiente.
- Texto legible a distancia.
- Indicador de foco claro.
- No depender de gestos táctiles.
- Controles coherentes.

### Compatibilidad

Objetivo inicial:

- Android TV.
- Google TV.
- Fire TV sujeto a validación.
- Hardware ARM común.
- API mínima definida por la base después de auditoría.

## 6. Reglas de negocio

- Una entrada puede pertenecer a un proveedor y a una identidad de contenido compartida.
- El proveedor define disponibilidad; la metadata externa define presentación.
- El perfil define preferencias y acceso.
- Un perfil infantil nunca recibe una recomendación fuera de sus reglas.
- Un título ya visto no aparece como nuevo.
- Una coincidencia corregida manualmente no se reemplaza automáticamente.
- Un offset manual bloqueado tiene prioridad sobre inferencias.
- Cambiar zona de visualización no requiere volver a descargar EPG.
- Cambiar zona de origen sí requiere recalcular programas sin offset explícito.

## 7. Diseño de filas de inicio

Orden sugerido:

1. Continuar viendo.
2. Televisión reciente.
3. Siguiente episodio.
4. Recomendaciones principales.
5. Porque viste…
6. Géneros preferidos.
7. Alta puntuación.
8. Descubrimientos.
9. Favoritos.
10. Volver a ver.

El orden puede ser configurable por perfil en una fase posterior.

## 8. Casos de error

- Credenciales inválidas.
- Proveedor inaccesible.
- EPG sin zona.
- XMLTV inválido.
- Stream no compatible.
- Metadata sin coincidencia.
- Metadata ambigua.
- Límite de API.
- Póster inválido.
- Base de datos llena/corrupta.
- Falta de espacio para caché o grabación.
- Dispositivo con hora/zona incorrecta.

Los mensajes deben ser claros y no exponer secretos.

## 9. Analítica local sugerida

- Tiempo de uso por perfil.
- Porcentaje terminado.
- Inicio y fallo de reproducción.
- Razón de error sanitizada.
- Acciones de feedback.
- Calidad de coincidencia.
- EPG con desfase corregido.

La sincronización remota de analítica requiere consentimiento y una decisión posterior.

## 10. Definición de terminado

Una historia está terminada cuando:

- Cumple aceptación.
- Tiene pruebas relevantes.
- Compila.
- Lint pasa.
- Funciona con D-pad.
- Mantiene privacidad.
- Tiene migración si corresponde.
- Está documentada.
- No empeora reproducción ni rendimiento medido.

## 11. Sincronización entre dispositivos

### Historias

- Como propietario, quiero vincular otro televisor mediante QR o código.
- Como usuario, quiero ver mis perfiles y guardados en todos mis dispositivos.
- Como usuario, quiero seguir usando la app sin Internet.
- Como administrador, quiero revocar un dispositivo perdido.

### RF-016 Cuenta opcional

La aplicación debe funcionar sin cuenta. La cuenta solo será obligatoria para cloud sync.

### RF-017 Hogar

Todos los datos compartidos estarán aislados mediante `householdId`.

### RF-018 Vinculación

El segundo dispositivo se vinculará con un código temporal, de un solo uso y aprobado.

### RF-019 Offline-first

Las acciones se guardan primero en Room. La UI no esperará al backend.

### RF-020 Cambios incrementales

Después del snapshot inicial se sincronizarán únicamente cambios posteriores mediante cursor.

### RF-021 Idempotencia

Reenviar el mismo `changeId` no debe duplicar datos.

### RF-022 Revocación

Un dispositivo revocado no puede sincronizar con tokens antiguos.

### RF-023 Credenciales

Las credenciales IPTV no se sincronizan en el MVP. Una fase posterior exige E2EE.

### RF-024 Conflictos

Favoritos, progreso, feedback y eliminaciones tendrán políticas específicas.

### RF-025 Recomendaciones

Se sincronizan señales de comportamiento; las recomendaciones se regeneran localmente.

Detalles: `docs/SYNC_SPEC.md`.
