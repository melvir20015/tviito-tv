package com.ultratv.tv.nativeapp.data.xmltv

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Timestamp XMLTV parseado sin convertir todavía a una hora visible. */
data class ParsedEpgTimestamp(
    val localDateTime: LocalDateTime,
    val explicitOffset: ZoneOffset?,
    val originalText: String,
)

enum class EpgTimeResolutionSource {
    EXPLICIT_OFFSET,
    CHANNEL_ZONE,
    PROVIDER_ZONE,
    INFERENCE,
    UNRESOLVED,
}

data class EpgTimeResolution(
    val instant: Instant,
    val sourceZoneId: ZoneId?,
    val appliedManualOffset: Duration,
    val resolutionSource: EpgTimeResolutionSource,
    val confidence: Double?,
)

data class EpgTimeRule(
    val sourceZoneId: String? = null,
    val manualOffsetMinutes: Int = 0,
)

/**
 * Resuelve horarios XMLTV a Instant. La zona de visualización del usuario no participa aquí:
 * se aplica solo al formatear en UI a partir del Instant persistido.
 */
object EpgTimeResolver {
    private val compactFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US)
    private val offsetPattern = Regex("^\\s*(\\d{14})(?:\\s*([+-]\\d{2}:?\\d{2}|Z))?.*$")

    fun parseTimestamp(raw: String?): ParsedEpgTimestamp? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return null
        val match = offsetPattern.matchEntire(value) ?: return null
        val local = runCatching { LocalDateTime.parse(match.groupValues[1], compactFormatter) }.getOrNull() ?: return null
        val offset = match.groupValues.getOrNull(2)
            ?.takeIf { it.isNotBlank() }
            ?.let(::parseOffset)
        return ParsedEpgTimestamp(local, offset, value)
    }

    fun resolve(
        parsed: ParsedEpgTimestamp,
        providerRule: EpgTimeRule = EpgTimeRule(),
        channelRule: EpgTimeRule = EpgTimeRule(),
        inferredSourceZoneId: String? = null,
        inferenceConfidence: Double? = null,
    ): EpgTimeResolution? {
        val base = when {
            parsed.explicitOffset != null -> BaseResolution(
                parsed.localDateTime.atOffset(parsed.explicitOffset).toInstant(),
                null,
                EpgTimeResolutionSource.EXPLICIT_OFFSET,
                null,
            )
            channelRule.sourceZoneId.isValidZoneId() -> resolveWithZone(parsed, channelRule.sourceZoneId!!, EpgTimeResolutionSource.CHANNEL_ZONE, null)
            providerRule.sourceZoneId.isValidZoneId() -> resolveWithZone(parsed, providerRule.sourceZoneId!!, EpgTimeResolutionSource.PROVIDER_ZONE, null)
            inferredSourceZoneId.isValidZoneId() && (inferenceConfidence ?: 0.0) >= 0.90 ->
                resolveWithZone(parsed, inferredSourceZoneId!!, EpgTimeResolutionSource.INFERENCE, inferenceConfidence)
            else -> return null
        }
        val manualOffset = Duration.ofMinutes((channelRule.manualOffsetMinutes.takeIf { it != 0 } ?: providerRule.manualOffsetMinutes).toLong())
        return EpgTimeResolution(
            instant = base.instant.plus(manualOffset),
            sourceZoneId = base.sourceZoneId,
            appliedManualOffset = manualOffset,
            resolutionSource = base.source,
            confidence = base.confidence,
        )
    }

    private fun resolveWithZone(
        parsed: ParsedEpgTimestamp,
        zoneIdText: String,
        source: EpgTimeResolutionSource,
        confidence: Double?,
    ): BaseResolution {
        val zoneId = ZoneId.of(zoneIdText)
        return BaseResolution(parsed.localDateTime.atZone(zoneId).toInstant(), zoneId, source, confidence)
    }

    private fun parseOffset(value: String): ZoneOffset = when (value) {
        "Z" -> ZoneOffset.UTC
        else -> {
            val normalized = if (value.length == 5) "${value.substring(0, 3)}:${value.substring(3)}" else value
            ZoneOffset.of(normalized)
        }
    }

    private fun String?.isValidZoneId(): Boolean = !this.isNullOrBlank() && runCatching { ZoneId.of(this) }.isSuccess

    private data class BaseResolution(
        val instant: Instant,
        val sourceZoneId: ZoneId?,
        val source: EpgTimeResolutionSource,
        val confidence: Double?,
    )
}
