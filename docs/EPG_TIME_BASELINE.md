# Baseline EPG y guía temporal — android-native

Fecha: 2026-07-14.

## Alcance revisado

La implementación activa de EPG en `android-native/` conserva esta máquina de estados existente:

1. `ProviderRepository.syncXmltv()` obtiene el proveedor activo, construye el mapa `xmltv channel id → channel.id`, reporta progreso en `SyncStatusBus`, invoca `XmltvParser`, borra el EPG previo del proveedor y persiste los programas nuevos con `EpgDao`.
2. `XmltvParser` sigue siendo un parser streaming XMLTV con `XmlPullParser`; no se introdujo DOM ni carga completa de XML en memoria.
3. Room conserva `EpgEntity.startMs` y `EpgEntity.endMs` como milisegundos epoch, equivalentes a `Instant`, y la guía consulta rangos con `EpgDao.rangeForChannels()`.
4. `EpgGuideLoader` agrupa programas por canal y `EpgGuide` renderiza una línea temporal TV-first con foco D-pad y bloques por programa.

No se cambió el flujo de carga/sincronización/visualización; el cambio se limita a cómo XMLTV resuelve los timestamps antes de persistirlos.

## Decisiones temporales

- Los timestamps XMLTV con offset explícito tienen prioridad absoluta y se convierten directamente a `Instant`.
- Los timestamps XMLTV sin offset requieren zona IANA de origen por canal o proveedor. La zona del usuario/dispositivo no se usa para corregir origen.
- Se añadieron campos Room para futura configuración persistente de zona IANA y offset manual por proveedor y por canal.
- El offset manual adicional se aplica después de resolver la hora base; canal tiene prioridad sobre proveedor cuando define un offset distinto de cero.
- La inferencia solo se acepta en el resolver si se entrega una confianza alta (`>= 0.90`). En esta etapa no se implementa ningún inferidor automático ni se aplican inferencias de baja confianza.
- La UI continúa mostrando los epoch millis con la zona local del dispositivo mediante el formateador existente. Cuando existan perfiles con zona propia, la capa de presentación debe recibir un `ZoneId` del perfil y formatear desde el mismo `Instant`, sin reescribir Room.
