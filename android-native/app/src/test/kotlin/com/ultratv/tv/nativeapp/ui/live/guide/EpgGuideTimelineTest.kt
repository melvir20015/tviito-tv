package com.ultratv.tv.nativeapp.ui.live.guide

import org.junit.Assert.assertEquals
import org.junit.Test

class EpgGuideTimelineTest {
    @Test fun accionDeProgramaDistinguePasadoActualYFuturo() {
        assertEquals(EpgProgramAction.PAST, epgProgramAction(startMs = 0L, endMs = 10L, nowMs = 10L))
        assertEquals(EpgProgramAction.CURRENT, epgProgramAction(startMs = 10L, endMs = 20L, nowMs = 15L))
        assertEquals(EpgProgramAction.FUTURE, epgProgramAction(startMs = 20L, endMs = 30L, nowMs = 15L))
    }

    @Test fun bloquesCubrenHuecosYRespetanDuracionProporcionalDeLaVentana() {
        val blocks = buildEpgTimelineBlocks(
            channelId = 1L,
            programs = listOf(
                epg(id = 10L, start = 30L, end = 90L),
                epg(id = 11L, start = 120L, end = 180L),
            ),
            windowStartMs = 0L,
            windowEndMs = 180L,
        )

        assertEquals(listOf(null, 10L, null, 11L), blocks.map { it.program?.id })
        assertEquals(listOf(0L to 30L, 30L to 90L, 90L to 120L, 120L to 180L), blocks.map { it.startMs to it.endMs })
    }

    private fun epg(id: Long, start: Long, end: Long) = com.ultratv.tv.nativeapp.data.db.EpgEntity(
        id = id,
        channelId = 1L,
        title = "Programa $id",
        description = null,
        startMs = start,
        endMs = end,
    )
}
