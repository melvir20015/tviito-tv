package com.ultratv.tv.nativeapp.ui.live

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LiveTvReducerTest {
    private val channels = listOf(10L, 20L, 30L)

    @Test fun focusNeverNullWhenChannelsExistAfterChannelRefresh() {
        val state = LiveTvUiState(channelIds = channels, focusedChannelId = null, selectedChannelId = null, playingChannelId = null)

        val reduced = LiveTvReducer.reduce(state, LiveTvAction.ChannelsChanged(channels))

        assertNotNull(reduced.focusedChannelId)
        assertEquals(10L, reduced.focusedChannelId)
    }

    @Test fun leftMovesFromEpgToChannelsGroupsAndRootNavigation() {
        val fromGuide = LiveTvUiState(mode = LiveTvMode.TV_GUIDE, channelIds = channels)

        val channelsLayer = LiveTvReducer.reduce(fromGuide, LiveTvAction.Dpad(LiveTvDirection.LEFT))
        val groupsLayer = LiveTvReducer.reduce(channelsLayer, LiveTvAction.Dpad(LiveTvDirection.LEFT))
        val rootLayer = LiveTvReducer.reduce(groupsLayer, LiveTvAction.Dpad(LiveTvDirection.LEFT))

        assertEquals(LiveTvMode.CHANNEL_LIST_PREVIEW, channelsLayer.mode)
        assertEquals(LiveTvMode.GROUP_LIST, groupsLayer.mode)
        assertEquals(LiveTvMode.ROOT_NAVIGATION, rootLayer.mode)
    }

    @Test fun backClosesContextGuideChannelsAndControlsByLayers() {
        val menu = LiveTvUiState(
            mode = LiveTvMode.CHANNEL_CONTEXT_MENU,
            openedFromMode = LiveTvMode.TV_GUIDE,
            channelIds = channels,
        )

        val guide = LiveTvReducer.reduce(menu, LiveTvAction.Back)
        val channelsLayer = LiveTvReducer.reduce(guide, LiveTvAction.Back)
        val fullscreen = LiveTvReducer.reduce(channelsLayer, LiveTvAction.Back)
        val controls = LiveTvReducer.reduce(fullscreen, LiveTvAction.Ok)
        val fullscreenAgain = LiveTvReducer.reduce(controls, LiveTvAction.Back)

        assertEquals(LiveTvMode.TV_GUIDE, guide.mode)
        assertEquals(LiveTvMode.CHANNEL_LIST_PREVIEW, channelsLayer.mode)
        assertEquals(LiveTvMode.PLAYER_FULLSCREEN, fullscreen.mode)
        assertEquals(LiveTvMode.PLAYER_CONTROLS_VISIBLE, controls.mode)
        assertEquals(LiveTvMode.PLAYER_FULLSCREEN, fullscreenAgain.mode)
    }

    @Test fun longBackAlwaysReturnsToFullscreen() {
        val state = LiveTvUiState(mode = LiveTvMode.PROGRAM_CONTEXT_MENU, openedFromMode = LiveTvMode.TV_GUIDE, channelIds = channels)

        val reduced = LiveTvReducer.reduce(state, LiveTvAction.LongBack)

        assertEquals(LiveTvMode.PLAYER_FULLSCREEN, reduced.mode)
        assertEquals(LiveTvVideoSurfaceMode.FULLSCREEN, reduced.videoSurfaceMode)
    }

    @Test fun longOkOpensProgramContextMenuFromGuideProgramFocus() {
        val state = LiveTvUiState(mode = LiveTvMode.TV_GUIDE, channelIds = channels, focusedProgramId = 99L)

        val reduced = LiveTvReducer.reduce(state, LiveTvAction.LongOk)

        assertEquals(LiveTvMode.PROGRAM_CONTEXT_MENU, reduced.mode)
        assertEquals(LiveTvMode.TV_GUIDE, reduced.openedFromMode)
    }

    @Test fun longOkOpensChannelContextMenuWithoutFocusedProgram() {
        val state = LiveTvUiState(mode = LiveTvMode.CHANNEL_LIST_OVERLAY, channelIds = channels, focusedProgramId = null)

        val reduced = LiveTvReducer.reduce(state, LiveTvAction.LongOk)

        assertEquals(LiveTvMode.CHANNEL_CONTEXT_MENU, reduced.mode)
        assertEquals(LiveTvMode.CHANNEL_LIST_OVERLAY, reduced.openedFromMode)
    }
}
