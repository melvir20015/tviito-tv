# Decisions

## ADR-001 — No modificar TiviMate

Se desarrollará una aplicación propia. No se descompilará, parcheará ni redistribuirá TiviMate.

## ADR-002 — Base nativa

Se evaluará y utilizará la implementación `android-native/` de Ultra TV como punto de partida, sujeta a auditoría.

## ADR-003 — Licencia y crédito

Se conservará la licencia MIT y la atribución requerida al código base. El producto tendrá identidad visual propia.

## ADR-004 — Local-first

Perfiles, historial, favoritos y recomendaciones funcionarán localmente sin cuenta obligatoria.

## ADR-005 — TV-first

D-pad, foco y estabilidad en Android TV tienen prioridad sobre soporte táctil.

## ADR-006 — Tiempo canónico

EPG se almacena como `Instant`. La zona local solo se utiliza para presentación.

## ADR-007 — Zona de origen separada

La zona del usuario no puede sustituir la zona de origen de un EPG sin offset. Se modelarán ambas.

## ADR-008 — Offset manual permanente

Aunque exista alineación automática, se conservará ajuste manual por proveedor y canal.

## ADR-009 — Metadatos no destructivos

El catálogo original del proveedor nunca se reemplaza de manera irreversible.

## ADR-010 — Adaptador de metadatos

TMDB será una implementación de `MetadataProvider`, no una dependencia inseparable del dominio.

## ADR-011 — Matching con confianza

Coincidencias dudosas no se asignarán automáticamente.

## ADR-012 — Recomendaciones explicables

El MVP usará scoring local determinista con motivos visibles. No machine learning remoto.

## ADR-013 — Vistos separados

Contenido ya visto se excluye de recomendaciones nuevas y se muestra en “Volver a ver”.

## ADR-014 — Telemetría desactivada inicialmente

Cualquier telemetría heredada se eliminará o desactivará. Una futura implementación será propia, sanitizada y con consentimiento.

## ADR-015 — Secretos fuera del repositorio

Claves, tokens y credenciales reales no se versionan.

## ADR-016 — Monolito modular primero

No se hará una gran modularización Gradle antes de estabilizar dominios y pruebas.

## ADR-017 — No copiar interfaz exacta

Se replicarán capacidades generales, no pantallas, assets o identidad de aplicaciones propietarias.

## ADR-018 — Sincronización opcional

La aplicación funcionará sin cuenta. Cloud sync será opcional.

## ADR-019 — Room local-first

La interfaz lee y escribe en Room. La sincronización usa outbox y cambios incrementales.

## ADR-020 — Hogar y dispositivos

Los perfiles pertenecen a un hogar. Cada instalación sincronizada es registrable y revocable.

## ADR-021 — Credenciales no sincronizadas en MVP

Las credenciales IPTV se ingresan por dispositivo. Una fase posterior requiere E2EE.

## ADR-022 — Backend intercambiable

El dominio depende de `CloudSyncGateway`, no de Firebase, Supabase o un backend concreto.

## ADR-023 — Conflictos por entidad

No se aplicará una única política global de last-write-wins.
