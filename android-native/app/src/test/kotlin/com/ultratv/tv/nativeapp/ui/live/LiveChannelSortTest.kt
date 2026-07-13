package com.ultratv.tv.nativeapp.ui.live

import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.prefs.LiveChannelSortMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveChannelSortTest {
    @Test
    fun providerOrderUsesProviderPositionByDefaultAndIgnoresManualPins() {
        val sorted = sortLiveChannels(
            listOf(
                ch("b", "Beta", providerPosition = 2, userPosition = 1),
                ch("a", "Alpha", providerPosition = 1),
            ),
            emptySet(),
            LiveChannelSortMode.PROVIDER,
        )

        assertEquals(listOf("a", "b"), sorted.map { it.remoteId })
    }

    @Test
    fun favoritesFirstKeepsProviderOrderInsideGroups() {
        val sorted = sortLiveChannels(
            listOf(ch("one", "One", 1), ch("two", "Two", 2), ch("three", "Three", 3)),
            setOf("three", "two"),
            LiveChannelSortMode.FAVORITES_FIRST,
        )

        assertEquals(listOf("two", "three", "one"), sorted.map { it.remoteId })
    }

    @Test
    fun manualPositionsStayAheadOfProviderBlock() {
        val sorted = sortLiveChannels(
            listOf(
                ch("provider-first", "Provider First", providerPosition = 1),
                ch("manual", "Manual", providerPosition = 9, userPosition = 100),
            ),
            emptySet(),
            LiveChannelSortMode.MANUAL,
        )

        assertEquals(listOf("manual", "provider-first"), sorted.map { it.remoteId })
    }

    @Test
    fun alphabeticalModesIgnoreProviderPosition() {
        val channels = listOf(ch("z", "Zulu", 1), ch("a", "alpha", 2))

        assertEquals(listOf("a", "z"), sortLiveChannels(channels, emptySet(), LiveChannelSortMode.ALPHA_ASC).map { it.remoteId })
        assertEquals(listOf("z", "a"), sortLiveChannels(channels, emptySet(), LiveChannelSortMode.ALPHA_DESC).map { it.remoteId })
    }

    private fun ch(remoteId: String, name: String, providerPosition: Int, userPosition: Int = 0) = ChannelEntity(
        providerId = 1,
        remoteId = remoteId,
        name = name,
        logo = null,
        categoryId = null,
        streamUrl = "http://example.invalid/$remoteId",
        providerPosition = providerPosition,
        userPosition = userPosition,
    )
}
