package com.ultratv.tv.nativeapp.ui.live

import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.repo.PlaybackContext
import com.ultratv.tv.nativeapp.data.repo.ProviderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PreviewPlaybackState {
    data object Idle : PreviewPlaybackState
    data object Locked : PreviewPlaybackState
    data object Connecting : PreviewPlaybackState
    data object Buffering : PreviewPlaybackState
    data object Playing : PreviewPlaybackState
    data object Reconnecting : PreviewPlaybackState
    data class Failed(val reason: String? = null) : PreviewPlaybackState
    data class Unsupported(val reason: String? = null) : PreviewPlaybackState
}

data class PreviewRequest(
    val channelId: Long,
    val providerId: Long,
    val remoteId: String,
    val title: String,
    val logo: String?,
    val storedUrl: String,
) {
    companion object {
        fun from(channel: ChannelEntity) = PreviewRequest(
            channelId = channel.id,
            providerId = channel.providerId,
            remoteId = channel.remoteId,
            title = channel.name,
            logo = channel.logo,
            storedUrl = channel.streamUrl,
        )
    }
}

interface PreviewPlayerController {
    fun stop()
    fun play(url: String)
}

class PreviewCoordinator private constructor(
    private val playback: PlaybackContext,
    private val defaultResolver: suspend (Long, String) -> String,
) {
    @Inject constructor(
        provider: ProviderRepository,
        playback: PlaybackContext,
    ) : this(playback, { id, url -> provider.resolvePlayUrl(id, url) })

    internal constructor(
        playback: PlaybackContext,
    ) : this(playback, { _, url -> url })

    private val _state = MutableStateFlow<PreviewPlaybackState>(PreviewPlaybackState.Idle)
    val state: StateFlow<PreviewPlaybackState> = _state.asStateFlow()

    private var requestJob: Job? = null
    private var currentChannelId: Long? = null

    fun request(
        scope: CoroutineScope,
        channel: ChannelEntity?,
        locked: Boolean,
        player: PreviewPlayerController,
        debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
    ) {
        request(scope, channel?.let(PreviewRequest::from), locked, player, debounceMillis, defaultResolver)
    }

    internal fun request(
        scope: CoroutineScope,
        request: PreviewRequest?,
        locked: Boolean,
        player: PreviewPlayerController,
        debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
        resolver: suspend (Long, String) -> String,
    ) {
        requestJob?.cancel()
        if (request == null || locked) {
            currentChannelId = null
            player.stop()
            _state.value = if (locked) PreviewPlaybackState.Locked else PreviewPlaybackState.Idle
            return
        }
        if (isActivePlayback(request) || request.channelId == currentChannelId) return

        currentChannelId = request.channelId
        player.stop()
        _state.value = PreviewPlaybackState.Connecting
        requestJob = scope.launch {
            delay(debounceMillis)
            val resolved = runCatching { resolver(request.channelId, request.storedUrl) }
                .getOrElse { error ->
                    currentChannelId = null
                    _state.value = PreviewPlaybackState.Failed(error.message)
                    return@launch
                }
            if (!isSupported(resolved)) {
                currentChannelId = null
                _state.value = PreviewPlaybackState.Unsupported()
                return@launch
            }
            runCatching { player.play(resolved) }
                .onFailure { error ->
                    currentChannelId = null
                    _state.value = PreviewPlaybackState.Failed(error.message)
                }
        }
    }

    fun onBuffering() {
        _state.value = if (_state.value == PreviewPlaybackState.Playing) {
            PreviewPlaybackState.Reconnecting
        } else {
            PreviewPlaybackState.Buffering
        }
    }

    fun onPlaying() {
        _state.value = PreviewPlaybackState.Playing
    }

    fun onError(reason: String?) {
        currentChannelId = null
        _state.value = PreviewPlaybackState.Failed(reason)
    }

    fun clear(player: PreviewPlayerController? = null) {
        requestJob?.cancel()
        requestJob = null
        currentChannelId = null
        player?.stop()
        _state.value = PreviewPlaybackState.Idle
    }

    private fun isActivePlayback(request: PreviewRequest): Boolean {
        val current = playback.current.value ?: return false
        return current.kind == "LIVE" &&
            current.providerId == request.providerId &&
            current.remoteId == request.remoteId
    }

    private fun isSupported(url: String): Boolean = url.startsWith("http://") ||
        url.startsWith("https://") ||
        url.startsWith("rtmp://") ||
        url.startsWith("rtsp://") ||
        url.startsWith("file://")

    companion object {
        const val DEFAULT_DEBOUNCE_MILLIS = 450L
    }
}
