package com.ultratv.tv.nativeapp.ui.live

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusCoordinatorTest {
    @Test fun leftRightStayWithinPanelBounds() {
        val f = FocusCoordinator(initialPanel = LivePanel.CATEGORY)
        assertEquals(LivePanel.CATEGORY, f.moveLeft())
        assertEquals(LivePanel.CHANNELS, f.moveRight(hasChannels = true))
        assertEquals(LivePanel.PREVIEW, f.moveRight(hasChannels = true))
        assertEquals(LivePanel.PREVIEW, f.moveRight(hasChannels = true))
        assertEquals(LivePanel.CHANNELS, f.moveLeft())
    }

    @Test fun categoryRestoresLastRememberedChannel() {
        val f = FocusCoordinator()
        assertEquals(1L, f.selectCategory("sports", 1L))
        f.rememberChannel("sports", 22L)
        f.selectCategory("news", 5L)
        assertEquals(22L, f.selectCategory("sports", 1L))
    }
}
