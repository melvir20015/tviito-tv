package com.ultratv.tv.nativeapp

import com.ultratv.tv.nativeapp.data.prefs.UserPrefs
import com.ultratv.tv.nativeapp.update.UpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SafeRemoteDefaultsTest {
    @Test
    fun `fork builds default to telemetry disabled and unconfigured`() {
        assertFalse(UserPrefs().telemetryEnabled)
        assertFalse(RemoteLog.telemetryEnabled)
        assertFalse(RemoteLog.isConfigured)
        assertEquals("", BuildConfig.LOG_URL)
        assertEquals("", BuildConfig.LOG_TOKEN)
    }

    @Test
    fun `fork builds default to upstream update checks disabled`() {
        assertFalse(BuildConfig.AUTO_UPDATE_ENABLED)
        assertEquals("", BuildConfig.UPDATE_REPO)
        assertEquals("", BuildConfig.UPDATE_APK_NAME)
        assertFalse(UpdateChecker.isConfigured)
    }
}
