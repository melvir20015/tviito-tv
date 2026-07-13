package com.ultratv.tv.nativeapp.ui.live

/** Explicit modes for the Live TV experience. */
enum class LiveTvMode {
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
    PLAYBACK_ERROR,
}

enum class LiveTvDirection { UP, DOWN, LEFT, RIGHT }

enum class LiveTvVideoSurfaceMode { FULLSCREEN, PREVIEW, HIDDEN }

data class LiveTvLoadState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class LiveTvUiState(
    val mode: LiveTvMode = LiveTvMode.PLAYER_FULLSCREEN,
    val channelIds: List<Long> = emptyList(),
    val focusedChannelId: Long? = channelIds.firstOrNull(),
    val selectedChannelId: Long? = focusedChannelId,
    val playingChannelId: Long? = selectedChannelId,
    val focusedProgramId: Long? = null,
    val activeGroupId: String? = null,
    val activePlaylistId: String? = null,
    val lastChannelByGroup: Map<String, Long> = emptyMap(),
    val verticalPositionByGroup: Map<String, Int> = emptyMap(),
    val epgHorizontalOffsetMs: Long = 0L,
    val openedFromMode: LiveTvMode? = null,
    val videoSurfaceMode: LiveTvVideoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN,
    val previewState: LiveTvLoadState = LiveTvLoadState(),
    val playerState: LiveTvLoadState = LiveTvLoadState(),
)

sealed interface LiveTvAction {
    data class ChannelsChanged(val channelIds: List<Long>) : LiveTvAction
    data class Dpad(val direction: LiveTvDirection) : LiveTvAction
    data object Ok : LiveTvAction
    data object LongOk : LiveTvAction
    data object Back : LiveTvAction
    data object LongBack : LiveTvAction
    data object ZapUp : LiveTvAction
    data object ZapDown : LiveTvAction
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
        is LiveTvAction.FocusChannel -> state.focusChannel(action.channelId)
        is LiveTvAction.FocusProgram -> state.copy(focusedProgramId = action.programId)
        is LiveTvAction.SelectGroup -> state.selectGroup(action.groupId, action.fallbackChannelId)
        is LiveTvAction.SelectPlaylist -> state.selectPlaylist(action.playlistId, action.fallbackChannelId)
        is LiveTvAction.OpenPanel -> state.openPanel(action.mode)
        LiveTvAction.ClosePanel -> state.onBack()
        is LiveTvAction.PreviewLoading -> state.copy(previewState = LiveTvLoadState(isLoading = action.isLoading))
        is LiveTvAction.PreviewError -> state.copy(previewState = LiveTvLoadState(errorMessage = action.message))
        is LiveTvAction.PlayerLoading -> state.copy(
            mode = LiveTvMode.LOADING_CHANNEL,
            selectedChannelId = action.channelId ?: state.selectedChannelId,
            playerState = LiveTvLoadState(isLoading = true),
        ).ensureValidFocus()
        is LiveTvAction.PlaybackError -> state.copy(
            mode = LiveTvMode.PLAYBACK_ERROR,
            playerState = LiveTvLoadState(errorMessage = action.message),
            videoSurfaceMode = LiveTvVideoSurfaceMode.HIDDEN,
        ).ensureValidFocus()
        LiveTvAction.PlaybackReady -> state.copy(
            mode = LiveTvMode.PLAYER_FULLSCREEN,
            playingChannelId = state.selectedChannelId ?: state.focusedChannelId,
            playerState = LiveTvLoadState(),
            videoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN,
        ).ensureValidFocus()
    }

    private fun LiveTvUiState.onDpad(direction: LiveTvDirection): LiveTvUiState = when (direction) {
        LiveTvDirection.LEFT -> when (mode) {
            LiveTvMode.TV_GUIDE -> copy(mode = LiveTvMode.CHANNEL_LIST_PREVIEW, videoSurfaceMode = LiveTvVideoSurfaceMode.PREVIEW)
            LiveTvMode.CHANNEL_LIST_PREVIEW, LiveTvMode.CHANNEL_LIST_OVERLAY -> copy(mode = LiveTvMode.GROUP_LIST)
            LiveTvMode.GROUP_LIST -> copy(mode = LiveTvMode.ROOT_NAVIGATION)
            else -> this
        }.ensureValidFocus()
        LiveTvDirection.RIGHT -> when (mode) {
            LiveTvMode.ROOT_NAVIGATION -> copy(mode = LiveTvMode.GROUP_LIST)
            LiveTvMode.GROUP_LIST -> copy(mode = LiveTvMode.CHANNEL_LIST_OVERLAY)
            LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW -> copy(mode = LiveTvMode.TV_GUIDE)
            else -> this
        }.ensureValidFocus()
        LiveTvDirection.UP -> moveFocus(step = -1)
        LiveTvDirection.DOWN -> moveFocus(step = 1)
    }

    private fun LiveTvUiState.onOk(): LiveTvUiState = when (mode) {
        LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW, LiveTvMode.TV_GUIDE -> copy(
            selectedChannelId = focusedChannelId,
            mode = LiveTvMode.LOADING_CHANNEL,
            playerState = LiveTvLoadState(isLoading = true),
        ).rememberFocusedChannel()
        LiveTvMode.PLAYER_FULLSCREEN -> copy(mode = LiveTvMode.PLAYER_CONTROLS_VISIBLE)
        else -> this
    }.ensureValidFocus()

    private fun LiveTvUiState.openContextMenu(): LiveTvUiState = copy(
        mode = if (mode == LiveTvMode.TV_GUIDE && focusedProgramId != null) {
            LiveTvMode.PROGRAM_CONTEXT_MENU
        } else {
            LiveTvMode.CHANNEL_CONTEXT_MENU
        },
        openedFromMode = mode,
    ).ensureValidFocus()

    private fun LiveTvUiState.onBack(): LiveTvUiState = when (mode) {
        LiveTvMode.CHANNEL_CONTEXT_MENU, LiveTvMode.PROGRAM_CONTEXT_MENU -> copy(
            mode = openedFromMode ?: LiveTvMode.CHANNEL_LIST_OVERLAY,
            openedFromMode = null,
        )
        LiveTvMode.PLAYBACK_ERROR, LiveTvMode.LOADING_CHANNEL -> toFullscreen()
        LiveTvMode.TV_GUIDE -> copy(mode = LiveTvMode.CHANNEL_LIST_PREVIEW, videoSurfaceMode = LiveTvVideoSurfaceMode.PREVIEW)
        LiveTvMode.ROOT_NAVIGATION -> copy(mode = LiveTvMode.GROUP_LIST)
        LiveTvMode.GROUP_LIST, LiveTvMode.PLAYLIST_LIST -> copy(mode = LiveTvMode.CHANNEL_LIST_OVERLAY)
        LiveTvMode.CHANNEL_LIST_OVERLAY, LiveTvMode.CHANNEL_LIST_PREVIEW, LiveTvMode.PLAYER_CONTROLS_VISIBLE -> toFullscreen()
        LiveTvMode.PLAYER_FULLSCREEN -> this
    }.ensureValidFocus()

    private fun LiveTvUiState.toFullscreen(): LiveTvUiState = copy(
        mode = LiveTvMode.PLAYER_FULLSCREEN,
        openedFromMode = null,
        videoSurfaceMode = LiveTvVideoSurfaceMode.FULLSCREEN,
        previewState = LiveTvLoadState(),
    ).ensureValidFocus()

    private fun LiveTvUiState.openPanel(newMode: LiveTvMode): LiveTvUiState = copy(
        mode = newMode,
        openedFromMode = if (newMode.name.endsWith("CONTEXT_MENU")) mode else openedFromMode,
        videoSurfaceMode = if (newMode == LiveTvMode.PLAYER_FULLSCREEN) LiveTvVideoSurfaceMode.FULLSCREEN else videoSurfaceMode,
    ).ensureValidFocus()

    private fun LiveTvUiState.selectGroup(groupId: String, fallbackChannelId: Long?): LiveTvUiState {
        val restored = lastChannelByGroup[groupId] ?: fallbackChannelId ?: channelIds.firstOrNull()
        return copy(activeGroupId = groupId, focusedChannelId = restored, selectedChannelId = restored, mode = LiveTvMode.CHANNEL_LIST_OVERLAY)
            .ensureValidFocus()
    }

    private fun LiveTvUiState.selectPlaylist(playlistId: String, fallbackChannelId: Long?): LiveTvUiState = copy(
        activePlaylistId = playlistId,
        focusedChannelId = fallbackChannelId ?: focusedChannelId ?: channelIds.firstOrNull(),
        mode = LiveTvMode.GROUP_LIST,
    ).ensureValidFocus()

    private fun LiveTvUiState.focusChannel(channelId: Long): LiveTvUiState = if (channelIds.isEmpty() || channelId in channelIds) {
        copy(focusedChannelId = channelId).rememberFocusedChannel()
    } else {
        this
    }.ensureValidFocus()

    private fun LiveTvUiState.moveFocus(step: Int): LiveTvUiState {
        if (channelIds.isEmpty()) return this
        val currentIndex = channelIds.indexOf(focusedChannelId).takeIf { it >= 0 } ?: 0
        val nextIndex = (currentIndex + step).coerceIn(channelIds.indices)
        return copy(focusedChannelId = channelIds[nextIndex]).rememberFocusedChannel()
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
            mode = LiveTvMode.LOADING_CHANNEL,
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
