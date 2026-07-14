package com.ultratv.tv.nativeapp.data.prefs

import com.ultratv.tv.nativeapp.ui.live.LiveTvMode
import com.ultratv.tv.nativeapp.ui.live.LiveTvVideoSurfaceMode
import com.ultratv.tv.nativeapp.ui.live.toLiveTvUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesLivePrefsTest {
    @Test
    fun `live preferences keep safe defaults`() {
        val prefs = UserPrefs()

        assertEquals("", prefs.liveLastChannelRemoteId)
        assertEquals("", prefs.livePreviousChannelRemoteId)
        assertEquals("", prefs.liveLastGroupId)
        assertEquals("", prefs.liveLastPlaylistId)
        assertEquals(LiveLastMode.PLAYER_FULLSCREEN, prefs.liveLastMode)
        assertTrue(prefs.liveAutoplayPreview)
        assertEquals(false, prefs.liveStayOnGuide)
        assertEquals(0.88f, prefs.liveOverlayAlpha, 0.001f)
        assertEquals(5_000L, prefs.liveControlsTimeoutMs)
        assertEquals("", prefs.liveAccentColor)
        assertEquals(1.0f, prefs.liveFontScale, 0.001f)
        assertEquals(false, prefs.liveReduceMotion)
        assertEquals(350L, prefs.livePreviewDebounceMs)
        assertEquals(emptyMap<String, Int>(), prefs.liveGroupPositions)
        assertEquals(emptyMap<String, String>(), prefs.liveButtonAssignments)
        assertEquals(emptyMap<String, String>(), prefs.liveRemoteActionAssignments)
    }

    @Test
    fun `group position serialization round trips escaped keys and skips invalid positions`() {
        val positions = mapOf(
            "sports" to 12,
            "news=local|east" to 3,
        )

        val encoded = LivePrefsSerialization.serializeGroupPositions(positions)
        assertEquals(positions.toSortedMap(), LivePrefsSerialization.parseGroupPositions(encoded).toSortedMap())
        assertEquals(mapOf("valid" to 8), LivePrefsSerialization.parseGroupPositions("valid=8|bad=NaN|=7|missing"))
    }

    @Test
    fun `button assignment serialization round trips escaped buttons and actions`() {
        val assignments = mapOf(
            "DPAD_CENTER_LONG" to "open_context_menu",
            "KEY=RED|ALT" to "toggle=favorite|live",
        )

        val encoded = LivePrefsSerialization.serializeButtonAssignments(assignments)

        assertEquals(assignments.toSortedMap(), LivePrefsSerialization.parseButtonAssignments(encoded).toSortedMap())
    }

    @Test
    fun `remote action assignment serialization round trips escaped surfaces commands and actions`() {
        val assignments = mapOf(
            "PLAYER:LONG_OK" to "OPEN_CONTEXT_MENU",
            "TV_GUIDE:LEFT" to "MOVE=LEFT|GRID",
        )

        val encoded = LivePrefsSerialization.serializeRemoteActionAssignments(assignments)

        assertEquals(assignments.toSortedMap(), LivePrefsSerialization.parseRemoteActionAssignments(encoded).toSortedMap())
    }

    @Test
    fun `live ui state restores initial values from preferences`() {
        val state = UserPrefs(
            liveLastGroupId = "group-news",
            liveLastPlaylistId = "playlist-main",
            liveLastMode = LiveLastMode.TV_GUIDE,
            livePreviousChannelRemoteId = "previous-remote",
            liveAutoplayPreview = false,
            liveStayOnGuide = true,
            liveOverlayAlpha = 0.42f,
            liveControlsTimeoutMs = 8_000L,
            liveAccentColor = "#00AAFF",
            liveFontScale = 1.2f,
            liveReduceMotion = true,
            livePreviewDebounceMs = 700L,
            liveGroupPositions = mapOf("group-news" to 24),
            liveButtonAssignments = mapOf("BLUE" to "open_guide"),
        ).toLiveTvUiState(channelIds = listOf(10L, 20L), lastChannelId = 20L)

        assertEquals(LiveTvMode.EPG_VISIBLE, state.mode)
        assertEquals(20L, state.focusedChannelId)
        assertEquals("group-news", state.activeGroupId)
        assertEquals("playlist-main", state.activePlaylistId)
        assertEquals("previous-remote", state.previousChannelRemoteId)
        assertEquals(false, state.autoplayPreview)
        assertEquals(true, state.stayOnGuide)
        assertEquals(0.42f, state.overlayAlpha, 0.001f)
        assertEquals(8_000L, state.controlsTimeoutMs)
        assertEquals("#00AAFF", state.accentColor)
        assertEquals(1.2f, state.fontScale, 0.001f)
        assertEquals(true, state.reduceMotion)
        assertEquals(700L, state.previewDebounceMs)
        assertEquals(mapOf("group-news" to 24), state.verticalPositionByGroup)
        assertEquals(mapOf("BLUE" to "open_guide"), state.buttonAssignments)
        assertEquals(LiveTvVideoSurfaceMode.PREVIEW, state.videoSurfaceMode)
    }

    @Test
    fun `blank restored ids remain null in live ui state`() {
        val state = UserPrefs().toLiveTvUiState()

        assertNull(state.activeGroupId)
        assertNull(state.activePlaylistId)
        assertNull(state.previousChannelRemoteId)
    }
}
