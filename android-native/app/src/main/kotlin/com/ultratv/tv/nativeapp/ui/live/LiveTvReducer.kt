package com.ultratv.tv.nativeapp.ui.live

import com.ultratv.tv.nativeapp.data.prefs.LiveLastMode
import com.ultratv.tv.nativeapp.data.prefs.UserPrefs

/** Explicit modes for the Live TV experience. */
enum class LiveTvMode {
    /** Reproducción limpia a pantalla completa; no hay barras ni paneles permanentes. */
    FULLSCREEN_PLAYBACK,
    /** Overlay inferior temporal de información del canal/programa. */
    PROGRAM_INFO_VISIBLE,
    /** Fila horizontal de canales recientes sobre el video. */
    RECENT_CHANNELS_VISIBLE,
    /** Lista vertical de canales sobre el lado izquierdo. */
    CHANNEL_LIST_VISIBLE,
    /** Panel adicional de categorías abierto desde la izquierda. */
    CATEGORY_PANEL_VISIBLE,
    /** Guía EPG como capa sobre el reproductor. */
    EPG_VISIBLE,
    /** Detalles ampliados del programa enfocado. */
    PROGRAM_DETAILS_VISIBLE,
    /** Menú contextual lateral abierto desde lista, guía o info. */
    CONTEXT_MENU_VISIBLE,
    /** Cambio de stream en curso sin desmontar el reproductor. */
    BUFFERING,
    PLAYBACK_ERROR,

    // Alias legados conservados mientras se migra LiveScreen por etapas.
    PLAYER_FULLSCREEN,
    PLAYER_CONTROLS_VISIBLE,
    CHANNEL_LIST_OVERLAY,
    CHANNEL_LIST_PREVIEW,
    TV_GUIDE,
    GROUP_LIST,
    PLAYLIST_LIST,
    ROOT_NAVIGATION,
    CHANNEL_CONTEXT_MENU,
    PROGRAM_CONTEXT_MENU,
    LOADING_CHANNEL,
}

enum class LiveTvDirection { UP, DOWN, LEFT, RIGHT }

enum class LiveTvVideoSurfaceMode { FULLSCREEN, PREVIEW, HIDDEN }

data class LiveTvLoadState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class LiveTvUiState(
    val mode: LiveTvMode = LiveTvMode.FULLSCREEN_PLAYBACK,
    val channelIds: List<Long> = emptyList(),
    val focusedChannelId: Long? = channelIds.firstOrNull(),
    val selectedChannelId: Long? = focusedChannelId,
    val playingChannelId: Long? = selectedChannelId,
    val focusedProgramId: Long? = null,
    val activeGroupId: String? = null,
    val activePlaylistId: String? = null,
    val lastChannelByGroup: Map<String, Long> = emptyMap(),
    val verticalPositionByGroup: Map<String, Int> = emptyMap(),
    val previousChannelRemoteId: String? = null,
    val autoplayPreview: Boolean = true,
    val stayOnGuide: Boolean = false,
    val overlayAlpha: Float = 0.88f,
    val controlsTimeoutMs: Long = 5_000L,
    val accentColor: String = "",
    val fontScale: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val previewDebounceMs: Long = 350L,
    val buttonAssignments: Map<String, String> = emptyMap(),
    val epgHorizontalOffsetMs: Long = 0L,
    val openedFromMode: LiveTvMode? = null,
    val focusedChannelByMode: Map<LiveTvMode, Long> = emptyMap(),
    val videoSurfaceMode: LiveTvVideoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN,
    val previewState: LiveTvLoadState = LiveTvLoadState(),
    val playerState: LiveTvLoadState = LiveTvLoadState(),
)

fun UserPrefs.toLiveTvUiState(
    channelIds: List<Long> = emptyList(),
    lastChannelId: Long? = null,
): LiveTvUiState {
    val initialMode = when (liveLastMode) {
        LiveLastMode.PLAYER_FULLSCREEN -> LiveTvMode.FULLSCREEN_PLAYBACK
        LiveLastMode.CHANNEL_LIST -> LiveTvMode.CHANNEL_LIST_VISIBLE
        LiveLastMode.TV_GUIDE -> LiveTvMode.EPG_VISIBLE
    }
    val initialChannelId = lastChannelId ?: channelIds.firstOrNull()
    return LiveTvUiState(
        mode = if (liveStayOnGuide) LiveTvMode.EPG_VISIBLE else initialMode,
        channelIds = channelIds,
        focusedChannelId = initialChannelId,
        selectedChannelId = initialChannelId,
        playingChannelId = initialChannelId,
        activeGroupId = liveLastGroupId.ifBlank { null },
        activePlaylistId = liveLastPlaylistId.ifBlank { null },
        previousChannelRemoteId = livePreviousChannelRemoteId.ifBlank { null },
        autoplayPreview = liveAutoplayPreview,
        stayOnGuide = liveStayOnGuide,
        overlayAlpha = liveOverlayAlpha,
        controlsTimeoutMs = liveControlsTimeoutMs,
        accentColor = liveAccentColor,
        fontScale = liveFontScale,
        reduceMotion = liveReduceMotion,
        previewDebounceMs = livePreviewDebounceMs,
        verticalPositionByGroup = liveGroupPositions,
        buttonAssignments = liveButtonAssignments,
        videoSurfaceMode = if (liveStayOnGuide || initialMode != LiveTvMode.FULLSCREEN_PLAYBACK) {
            LiveTvVideoSurfaceMode.PREVIEW
        } else {
            LiveTvVideoSurfaceMode.FULLSCREEN
        },
    )
}

sealed interface LiveTvAction {
    data class ChannelsChanged(val channelIds: List<Long>) : LiveTvAction
    data class Dpad(val direction: LiveTvDirection) : LiveTvAction
    data object Ok : LiveTvAction
    data object LongOk : LiveTvAction
    data object Back : LiveTvAction
    data object LongBack : LiveTvAction
    data object ZapUp : LiveTvAction
    data object ZapDown : LiveTvAction
    data class NumberInput(val digit: Int) : LiveTvAction
    data class FocusChannel(val channelId: Long) : LiveTvAction
    data class FocusProgram(val programId: Long?) : LiveTvAction
    data class SelectGroup(val groupId: String, val fallbackChannelId: Long? = null) : LiveTvAction
    data class SelectPlaylist(val playlistId: String, val fallbackChannelId: Long? = null) : LiveTvAction
    data class OpenPanel(val mode: LiveTvMode) : LiveTvAction
    data object ClosePanel : LiveTvAction
    data class PreviewLoading(val isLoading: Boolean) : LiveTvAction
    data class PreviewError(val message: String?) : LiveTvAction
    data class PlayerLoading(val channelId: Long? = null) : LiveTvAction
    data class PlaybackError(val message: String) : LiveTvAction
    data object PlaybackReady : LiveTvAction
}

object LiveTvReducer {
    fun reduce(state: LiveTvUiState, action: LiveTvAction): LiveTvUiState = when (action) {
        is LiveTvAction.ChannelsChanged -> state.copy(channelIds = action.channelIds).ensureValidFocus()
        is LiveTvAction.Dpad -> state.onDpad(action.direction)
        LiveTvAction.Ok -> state.onOk()
        LiveTvAction.LongOk -> state.openContextMenu()
        LiveTvAction.Back -> state.onBack()
        LiveTvAction.LongBack -> state.toFullscreen()
        LiveTvAction.ZapUp -> state.zap(step = -1)
        LiveTvAction.ZapDown -> state.zap(step = 1)
        is LiveTvAction.NumberInput -> state
        is LiveTvAction.FocusChannel -> state.focusChannel(action.channelId)
        is LiveTvAction.FocusProgram -> state.copy(focusedProgramId = action.programId)
        is LiveTvAction.SelectGroup -> state.selectGroup(action.groupId, action.fallbackChannelId)
        is LiveTvAction.SelectPlaylist -> state.selectPlaylist(action.playlistId, action.fallbackChannelId)
        is LiveTvAction.OpenPanel -> state.openPanel(action.mode)
        LiveTvAction.ClosePanel -> state.onBack()
        is LiveTvAction.PreviewLoading -> state.copy(previewState = LiveTvLoadState(isLoading = action.isLoading))
        is LiveTvAction.PreviewError -> state.copy(previewState = LiveTvLoadState(errorMessage = action.message))
        is LiveTvAction.PlayerLoading -> state.copy(
            mode = LiveTvMode.BUFFERING,
            selectedChannelId = action.channelId ?: state.selectedChannelId,
            playerState = LiveTvLoadState(isLoading = true),
        ).ensureValidFocus()
        is LiveTvAction.PlaybackError -> state.copy(
            mode = LiveTvMode.PLAYBACK_ERROR,
            playerState = LiveTvLoadState(errorMessage = action.message),
            videoSurfaceMode = LiveTvVideoSurfaceMode.HIDDEN,
        ).ensureValidFocus()
        LiveTvAction.PlaybackReady -> state.copy(
            mode = LiveTvMode.FULLSCREEN_PLAYBACK,
            playingChannelId = state.selectedChannelId ?: state.focusedChannelId,
            playerState = LiveTvLoadState(),
            videoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN,
        ).ensureValidFocus()
    }

    private fun LiveTvUiState.onDpad(direction: LiveTvDirection): LiveTvUiState = when (direction) {
        LiveTvDirection.LEFT -> when (mode) {
            LiveTvMode.FULLSCREEN_PLAYBACK, LiveTvMode.PROGRAM_INFO_VISIBLE, LiveTvMode.PLAYER_FULLSCREEN, LiveTvMode.PLAYER_CONTROLS_VISIBLE -> switchMode(LiveTvMode.CHANNEL_LIST_VISIBLE)
            LiveTvMode.EPG_VISIBLE, LiveTvMode.TV_GUIDE -> switchMode(LiveTvMode.CHANNEL_LIST_VISIBLE).copy(videoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN)
            LiveTvMode.CHANNEL_LIST_VISIBLE, LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW -> switchMode(LiveTvMode.CATEGORY_PANEL_VISIBLE)
            LiveTvMode.CATEGORY_PANEL_VISIBLE -> this
            LiveTvMode.RECENT_CHANNELS_VISIBLE -> moveFocus(step = -1)
            else -> this
        }.ensureValidFocus()
        LiveTvDirection.RIGHT -> when (mode) {
            LiveTvMode.CATEGORY_PANEL_VISIBLE, LiveTvMode.GROUP_LIST, LiveTvMode.ROOT_NAVIGATION -> switchMode(LiveTvMode.CHANNEL_LIST_VISIBLE)
            LiveTvMode.CHANNEL_LIST_VISIBLE, LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW -> switchMode(LiveTvMode.EPG_VISIBLE)
            LiveTvMode.FULLSCREEN_PLAYBACK, LiveTvMode.PROGRAM_INFO_VISIBLE, LiveTvMode.PLAYER_FULLSCREEN, LiveTvMode.PLAYER_CONTROLS_VISIBLE -> switchMode(LiveTvMode.EPG_VISIBLE)
            LiveTvMode.RECENT_CHANNELS_VISIBLE -> moveFocus(step = 1)
            else -> this
        }.ensureValidFocus()
        LiveTvDirection.UP -> moveFocus(step = -1)
        LiveTvDirection.DOWN -> moveFocus(step = 1)
    }

    private fun LiveTvUiState.onOk(): LiveTvUiState = when (mode) {
        LiveTvMode.CHANNEL_LIST_VISIBLE, LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW, LiveTvMode.EPG_VISIBLE, LiveTvMode.TV_GUIDE, LiveTvMode.RECENT_CHANNELS_VISIBLE -> copy(
            selectedChannelId = focusedChannelId,
            mode = LiveTvMode.BUFFERING,
            playerState = LiveTvLoadState(isLoading = true),
        ).rememberFocusedChannel()
        LiveTvMode.CATEGORY_PANEL_VISIBLE -> switchMode(LiveTvMode.CHANNEL_LIST_VISIBLE)
        LiveTvMode.FULLSCREEN_PLAYBACK, LiveTvMode.PLAYER_FULLSCREEN -> switchMode(LiveTvMode.PROGRAM_INFO_VISIBLE)
        else -> this
    }.ensureValidFocus()

    private fun LiveTvUiState.openContextMenu(): LiveTvUiState = rememberFocusedChannelForMode().copy(
        mode = LiveTvMode.CONTEXT_MENU_VISIBLE,
        openedFromMode = mode,
    ).ensureValidFocus()

    private fun LiveTvUiState.onBack(): LiveTvUiState = when (mode) {
        LiveTvMode.CONTEXT_MENU_VISIBLE, LiveTvMode.CHANNEL_CONTEXT_MENU, LiveTvMode.PROGRAM_CONTEXT_MENU -> {
            val restoredMode = openedFromMode ?: LiveTvMode.CHANNEL_LIST_VISIBLE
            copy(
                mode = restoredMode,
                openedFromMode = null,
                focusedChannelId = focusedChannelByMode[restoredMode] ?: focusedChannelId,
            )
        }
        LiveTvMode.PLAYBACK_ERROR, LiveTvMode.BUFFERING, LiveTvMode.LOADING_CHANNEL -> toFullscreen()
        LiveTvMode.CATEGORY_PANEL_VISIBLE, LiveTvMode.GROUP_LIST, LiveTvMode.ROOT_NAVIGATION -> switchMode(LiveTvMode.CHANNEL_LIST_VISIBLE)
        LiveTvMode.PROGRAM_DETAILS_VISIBLE -> switchMode(LiveTvMode.EPG_VISIBLE)
        LiveTvMode.EPG_VISIBLE, LiveTvMode.TV_GUIDE -> switchMode(LiveTvMode.CHANNEL_LIST_VISIBLE).copy(videoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN)
        LiveTvMode.RECENT_CHANNELS_VISIBLE -> openedFromMode?.let { restoredMode ->
            copy(
                mode = restoredMode,
                openedFromMode = null,
                focusedChannelId = focusedChannelByMode[restoredMode] ?: focusedChannelId,
                videoSurfaceMode = if (restoredMode == LiveTvMode.FULLSCREEN_PLAYBACK) LiveTvVideoSurfaceMode.FULLSCREEN else videoSurfaceMode,
            )
        } ?: toFullscreen()
        LiveTvMode.CHANNEL_LIST_VISIBLE, LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW, LiveTvMode.PROGRAM_INFO_VISIBLE, LiveTvMode.PLAYER_CONTROLS_VISIBLE -> toFullscreen()
        LiveTvMode.PLAYLIST_LIST -> switchMode(LiveTvMode.CATEGORY_PANEL_VISIBLE)
        LiveTvMode.FULLSCREEN_PLAYBACK, LiveTvMode.PLAYER_FULLSCREEN -> this
    }.ensureValidFocus()

    private fun LiveTvUiState.toFullscreen(): LiveTvUiState = copy(
        mode = LiveTvMode.FULLSCREEN_PLAYBACK,
        openedFromMode = null,
        videoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN,
        previewState = LiveTvLoadState(),
    ).ensureValidFocus()

    private fun LiveTvUiState.openPanel(newMode: LiveTvMode): LiveTvUiState {
        val remembered = if (mode.isFocusRestorableLayer() && focusedChannelId != null) {
            focusedChannelByMode + (mode to focusedChannelId)
        } else {
            focusedChannelByMode
        }
        return copy(
            mode = newMode,
            focusedChannelId = remembered[newMode] ?: focusedChannelId,
            focusedChannelByMode = remembered,
            openedFromMode = when {
                newMode == LiveTvMode.CONTEXT_MENU_VISIBLE || newMode.name.endsWith("CONTEXT_MENU") -> mode
                newMode == LiveTvMode.RECENT_CHANNELS_VISIBLE -> mode
                else -> openedFromMode
            },
            videoSurfaceMode = if (newMode == LiveTvMode.FULLSCREEN_PLAYBACK) LiveTvVideoSurfaceMode.FULLSCREEN else videoSurfaceMode,
        ).ensureValidFocus()
    }

    private fun LiveTvUiState.switchMode(newMode: LiveTvMode): LiveTvUiState {
        val remembered = if (mode.isFocusRestorableLayer() && focusedChannelId != null) {
            focusedChannelByMode + (mode to focusedChannelId)
        } else {
            focusedChannelByMode
        }
        return copy(
            mode = newMode,
            focusedChannelId = remembered[newMode] ?: focusedChannelId,
            focusedChannelByMode = remembered,
        )
    }

    private fun LiveTvUiState.rememberFocusedChannelForMode(): LiveTvUiState {
        val channelId = focusedChannelId ?: return this
        if (!mode.isFocusRestorableLayer()) return this
        return copy(focusedChannelByMode = focusedChannelByMode + (mode to channelId))
    }

    private fun LiveTvMode.isFocusRestorableLayer(): Boolean = when (this) {
        LiveTvMode.CHANNEL_LIST_VISIBLE,
        LiveTvMode.CATEGORY_PANEL_VISIBLE,
        LiveTvMode.EPG_VISIBLE,
        LiveTvMode.RECENT_CHANNELS_VISIBLE -> true
        else -> false
    }

    private fun LiveTvUiState.selectGroup(groupId: String, fallbackChannelId: Long?): LiveTvUiState {
        val restored = lastChannelByGroup[groupId] ?: fallbackChannelId ?: channelIds.firstOrNull()
        return copy(activeGroupId = groupId, focusedChannelId = restored, selectedChannelId = restored, mode = LiveTvMode.CHANNEL_LIST_VISIBLE)
            .ensureValidFocus()
    }

    private fun LiveTvUiState.selectPlaylist(playlistId: String, fallbackChannelId: Long?): LiveTvUiState = copy(
        activePlaylistId = playlistId,
        focusedChannelId = fallbackChannelId ?: focusedChannelId ?: channelIds.firstOrNull(),
        mode = LiveTvMode.GROUP_LIST,
    ).ensureValidFocus()

    private fun LiveTvUiState.focusChannel(channelId: Long): LiveTvUiState = if (channelIds.isEmpty() || channelId in channelIds) {
        copy(focusedChannelId = channelId).rememberFocusedChannel().rememberFocusedChannelForMode()
    } else {
        this
    }.ensureValidFocus()

    private fun LiveTvUiState.moveFocus(step: Int): LiveTvUiState {
        if (channelIds.isEmpty()) return this
        val currentIndex = channelIds.indexOf(focusedChannelId).takeIf { it >= 0 } ?: 0
        val nextIndex = (currentIndex + step).coerceIn(channelIds.indices)
        return copy(focusedChannelId = channelIds[nextIndex]).rememberFocusedChannel().rememberFocusedChannelForMode()
    }

    private fun LiveTvUiState.zap(step: Int): LiveTvUiState {
        if (channelIds.isEmpty()) return this
        val current = playingChannelId ?: selectedChannelId ?: focusedChannelId
        val currentIndex = channelIds.indexOf(current).takeIf { it >= 0 } ?: 0
        val nextIndex = (currentIndex + step).floorMod(channelIds.size)
        val nextChannelId = channelIds[nextIndex]
        return copy(
            focusedChannelId = nextChannelId,
            selectedChannelId = nextChannelId,
            mode = LiveTvMode.BUFFERING,
            playerState = LiveTvLoadState(isLoading = true),
        ).rememberFocusedChannel()
    }

    private fun LiveTvUiState.rememberFocusedChannel(): LiveTvUiState {
        val groupId = activeGroupId ?: return this
        val channelId = focusedChannelId ?: return this
        return copy(lastChannelByGroup = lastChannelByGroup + (groupId to channelId))
    }

    private fun LiveTvUiState.ensureValidFocus(): LiveTvUiState {
        if (channelIds.isEmpty()) return this
        val validFocus = focusedChannelId?.takeIf { it in channelIds } ?: selectedChannelId?.takeIf { it in channelIds } ?: channelIds.first()
        val validSelected = selectedChannelId?.takeIf { it in channelIds } ?: validFocus
        val validPlaying = playingChannelId?.takeIf { it in channelIds } ?: validSelected
        return copy(focusedChannelId = validFocus, selectedChannelId = validSelected, playingChannelId = validPlaying)
    }

    private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus
}
