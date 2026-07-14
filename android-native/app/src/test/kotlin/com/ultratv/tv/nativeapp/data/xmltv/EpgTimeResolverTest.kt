package com.ultratv.tv.nativeapp.data.xmltv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class EpgTimeResolverTest {
    @Test fun `offset positivo explicito se convierte a instant canonico`() {
        val result = resolve("20250714140000 +0200")
        assertEquals(Instant.parse("2025-07-14T12:00:00Z"), result.instant)
        assertEquals(EpgTimeResolutionSource.EXPLICIT_OFFSET, result.resolutionSource)
    }

    @Test fun `offset negativo explicito se convierte a instant canonico`() {
        val result = resolve("20250714080000 -0500")
        assertEquals(Instant.parse("2025-07-14T13:00:00Z"), result.instant)
        assertEquals(EpgTimeResolutionSource.EXPLICIT_OFFSET, result.resolutionSource)
    }

    @Test fun `timestamp sin offset usa zona de origen del proveedor`() {
        val result = resolve("20250714140000", providerRule = EpgTimeRule(sourceZoneId = "Europe/Madrid"))
        assertEquals(Instant.parse("2025-07-14T12:00:00Z"), result.instant)
        assertEquals(ZoneId.of("Europe/Madrid"), result.sourceZoneId)
        assertEquals(EpgTimeResolutionSource.PROVIDER_ZONE, result.resolutionSource)
    }

    @Test fun `la conversion de visualizacion usa la zona del perfil o dispositivo sin cambiar el instant`() {
        val result = resolve("20250714140000 +0200")
        assertEquals("2025-07-14T05:00-07:00[America/Los_Angeles]", result.instant.atZone(ZoneId.of("America/Los_Angeles")).toString())
        assertEquals(Instant.parse("2025-07-14T12:00:00Z"), result.instant)
    }

    @Test fun `dst spring-forward usa reglas IANA de la zona de origen`() {
        val result = resolve("20250309030000", providerRule = EpgTimeRule(sourceZoneId = "America/New_York"))
        assertEquals(Instant.parse("2025-03-09T07:00:00Z"), result.instant)
    }

    @Test fun `dst fall-back usa offsets explicitos para diferenciar la hora repetida`() {
        val first = resolve("20251102013000 -0400", providerRule = EpgTimeRule(sourceZoneId = "America/New_York"))
        val second = resolve("20251102013000 -0500", providerRule = EpgTimeRule(sourceZoneId = "America/New_York"))
        assertEquals(Instant.parse("2025-11-02T05:30:00Z"), first.instant)
        assertEquals(Instant.parse("2025-11-02T06:30:00Z"), second.instant)
    }

    @Test fun `offset manual adicional se aplica despues de resolver la hora base`() {
        val result = resolve("20250714140000", providerRule = EpgTimeRule(sourceZoneId = "Europe/Madrid", manualOffsetMinutes = 45))
        assertEquals(Instant.parse("2025-07-14T12:45:00Z"), result.instant)
        assertEquals(Duration.ofMinutes(45), result.appliedManualOffset)
    }

    @Test fun `prioridad canal proveedor inferencia para timestamps sin offset`() {
        val channelWins = resolve(
            raw = "20250714140000",
            providerRule = EpgTimeRule(sourceZoneId = "Europe/Madrid", manualOffsetMinutes = 30),
            channelRule = EpgTimeRule(sourceZoneId = "America/New_York", manualOffsetMinutes = -15),
            inferredSourceZoneId = "America/Los_Angeles",
            inferenceConfidence = 0.99,
        )
        val providerWins = resolve("20250714140000", providerRule = EpgTimeRule(sourceZoneId = "Europe/Madrid"), inferredSourceZoneId = "America/Los_Angeles", inferenceConfidence = 0.99)
        val inferenceWins = resolve("20250714140000", inferredSourceZoneId = "America/Los_Angeles", inferenceConfidence = 0.95)
        val lowConfidence = EpgTimeResolver.resolve(EpgTimeResolver.parseTimestamp("20250714140000")!!, inferredSourceZoneId = "America/Los_Angeles", inferenceConfidence = 0.50)

        assertEquals(Instant.parse("2025-07-14T17:45:00Z"), channelWins.instant)
        assertEquals(EpgTimeResolutionSource.CHANNEL_ZONE, channelWins.resolutionSource)
        assertEquals(Instant.parse("2025-07-14T12:00:00Z"), providerWins.instant)
        assertEquals(EpgTimeResolutionSource.PROVIDER_ZONE, providerWins.resolutionSource)
        assertEquals(Instant.parse("2025-07-14T21:00:00Z"), inferenceWins.instant)
        assertEquals(EpgTimeResolutionSource.INFERENCE, inferenceWins.resolutionSource)
        assertNull(lowConfidence)
    }

    private fun resolve(
        raw: String,
        providerRule: EpgTimeRule = EpgTimeRule(),
        channelRule: EpgTimeRule = EpgTimeRule(),
        inferredSourceZoneId: String? = null,
        inferenceConfidence: Double? = null,
    ): EpgTimeResolution = EpgTimeResolver.resolve(EpgTimeResolver.parseTimestamp(raw)!!, providerRule, channelRule, inferredSourceZoneId, inferenceConfidence)!!
}
