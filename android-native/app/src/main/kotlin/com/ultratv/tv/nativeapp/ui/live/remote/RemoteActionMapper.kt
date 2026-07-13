package com.ultratv.tv.nativeapp.ui.live.remote

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.nativeKeyEvent
import com.ultratv.tv.nativeapp.ui.live.LiveTvAction
import com.ultratv.tv.nativeapp.ui.live.LiveTvDirection
import com.ultratv.tv.nativeapp.ui.live.LiveTvMode
import android.view.KeyEvent as AndroidKeyEvent

/** Logical Live TV surfaces that can have independent remote-control mappings. */
enum class RemoteSurface {
    PLAYER,
    TV_GUIDE,
    CHANNEL_LIST,
    MOVIES,
    TV_SHOWS,
}

/** Normalized remote commands before they are translated to Live TV actions. */
enum class RemoteCommand {
    OK,
    LONG_OK,
    LEFT,
    LONG_LEFT,
    RIGHT,
    UP,
    DOWN,
    BACK,
    LONG_BACK,
    MENU,
    CHANNEL_UP,
    CHANNEL_DOWN,
    NUMBER_INPUT,
}

data class RemoteCommandEvent(
    val command: RemoteCommand,
    val number: Int? = null,
)

object RemoteActionMapper {
    private val playerDefaults: Map<RemoteCommand, LiveTvAction> = mapOf(
        RemoteCommand.OK to LiveTvAction.Ok,
        RemoteCommand.LONG_OK to LiveTvAction.LongOk,
        RemoteCommand.LEFT to LiveTvAction.OpenPanel(LiveTvMode.CHANNEL_LIST_OVERLAY),
        RemoteCommand.LONG_LEFT to LiveTvAction.OpenPanel(LiveTvMode.TV_GUIDE),
        RemoteCommand.RIGHT to LiveTvAction.OpenPanel(LiveTvMode.TV_GUIDE),
        RemoteCommand.UP to LiveTvAction.ZapUp,
        RemoteCommand.DOWN to LiveTvAction.ZapDown,
        RemoteCommand.BACK to LiveTvAction.Back,
        RemoteCommand.LONG_BACK to LiveTvAction.LongBack,
        RemoteCommand.MENU to LiveTvAction.LongOk,
        RemoteCommand.CHANNEL_UP to LiveTvAction.ZapUp,
        RemoteCommand.CHANNEL_DOWN to LiveTvAction.ZapDown,
    )

    private val guideDefaults: Map<RemoteCommand, LiveTvAction> = mapOf(
        RemoteCommand.OK to LiveTvAction.Ok,
        RemoteCommand.LONG_OK to LiveTvAction.LongOk,
        RemoteCommand.LEFT to LiveTvAction.Dpad(LiveTvDirection.LEFT),
        RemoteCommand.LONG_LEFT to LiveTvAction.Dpad(LiveTvDirection.LEFT),
        RemoteCommand.RIGHT to LiveTvAction.Dpad(LiveTvDirection.RIGHT),
        RemoteCommand.UP to LiveTvAction.Dpad(LiveTvDirection.UP),
        RemoteCommand.DOWN to LiveTvAction.Dpad(LiveTvDirection.DOWN),
        RemoteCommand.BACK to LiveTvAction.Back,
        RemoteCommand.LONG_BACK to LiveTvAction.LongBack,
        RemoteCommand.MENU to LiveTvAction.LongOk,
        RemoteCommand.CHANNEL_UP to LiveTvAction.ZapUp,
        RemoteCommand.CHANNEL_DOWN to LiveTvAction.ZapDown,
    )

    fun surfaceFor(mode: LiveTvMode): RemoteSurface = when (mode) {
        LiveTvMode.PLAYER_FULLSCREEN, LiveTvMode.PLAYER_CONTROLS_VISIBLE, LiveTvMode.LOADING_CHANNEL, LiveTvMode.PLAYBACK_ERROR -> RemoteSurface.PLAYER
        LiveTvMode.TV_GUIDE, LiveTvMode.PROGRAM_CONTEXT_MENU -> RemoteSurface.TV_GUIDE
        LiveTvMode.CHANNEL_LIST_OVERLAY,
        LiveTvMode.CHANNEL_LIST_PREVIEW,
        LiveTvMode.GROUP_LIST,
        LiveTvMode.PLAYLIST_LIST,
        LiveTvMode.ROOT_NAVIGATION,
        LiveTvMode.CHANNEL_CONTEXT_MENU -> RemoteSurface.CHANNEL_LIST
    }

    fun map(event: KeyEvent, surface: RemoteSurface): LiveTvAction? = map(commandFor(event), surface)

    fun map(commandEvent: RemoteCommandEvent?, surface: RemoteSurface): LiveTvAction? {
        val command = commandEvent ?: return null
        if (command.command == RemoteCommand.NUMBER_INPUT) {
            return command.number?.let(LiveTvAction::NumberInput)
        }
        return defaultsFor(surface)[command.command]
    }

    fun commandFor(event: KeyEvent): RemoteCommandEvent? {
        val nativeEvent = event.nativeKeyEvent
        val longPress = nativeEvent.isLongPress
        return when (nativeEvent.keyCode) {
            AndroidKeyEvent.KEYCODE_ENTER,
            AndroidKeyEvent.KEYCODE_DPAD_CENTER,
            AndroidKeyEvent.KEYCODE_NUMPAD_ENTER -> RemoteCommandEvent(if (longPress) RemoteCommand.LONG_OK else RemoteCommand.OK)
            AndroidKeyEvent.KEYCODE_DPAD_LEFT -> RemoteCommandEvent(if (longPress) RemoteCommand.LONG_LEFT else RemoteCommand.LEFT)
            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> RemoteCommandEvent(RemoteCommand.RIGHT)
            AndroidKeyEvent.KEYCODE_DPAD_UP -> RemoteCommandEvent(RemoteCommand.UP)
            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> RemoteCommandEvent(RemoteCommand.DOWN)
            AndroidKeyEvent.KEYCODE_BACK -> RemoteCommandEvent(if (longPress) RemoteCommand.LONG_BACK else RemoteCommand.BACK)
            AndroidKeyEvent.KEYCODE_MENU -> RemoteCommandEvent(RemoteCommand.MENU)
            AndroidKeyEvent.KEYCODE_CHANNEL_UP,
            AndroidKeyEvent.KEYCODE_PAGE_UP -> RemoteCommandEvent(RemoteCommand.CHANNEL_UP)
            AndroidKeyEvent.KEYCODE_CHANNEL_DOWN,
            AndroidKeyEvent.KEYCODE_PAGE_DOWN -> RemoteCommandEvent(RemoteCommand.CHANNEL_DOWN)
            AndroidKeyEvent.KEYCODE_0,
            AndroidKeyEvent.KEYCODE_NUMPAD_0 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 0)
            AndroidKeyEvent.KEYCODE_1,
            AndroidKeyEvent.KEYCODE_NUMPAD_1 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 1)
            AndroidKeyEvent.KEYCODE_2,
            AndroidKeyEvent.KEYCODE_NUMPAD_2 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 2)
            AndroidKeyEvent.KEYCODE_3,
            AndroidKeyEvent.KEYCODE_NUMPAD_3 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 3)
            AndroidKeyEvent.KEYCODE_4,
            AndroidKeyEvent.KEYCODE_NUMPAD_4 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 4)
            AndroidKeyEvent.KEYCODE_5,
            AndroidKeyEvent.KEYCODE_NUMPAD_5 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 5)
            AndroidKeyEvent.KEYCODE_6,
            AndroidKeyEvent.KEYCODE_NUMPAD_6 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 6)
            AndroidKeyEvent.KEYCODE_7,
            AndroidKeyEvent.KEYCODE_NUMPAD_7 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 7)
            AndroidKeyEvent.KEYCODE_8,
            AndroidKeyEvent.KEYCODE_NUMPAD_8 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 8)
            AndroidKeyEvent.KEYCODE_9,
            AndroidKeyEvent.KEYCODE_NUMPAD_9 -> RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 9)
            else -> null
        }
    }

    private fun defaultsFor(surface: RemoteSurface): Map<RemoteCommand, LiveTvAction> = when (surface) {
        RemoteSurface.PLAYER -> playerDefaults
        RemoteSurface.TV_GUIDE -> guideDefaults
        RemoteSurface.CHANNEL_LIST -> guideDefaults
        RemoteSurface.MOVIES -> emptyMap()
        RemoteSurface.TV_SHOWS -> emptyMap()
    }
}
