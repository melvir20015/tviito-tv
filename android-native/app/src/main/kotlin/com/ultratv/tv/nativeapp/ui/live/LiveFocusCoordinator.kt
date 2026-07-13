package com.ultratv.tv.nativeapp.ui.live

enum class LivePanel { CATEGORY, CHANNELS, PREVIEW }

class FocusCoordinator(
    initialPanel: LivePanel = LivePanel.CATEGORY,
    initialCategory: String = CATEGORY_ALL,
) {
    var activePanel: LivePanel = initialPanel
        private set
    var selectedCategory: String = initialCategory
        private set
    private val lastChannelByCategory = linkedMapOf<String, Long>()

    fun moveLeft(): LivePanel {
        activePanel = when (activePanel) {
            LivePanel.CATEGORY -> LivePanel.CATEGORY
            LivePanel.CHANNELS -> LivePanel.CATEGORY
            LivePanel.PREVIEW -> LivePanel.CHANNELS
        }
        return activePanel
    }

    fun moveRight(hasChannels: Boolean): LivePanel {
        activePanel = when (activePanel) {
            LivePanel.CATEGORY -> if (hasChannels) LivePanel.CHANNELS else LivePanel.CATEGORY
            LivePanel.CHANNELS -> LivePanel.PREVIEW
            LivePanel.PREVIEW -> LivePanel.PREVIEW
        }
        return activePanel
    }

    fun selectCategory(categoryId: String, fallbackChannelId: Long? = null): Long? {
        selectedCategory = categoryId
        activePanel = LivePanel.CHANNELS
        return lastChannelByCategory[categoryId] ?: fallbackChannelId
    }

    fun rememberChannel(categoryId: String = selectedCategory, channelId: Long) {
        lastChannelByCategory[categoryId] = channelId
    }

    fun restoreChannel(categoryId: String = selectedCategory, fallbackChannelId: Long? = null): Long? =
        lastChannelByCategory[categoryId] ?: fallbackChannelId
}
