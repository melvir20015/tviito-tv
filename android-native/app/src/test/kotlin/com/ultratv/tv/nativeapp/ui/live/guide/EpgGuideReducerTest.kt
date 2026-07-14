package com.ultratv.tv.nativeapp.ui.live.guide

import org.junit.Assert.assertEquals
import org.junit.Test

class EpgGuideReducerTest {
    private val state = EpgGuideUiState(
        channelIds = listOf(10L, 20L, 30L),
        programIdsByChannel = mapOf(
            10L to listOf(101L, 102L),
            20L to listOf(201L, 202L, 203L),
            30L to listOf(301L),
        ),
        focusLayer = EpgFocusLayer.PROGRAM_CELL,
    )

    @Test fun derechaEIzquierdaNaveganProgramasSinSalirDeLaCelda() {
        val segundo = EpgGuideReducer.reduce(state, EpgGuideAction.Dpad(EpgGuideDirection.RIGHT))
        val primero = EpgGuideReducer.reduce(segundo, EpgGuideAction.Dpad(EpgGuideDirection.LEFT))

        assertEquals(EpgFocusLayer.PROGRAM_CELL, segundo.focusLayer)
        assertEquals(102L, segundo.focusedProgramId)
        assertEquals(101L, primero.focusedProgramId)
    }

    @Test fun arribaYAbajoCambianFilaManteniendoIndiceHorizontalSeguro() {
        val atSecondProgram = state.copy(focusedProgramIndex = 1)

        val nextRow = EpgGuideReducer.reduce(atSecondProgram, EpgGuideAction.Dpad(EpgGuideDirection.DOWN))
        val lastRow = EpgGuideReducer.reduce(nextRow, EpgGuideAction.Dpad(EpgGuideDirection.DOWN))

        assertEquals(20L, nextRow.focusedChannelId)
        assertEquals(202L, nextRow.focusedProgramId)
        assertEquals(30L, lastRow.focusedChannelId)
        assertEquals(301L, lastRow.focusedProgramId)
    }

    @Test fun izquierdaSeparaFocoEntreProgramaFilaGrupoPlaylistYRaiz() {
        val row = EpgGuideReducer.reduce(state, EpgGuideAction.Dpad(EpgGuideDirection.LEFT))
        val group = EpgGuideReducer.reduce(row, EpgGuideAction.Dpad(EpgGuideDirection.LEFT))
        val playlist = EpgGuideReducer.reduce(group, EpgGuideAction.Dpad(EpgGuideDirection.LEFT))
        val root = EpgGuideReducer.reduce(playlist, EpgGuideAction.Dpad(EpgGuideDirection.LEFT))

        assertEquals(EpgFocusLayer.CHANNEL_ROW, row.focusLayer)
        assertEquals(EpgFocusLayer.GROUP, group.focusLayer)
        assertEquals(EpgFocusLayer.PLAYLIST, playlist.focusLayer)
        assertEquals(EpgFocusLayer.ROOT_NAVIGATION, root.focusLayer)
    }

    @Test fun capasNoProgramaticasNaveganHaciaLaDerechaHastaProgramas() {
        val root = state.copy(focusLayer = EpgFocusLayer.ROOT_NAVIGATION)
        val playlist = EpgGuideReducer.reduce(root, EpgGuideAction.Dpad(EpgGuideDirection.RIGHT))
        val group = EpgGuideReducer.reduce(playlist, EpgGuideAction.Dpad(EpgGuideDirection.RIGHT))
        val program = EpgGuideReducer.reduce(group, EpgGuideAction.Dpad(EpgGuideDirection.RIGHT))

        assertEquals(EpgFocusLayer.PLAYLIST, playlist.focusLayer)
        assertEquals(EpgFocusLayer.GROUP, group.focusLayer)
        assertEquals(EpgFocusLayer.PROGRAM_CELL, program.focusLayer)
    }
    @Test fun navegacionVerticalConservaIndiceHorizontalDisponible() {
        val secondRowSecondProgram = EpgGuideReducer.reduce(
            state.copy(focusedChannelIndex = 1, focusedProgramIndex = 1),
            EpgGuideAction.Dpad(EpgGuideDirection.DOWN),
        )
        val backToSecondRow = EpgGuideReducer.reduce(secondRowSecondProgram, EpgGuideAction.Dpad(EpgGuideDirection.UP))

        assertEquals(30L, secondRowSecondProgram.focusedChannelId)
        assertEquals(301L, secondRowSecondProgram.focusedProgramId)
        assertEquals(1, secondRowSecondProgram.horizontalWindowOffset)
        assertEquals(20L, backToSecondRow.focusedChannelId)
        assertEquals(202L, backToSecondRow.focusedProgramId)
    }

}
