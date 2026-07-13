package com.ultratv.tv.nativeapp.ui.live

class PreviewDebouncer(
    val debounceMs: Long = DEFAULT_DEBOUNCE_MS,
) {
    private var activeChannelId: Long? = null
    private var pendingChannelId: Long? = null
    private var pendingSinceMs: Long = 0L

    fun request(channelId: Long, nowMs: Long): Boolean {
        if (channelId == activeChannelId) return false
        if (channelId != pendingChannelId) {
            pendingChannelId = channelId
            pendingSinceMs = nowMs
            return false
        }
        return nowMs - pendingSinceMs >= debounceMs
    }

    fun activate(channelId: Long) {
        activeChannelId = channelId
        pendingChannelId = null
    }

    fun activeChannelId(): Long? = activeChannelId

    companion object { const val DEFAULT_DEBOUNCE_MS = 450L }
}
