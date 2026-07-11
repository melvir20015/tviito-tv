# Fase 1 — Identidad propia

Fecha: 2026-07-11.

## Alcance aplicado

Esta fase separa la identidad operativa de Tviito TV respecto de la base heredada sin introducir proveedores, listas, credenciales, canales ni URLs IPTV reales.

## Cambios principales

- `applicationId` Android propio: `com.tviito.tv`.
- Nombre visible de aplicación: `Tviito TV`.
- Icono adaptativo y banner conservan vectores propios del repositorio y se documentan como variantes originales de Tviito TV.
- Textos visibles que mencionaban la marca heredada pasan a `Tviito TV`, salvo la atribución explícita al proyecto base bajo licencia MIT.
- Backups exportados usan prefijo `tviito-tv-backup`.
- La pantalla de ajustes incluye un bloque “Acerca de Tviito TV” con:
  - declaración de identidad propia,
  - aclaración de que la app no incluye contenido IPTV,
  - atribución MIT al proyecto base,
  - estado de privacidad de telemetría y actualizador.
- Variables de opt-in para telemetría/actualización y firma pasan a prefijo `TVIITO_*`.
- CI de Android instala explícitamente JDK 17 y Android SDK/plataforma 35 antes de compilar y ejecutar pruebas unitarias.

## Impacto del `applicationId`

Cambiar `applicationId` de la app instalada crea una identidad de paquete distinta para Android. En la práctica:

- `com.tviito.tv` se instala como aplicación diferente respecto de builds previos con `com.ultratv.tv.nativeapp`.
- Los datos privados de Android no se migran automáticamente entre paquetes.
- Cualquier publicación, firma, enlace profundo, permisos de proveedor o configuración MDM debe apuntar al nuevo paquete.
- El namespace Kotlin se mantiene temporalmente en `com.ultratv.tv.nativeapp` para evitar un refactor masivo de paquetes en esta fase. La migración de package Kotlin queda como plan gradual.

## Telemetría y actualizador

La telemetría remota y el auto-update siguen desactivados por defecto:

- `LOG_URL` y `LOG_TOKEN` quedan vacíos si no se define `TVIITO_LOG_URL` y `TVIITO_LOG_TOKEN`.
- `AUTO_UPDATE_ENABLED` queda `false` si no se define `TVIITO_AUTO_UPDATE_ENABLED=true`.
- `UPDATE_REPO` y `UPDATE_APK_NAME` quedan vacíos si no se define `TVIITO_UPDATE_REPO` y `TVIITO_UPDATE_APK_NAME`.
- El `network_security_config` elimina el dominio heredado de Worker y solo conserva GitHub en TLS para builds que opten explícitamente por GitHub Releases.

## Atribución MIT

Se conserva `LICENSE` con el aviso MIT del proyecto base. La app muestra atribución visible en Ajustes → Acerca de Tviito TV.

## Deuda técnica intencional

- El package Kotlin y nombres internos como `UltraDb`, `UltraTvApp` o `UltraTokens` no se renombraron para evitar un refactor grande no relacionado y reducir riesgo de build.
- El workflow de release sigue existiendo, pero usa secretos y artefactos con prefijo Tviito. El actualizador no consulta releases si no se configura opt-in explícito.
