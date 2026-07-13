package com.ultratv.tv.nativeapp.ui.live.remote

import com.ultratv.tv.nativeapp.ui.live.LiveTvAction
import com.ultratv.tv.nativeapp.ui.live.LiveTvDirection
import com.ultratv.tv.nativeapp.ui.live.LiveTvMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteActionMapperTest {
    @Test
    fun `player fullscreen defaults map principal playback commands`() {
        assertEquals(LiveTvAction.Ok, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.OK), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.LongOk, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LONG_OK), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.OpenPanel(LiveTvMode.CHANNEL_LIST_OVERLAY), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LEFT), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.OpenPanel(LiveTvMode.TV_GUIDE), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LONG_LEFT), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.OpenPanel(LiveTvMode.TV_GUIDE), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.RIGHT), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.ZapUp, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.UP), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.ZapDown, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.DOWN), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.Back, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.BACK), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.LongBack, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LONG_BACK), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.LongOk, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.MENU), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.ZapUp, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.CHANNEL_UP), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.ZapDown, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.CHANNEL_DOWN), RemoteSurface.PLAYER))
        assertEquals(LiveTvAction.NumberInput(7), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 7), RemoteSurface.PLAYER))
    }

    @Test
    fun `tv guide defaults map principal navigation commands`() {
        assertEquals(LiveTvAction.Ok, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.OK), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.LongOk, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LONG_OK), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.Dpad(LiveTvDirection.LEFT), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LEFT), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.Dpad(LiveTvDirection.LEFT), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LONG_LEFT), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.Dpad(LiveTvDirection.RIGHT), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.RIGHT), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.Dpad(LiveTvDirection.UP), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.UP), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.Dpad(LiveTvDirection.DOWN), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.DOWN), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.Back, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.BACK), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.LongBack, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.LONG_BACK), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.LongOk, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.MENU), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.ZapUp, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.CHANNEL_UP), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.ZapDown, RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.CHANNEL_DOWN), RemoteSurface.TV_GUIDE))
        assertEquals(LiveTvAction.NumberInput(3), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 3), RemoteSurface.TV_GUIDE))
    }

    @Test
    fun `android key codes map without compose native key extension`() {
        assertEquals(RemoteCommandEvent(RemoteCommand.OK), RemoteActionMapper.commandFor(android.view.KeyEvent.KEYCODE_DPAD_CENTER))
        assertEquals(RemoteCommandEvent(RemoteCommand.LONG_OK), RemoteActionMapper.commandFor(android.view.KeyEvent.KEYCODE_DPAD_CENTER, isLongPress = true))
        assertEquals(RemoteCommandEvent(RemoteCommand.LONG_BACK), RemoteActionMapper.commandFor(android.view.KeyEvent.KEYCODE_BACK, isLongPress = true))
        assertEquals(RemoteCommandEvent(RemoteCommand.CHANNEL_UP), RemoteActionMapper.commandFor(android.view.KeyEvent.KEYCODE_CHANNEL_UP))
        assertEquals(RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 9), RemoteActionMapper.commandFor(android.view.KeyEvent.KEYCODE_9))
    }

    @Test
    fun `surface is inferred from live tv mode`() {
        assertEquals(RemoteSurface.PLAYER, RemoteActionMapper.surfaceFor(LiveTvMode.PLAYER_FULLSCREEN))
        assertEquals(RemoteSurface.TV_GUIDE, RemoteActionMapper.surfaceFor(LiveTvMode.TV_GUIDE))
        assertEquals(RemoteSurface.CHANNEL_LIST, RemoteActionMapper.surfaceFor(LiveTvMode.CHANNEL_LIST_OVERLAY))
    }

    @Test
    fun `future vod surfaces do not map live actions by default except numeric input`() {
        assertNull(RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.OK), RemoteSurface.MOVIES))
        assertEquals(LiveTvAction.NumberInput(1), RemoteActionMapper.map(RemoteCommandEvent(RemoteCommand.NUMBER_INPUT, 1), RemoteSurface.TV_SHOWS))
    }
}
