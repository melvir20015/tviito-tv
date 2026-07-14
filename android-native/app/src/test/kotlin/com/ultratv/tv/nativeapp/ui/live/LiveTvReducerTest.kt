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

    @Test fun leftMovesFromPlaybackToChannelsAndCategoriesWithoutChangingVideoSurface() {
        val playback = LiveTvUiState(mode = LiveTvMode.FULLSCREEN_PLAYBACK, channelIds = channels)

        val channelsLayer = LiveTvReducer.reduce(playback, LiveTvAction.Dpad(LiveTvDirection.LEFT))
        val categoriesLayer = LiveTvReducer.reduce(channelsLayer, LiveTvAction.Dpad(LiveTvDirection.LEFT))

        assertEquals(LiveTvMode.CHANNEL_LIST_VISIBLE, channelsLayer.mode)
        assertEquals(LiveTvVideoSurfaceMode.FULLSCREEN, channelsLayer.videoSurfaceMode)
        assertEquals(LiveTvMode.CATEGORY_PANEL_VISIBLE, categoriesLayer.mode)
    }

    @Test fun rightMovesFromChannelsToEpgAndBackReturnsOneLayerAtATime() {
        val channelsLayer = LiveTvUiState(mode = LiveTvMode.CHANNEL_LIST_VISIBLE, channelIds = channels)

        val guide = LiveTvReducer.reduce(channelsLayer, LiveTvAction.Dpad(LiveTvDirection.RIGHT))
        val backToChannels = LiveTvReducer.reduce(guide, LiveTvAction.Back)
        val fullscreen = LiveTvReducer.reduce(backToChannels, LiveTvAction.Back)

        assertEquals(LiveTvMode.EPG_VISIBLE, guide.mode)
        assertEquals(LiveTvMode.CHANNEL_LIST_VISIBLE, backToChannels.mode)
        assertEquals(LiveTvMode.FULLSCREEN_PLAYBACK, fullscreen.mode)
    }

    @Test fun backClosesContextMenuToExactOpeningLayer() {
        val menu = LiveTvUiState(
            mode = LiveTvMode.CONTEXT_MENU_VISIBLE,
            openedFromMode = LiveTvMode.EPG_VISIBLE,
            channelIds = channels,
        )

        val guide = LiveTvReducer.reduce(menu, LiveTvAction.Back)

        assertEquals(LiveTvMode.EPG_VISIBLE, guide.mode)
    }

    @Test fun okFromFullscreenShowsProgramInfoAndBackRestoresCleanPlayback() {
        val fullscreen = LiveTvUiState(mode = LiveTvMode.FULLSCREEN_PLAYBACK, channelIds = channels)

        val info = LiveTvReducer.reduce(fullscreen, LiveTvAction.Ok)
        val clean = LiveTvReducer.reduce(info, LiveTvAction.Back)

        assertEquals(LiveTvMode.PROGRAM_INFO_VISIBLE, info.mode)
        assertEquals(LiveTvMode.FULLSCREEN_PLAYBACK, clean.mode)
    }

    @Test fun okFromListStartsBufferingButDoesNotMarkPlayingUntilPlaybackReady() {
        val state = LiveTvUiState(
            mode = LiveTvMode.CHANNEL_LIST_VISIBLE,
            channelIds = channels,
            focusedChannelId = 20L,
            selectedChannelId = 10L,
            playingChannelId = 10L,
        )

        val buffering = LiveTvReducer.reduce(state, LiveTvAction.Ok)
        val ready = LiveTvReducer.reduce(buffering, LiveTvAction.PlaybackReady)

        assertEquals(LiveTvMode.BUFFERING, buffering.mode)
        assertEquals(20L, buffering.selectedChannelId)
        assertEquals(10L, buffering.playingChannelId)
        assertEquals(LiveTvMode.FULLSCREEN_PLAYBACK, ready.mode)
        assertEquals(20L, ready.playingChannelId)
    }

    @Test fun longOkAlwaysOpensContextMenuAndBackRestoresFocusLayer() {
        val state = LiveTvUiState(mode = LiveTvMode.CHANNEL_LIST_VISIBLE, channelIds = channels, focusedProgramId = null)

        val menu = LiveTvReducer.reduce(state, LiveTvAction.LongOk)
        val restored = LiveTvReducer.reduce(menu, LiveTvAction.Back)

        assertEquals(LiveTvMode.CONTEXT_MENU_VISIBLE, menu.mode)
        assertEquals(LiveTvMode.CHANNEL_LIST_VISIBLE, menu.openedFromMode)
        assertEquals(LiveTvMode.CHANNEL_LIST_VISIBLE, restored.mode)
    }
}
