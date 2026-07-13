package com.ultratv.tv.nativeapp.ui.live

enum class LiveLevel { Categories, Channels, Preview, Fullscreen }

enum class LiveNavigationAction { ShowCategories, ShowChannels, ShowPreview, OpenFullscreen }

class LiveNavigationCoordinator(
    initialLevel: LiveLevel = LiveLevel.Categories,
    initialCategory: String = CATEGORY_ALL,
) {
    var level: LiveLevel = initialLevel
        private set
    var selectedCategory: String = initialCategory
        private set
    var previewChannelId: Long? = null
        private set
    private val lastChannelByCategory = linkedMapOf<String, Long>()

    fun selectCategory(categoryId: String, fallbackChannelId: Long? = null): Long? {
        selectedCategory = categoryId
        level = LiveLevel.Channels
        return lastChannelByCategory[categoryId] ?: fallbackChannelId
    }

    fun rememberChannel(categoryId: String = selectedCategory, channelId: Long) {
        lastChannelByCategory[categoryId] = channelId
    }

    fun restoreChannel(categoryId: String = selectedCategory, fallbackChannelId: Long? = null): Long? =
        lastChannelByCategory[categoryId] ?: fallbackChannelId

    fun clickChannel(channelId: Long): LiveNavigationAction {
        rememberChannel(selectedCategory, channelId)
        return if (previewChannelId == channelId) {
            level = LiveLevel.Fullscreen
            LiveNavigationAction.OpenFullscreen
        } else {
            previewChannelId = channelId
            level = LiveLevel.Preview
            LiveNavigationAction.ShowPreview
        }
    }

    fun back(): LiveNavigationAction {
        level = when (level) {
            LiveLevel.Categories -> LiveLevel.Categories
            LiveLevel.Channels -> LiveLevel.Categories
            LiveLevel.Preview -> LiveLevel.Channels
            LiveLevel.Fullscreen -> LiveLevel.Preview
        }
        return when (level) {
            LiveLevel.Categories -> LiveNavigationAction.ShowCategories
            LiveLevel.Channels -> LiveNavigationAction.ShowChannels
            LiveLevel.Preview -> LiveNavigationAction.ShowPreview
            LiveLevel.Fullscreen -> LiveNavigationAction.OpenFullscreen
        }
    }

    fun restoreFromFullscreen() {
        level = if (previewChannelId == null) LiveLevel.Channels else LiveLevel.Preview
    }
}

// Compatibility aliases for older tests/call sites while the Live screen uses
// the level-based coordinator.
typealias LivePanel = LiveLevel
typealias FocusCoordinator = LiveNavigationCoordinator
