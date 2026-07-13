package com.ultratv.tv.nativeapp.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class SpanishCoverageTest {
    @Test
    fun spanishDefinesEveryStringKey() {
        assertEquals(emptyList<StringKey>(), missingSpanishTranslations())
    }

    @Test
    fun userVisibleLanguagesAreLimitedToSystemEnglishAndSpanish() {
        assertEquals(listOf(AppLang.System, AppLang.English, AppLang.Spanish), AppLang.visibleEntries)
    }
}
