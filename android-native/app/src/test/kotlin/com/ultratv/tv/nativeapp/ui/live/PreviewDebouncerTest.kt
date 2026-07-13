package com.ultratv.tv.nativeapp.ui.live

import org.junit.Assert.*
import org.junit.Test

class PreviewDebouncerTest {
    @Test fun waitsConfiguredDelayBeforeActivating() {
        val d = PreviewDebouncer(450)
        assertFalse(d.request(10, 1_000))
        assertFalse(d.request(10, 1_300))
        assertTrue(d.request(10, 1_450))
    }

    @Test fun ignoresAlreadyActiveChannel() {
        val d = PreviewDebouncer(450)
        d.activate(7)
        assertFalse(d.request(7, 2_000))
    }

    @Test fun newChannelRestartsPendingWindow() {
        val d = PreviewDebouncer(450)
        assertFalse(d.request(1, 1_000))
        assertFalse(d.request(2, 1_300))
        assertFalse(d.request(2, 1_700))
        assertTrue(d.request(2, 1_751))
    }
}
