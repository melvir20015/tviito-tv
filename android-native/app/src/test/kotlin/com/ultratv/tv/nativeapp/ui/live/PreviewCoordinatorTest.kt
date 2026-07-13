package com.ultratv.tv.nativeapp.ui.live

import com.ultratv.tv.nativeapp.data.repo.PlaybackContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreviewCoordinatorTest {
    private val playback = PlaybackContext()

    @Test
    fun debounce450ms_waitsBeforeOpeningStream() = runTest {
        val coordinator = coordinator()
        val player = FakePreviewPlayer()
        var resolves = 0

        coordinator.request(backgroundScope, request(1), locked = false, player = player) { _, url ->
            resolves += 1
            url
        }

        advanceTimeBy(449)
        runCurrent()
        assertEquals(0, resolves)
        assertEquals(emptyList<String>(), player.played)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, resolves)
        assertEquals(listOf("https://example.test/1.m3u8"), player.played)
    }

    @Test
    fun requestingNewChannel_cancelsPreviousChannel() = runTest {
        val coordinator = coordinator()
        val player = FakePreviewPlayer()
        val resolved = mutableListOf<Long>()

        coordinator.request(backgroundScope, request(1), locked = false, player = player) { id, url ->
            resolved += id
            url
        }
        advanceTimeBy(200)
        coordinator.request(backgroundScope, request(2), locked = false, player = player) { id, url ->
            resolved += id
            url
        }
        advanceTimeBy(450)
        runCurrent()

        assertEquals(listOf(2L), resolved)
        assertEquals(listOf("https://example.test/2.m3u8"), player.played)
    }

    @Test
    fun noAbreDosStreamsSimultaneos() = runTest {
        val coordinator = coordinator()
        val player = FakePreviewPlayer()

        coordinator.request(backgroundScope, request(1), locked = false, player = player) { _, url -> url }
        advanceTimeBy(450)
        runCurrent()
        coordinator.request(backgroundScope, request(2), locked = false, player = player) { _, url -> url }
        advanceTimeBy(450)
        runCurrent()

        assertEquals(listOf("stop", "play:https://example.test/1.m3u8", "stop", "play:https://example.test/2.m3u8"), player.events)
    }

    @Test
    fun noReiniciaElCanalActivo() = runTest {
        playback.set(PlaybackContext.Item(1, "LIVE", "remote-1", "Canal 1", null, "https://example.test/1.m3u8"))
        val coordinator = coordinator()
        val player = FakePreviewPlayer()
        var resolves = 0

        coordinator.request(backgroundScope, request(1), locked = false, player = player) { _, url ->
            resolves += 1
            url
        }
        advanceTimeBy(450)
        runCurrent()

        assertEquals(0, resolves)
        assertEquals(emptyList<String>(), player.events)
    }

    @Test
    fun errorDeStreamNoBloqueaNuevaNavegacion() = runTest {
        val coordinator = coordinator()
        val player = FakePreviewPlayer()

        coordinator.request(backgroundScope, request(1), locked = false, player = player) { _, _ ->
            error("fallo de proveedor")
        }
        advanceTimeBy(450)
        runCurrent()
        assertTrue(coordinator.state.value is PreviewPlaybackState.Failed)

        coordinator.request(backgroundScope, request(2), locked = false, player = player) { _, url -> url }
        advanceTimeBy(450)
        runCurrent()

        assertEquals(listOf("https://example.test/2.m3u8"), player.played)
        assertEquals(PreviewPlaybackState.Connecting, coordinator.state.value)
    }

    private fun coordinator() = PreviewCoordinator(playback)

    private fun request(id: Long) = PreviewRequest(
        channelId = id,
        providerId = 1,
        remoteId = "remote-$id",
        title = "Canal $id",
        logo = null,
        storedUrl = "https://example.test/$id.m3u8",
    )

    private class FakePreviewPlayer : PreviewPlayerController {
        val events = mutableListOf<String>()
        val played: List<String> get() = events.mapNotNull { it.removePrefix("play:").takeIf { value -> value != it } }
        override fun stop() { events += "stop" }
        override fun play(url: String) { events += "play:$url" }
    }
}
