# Primera tarea para Codex

Trabaja en este repositorio como ingeniero principal de Android TV.

Lee primero:

- `README.md`
- `AGENTS.md`
- `docs/PRODUCT_REQUIREMENTS.md`
- `docs/ARCHITECTURE.md`
- `docs/DECISIONS.md`
- `docs/ROADMAP.md`

## Objetivo

Completar únicamente la **Fase 0 — Auditoría y línea base** y preparar un primer cambio seguro. No implementar todavía perfiles, TMDB, recomendaciones ni rediseño general.

## Tareas

1. Inspecciona la estructura real del repositorio e identifica cuál es la implementación Android nativa activa.
2. Compila la aplicación nativa desde cero con JDK 17.
3. Ejecuta:
   - tests unitarios,
   - lint,
   - `assembleDebug`.
4. Documenta versiones reales de:
   - Gradle wrapper,
   - Android Gradle Plugin,
   - Kotlin,
   - Compose,
   - Compose for TV,
   - Media3,
   - Room,
   - Hilt,
   - WorkManager,
   - minSdk,
   - targetSdk,
   - compileSdk.
5. Inventaría:
   - entidades Room,
   - DAOs,
   - migraciones,
   - repositorios,
   - workers,
   - rutas de navegación,
   - pantallas,
   - componentes de playback,
   - telemetría,
   - actualización automática,
   - configuración remota.
6. Busca de forma específica:
   - URLs hardcodeadas,
   - tokens,
   - secretos,
   - endpoints de Cloudflare,
   - mecanismos que envíen crashes o eventos,
   - actualizador que consulte releases del proyecto original.
7. Evita que una compilación propia envíe información o consulte actualizaciones del upstream:
   - preferentemente crea una configuración segura desactivada por defecto,
   - no reemplaces con secretos falsos,
   - no rompas compilación,
   - añade pruebas o validación donde sea razonable.
8. Crea `docs/BASELINE_AUDIT.md` con:
   - hallazgos,
   - comandos,
   - resultados reales,
   - riesgos,
   - deuda técnica,
   - diagrama simple de arquitectura actual,
   - recomendación para la Fase 1.
9. Crea `local.properties.example` o equivalente únicamente si hace falta, sin secretos.
10. Añade o mejora pruebas de humo mínimas que no requieran credenciales ni streams privados.

## Restricciones

- No cambies todavía el package completo.
- No implementes todavía la sincronización cloud; solo identifica cualquier backend o mecanismo remoto heredado.
- No cambies el esquema Room salvo que sea imprescindible para una corrección de seguridad; si ocurre, incluye migración y prueba.
- No borres soporte funcional existente.
- No incluyas URLs IPTV reales.
- No copies recursos de otras apps.
- No hagas commit ni push.
- No afirmes que una prueba pasó si no se ejecutó.
- No ocultes fallos existentes: documéntalos.
- Mantén el cambio pequeño y revisable.

## Criterios de aceptación

- Existe una ruta reproducible para compilar.
- `docs/BASELINE_AUDIT.md` refleja el código real.
- No quedan conexiones automáticas activas hacia telemetría o actualizaciones del upstream en la configuración por defecto del fork.
- No se añadieron secretos.
- Build, tests y lint ejecutados o sus impedimentos quedan claramente documentados.
- La aplicación mantiene su funcionalidad base.
- El resultado prepara una Fase 1 de rebranding sin mezclar todavía funciones de producto.

## Entrega

Al finalizar, proporciona:

1. Resumen.
2. Archivos modificados.
3. Hallazgos de seguridad.
4. Comandos y resultados.
5. Riesgos pendientes.
6. Próxima tarea recomendada.
