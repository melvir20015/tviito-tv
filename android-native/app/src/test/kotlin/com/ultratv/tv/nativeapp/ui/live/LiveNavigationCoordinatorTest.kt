package com.ultratv.tv.nativeapp.ui.live

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveNavigationCoordinatorTest {
    @Test fun categorySelectedMovesToChannelsLevel() {
        val nav = LiveNavigationCoordinator(initialLevel = LiveLevel.Categories)

        assertEquals(7L, nav.selectCategory("sports", 7L))

        assertEquals(LiveLevel.Channels, nav.level)
        assertEquals("sports", nav.selectedCategory)
    }

    @Test fun backFromChannelsReturnsToCategories() {
        val nav = LiveNavigationCoordinator()
        nav.selectCategory("news", 1L)

        assertEquals(LiveNavigationAction.ShowCategories, nav.back())

        assertEquals(LiveLevel.Categories, nav.level)
    }

    @Test fun okOnNonPreviewedChannelMovesToPreview() {
        val nav = LiveNavigationCoordinator(initialLevel = LiveLevel.Channels)

        assertEquals(LiveNavigationAction.ShowPreview, nav.clickChannel(10L))

        assertEquals(LiveLevel.Preview, nav.level)
        assertEquals(10L, nav.previewChannelId)
    }

    @Test fun okOnAlreadyPreviewedChannelRequestsFullscreen() {
        val nav = LiveNavigationCoordinator(initialLevel = LiveLevel.Channels)
        nav.clickChannel(10L)

        assertEquals(LiveNavigationAction.OpenFullscreen, nav.clickChannel(10L))

        assertEquals(LiveLevel.Fullscreen, nav.level)
        assertEquals(10L, nav.previewChannelId)
    }

    @Test fun focusingAnotherChannelDoesNotChangePreview() {
        val nav = LiveNavigationCoordinator(initialLevel = LiveLevel.Channels)
        nav.clickChannel(10L)

        nav.rememberChannel(channelId = 11L)

        assertEquals(10L, nav.previewChannelId)
        assertEquals(11L, nav.restoreChannel())
    }

    @Test fun backFromPreviewReturnsToChannelsAndKeepsPreviewFocusCandidate() {
        val nav = LiveNavigationCoordinator(initialLevel = LiveLevel.Channels)
        nav.clickChannel(21L)

        assertEquals(LiveNavigationAction.ShowChannels, nav.back())

        assertEquals(LiveLevel.Channels, nav.level)
        assertEquals(21L, nav.previewChannelId)
        assertEquals(21L, nav.restoreChannel())
    }

    @Test fun noPreviewInitially() {
        val nav = LiveNavigationCoordinator()
        assertNull(nav.previewChannelId)
    }
}
