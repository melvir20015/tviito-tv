package com.ultratv.tv.nativeapp.ui.live

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.db.EpgEntity
import com.ultratv.tv.nativeapp.ui.common.ChannelLogo
import com.ultratv.tv.nativeapp.ui.common.prettyCategoryName
import com.ultratv.tv.nativeapp.ui.theme.UltraFonts
import com.ultratv.tv.nativeapp.ui.theme.UltraTokens
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class LiveCategoryUi(val id: String, val title: String, val count: Int? = null)
private enum class PreviewState { Idle, Loading, Ready, Buffering, Error, Locked }

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun LiveScreen(onPlay: (url: String, title: String) -> Unit, vm: LiveViewModel = hiltViewModel()) {
    val realCats by vm.categories.collectAsState()
    val channels by vm.channels.collectAsState()
    val selected by vm.selectedCategory.collectAsState()
    val locked by vm.lockedChannels.collectAsState()
    val nowNext by vm.nowNext.collectAsState()
    val showNumbers by vm.showChannelNumbers.collectAsState()
    val s = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    val focus = remember { FocusCoordinator(initialPanel = LivePanel.CHANNELS) }
    var activePanel by remember { mutableStateOf(focus.activePanel) }
    var focusedChannelId by remember { mutableStateOf<Long?>(null) }
    var contextChannel by remember { mutableStateOf<ChannelEntity?>(null) }
    val catRequester = remember { FocusRequester() }
    val chanRequester = remember { FocusRequester() }
    val previewRequester = remember { FocusRequester() }
    val channelListState = rememberLazyListState()
    val categories = remember(realCats, selected, channels.size) {
        buildList {
            add(LiveCategoryUi(CATEGORY_ALL, s.liveAllChannels, if (selected == CATEGORY_ALL) channels.size else null))
            add(LiveCategoryUi(CATEGORY_FAVORITES, "Favoritos", if (selected == CATEGORY_FAVORITES) channels.size else null))
            add(LiveCategoryUi(CATEGORY_RECENTS, "Recientes", if (selected == CATEGORY_RECENTS) channels.size else null))
            add(LiveCategoryUi(CATEGORY_HISTORY, "Historial", if (selected == CATEGORY_HISTORY) channels.size else null))
            realCats.forEach { add(LiveCategoryUi(it.remoteId, prettyCategoryName(it.name))) }
        }
    }
    val activeChannel = channels.firstOrNull { it.id == focusedChannelId } ?: channels.firstOrNull()

    BackHandler(enabled = contextChannel != null || activePanel != LivePanel.CATEGORY) {
        if (contextChannel != null) contextChannel = null else {
            activePanel = focus.moveLeft()
            requestPanelFocus(activePanel, catRequester, chanRequester, previewRequester)
        }
    }

    LaunchedEffect(selected, channels) {
        val restored = focus.restoreChannel(selected, channels.firstOrNull()?.id)
        focusedChannelId = restored?.takeIf { id -> channels.any { it.id == id } } ?: channels.firstOrNull()?.id
        focusedChannelId?.let { id -> channels.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { channelListState.scrollToItem(it) } }
    }

    Row(
        Modifier.fillMaxSize().background(UltraTokens.Bg).padding(horizontal = UltraTokens.EdgeGutter, vertical = 28.dp)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> { activePanel = focus.moveLeft(); requestPanelFocus(activePanel, catRequester, chanRequester, previewRequester); true }
                    Key.DirectionRight -> { activePanel = focus.moveRight(channels.isNotEmpty()); requestPanelFocus(activePanel, catRequester, chanRequester, previewRequester); true }
                    Key.ChannelUp -> { focusedChannelId = stepChannel(channels, focusedChannelId, -1); true }
                    Key.ChannelDown -> { focusedChannelId = stepChannel(channels, focusedChannelId, 1); true }
                    else -> false
                }
            },
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CategoryPanel(categories, selected, activePanel == LivePanel.CATEGORY, catRequester) { id ->
            vm.selectCategory(id); focusedChannelId = focus.selectCategory(id, channels.firstOrNull()?.id); activePanel = LivePanel.CHANNELS
        }
        ChannelListPanel(
            channels, focusedChannelId, selected, activePanel == LivePanel.CHANNELS, chanRequester, channelListState, nowNext, locked, showNumbers,
            onFocus = { ch -> focusedChannelId = ch.id; focus.rememberChannel(selected, ch.id) },
            onPlay = { ch -> if (lockedKey(ch) in locked) contextChannel = ch else vm.resolveAndPlay(ch, onPlay) },
            onLongPress = { ch -> contextChannel = ch },
        )
        LivePreviewPanel(activeChannel, nowNext[activeChannel?.id], lockedKey(activeChannel) in locked, activePanel == LivePanel.PREVIEW, previewRequester, vm) {
            activeChannel?.let { if (lockedKey(it) !in locked) vm.resolveAndPlay(it, onPlay) }
        }
    }
    contextChannel?.let { ch -> ChannelContextMenu(ch, lockedKey(ch) in locked, onToggleLock = { vm.toggleLock(ch); contextChannel = null }, onDismiss = { contextChannel = null }) }
}

private fun requestPanelFocus(panel: LivePanel, cat: FocusRequester, chan: FocusRequester, preview: FocusRequester) {
    runCatching { when (panel) { LivePanel.CATEGORY -> cat; LivePanel.CHANNELS -> chan; LivePanel.PREVIEW -> preview }.requestFocus() }
}
private fun stepChannel(list: List<ChannelEntity>, current: Long?, delta: Int): Long? {
    if (list.isEmpty()) return null
    val idx = list.indexOfFirst { it.id == current }.let { if (it < 0) 0 else it }
    return list[(idx + delta).coerceIn(0, list.lastIndex)].id
}
private fun lockedKey(ch: ChannelEntity?) = ch?.let { "${it.providerId}:${it.remoteId}" } ?: ""

@Composable
private fun CategoryPanel(items: List<LiveCategoryUi>, selected: String, focused: Boolean, requester: FocusRequester, onSelect: (String) -> Unit) {
    Column(Modifier.width(220.dp).fillMaxHeight().focusRequester(requester), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("TV EN VIVO", color = UltraTokens.Fg, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text("CATEGORÍAS", color = UltraTokens.Fg3, fontSize = 11.sp, letterSpacing = 2.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.id == selected
                Card(onClick = { onSelect(item.id) }, shape = CardDefaults.shape(RoundedCornerShape(12.dp)), colors = CardDefaults.colors(containerColor = if (isSelected) UltraTokens.AccentSoft else UltraTokens.Surface1), modifier = Modifier.border(if (focused && isSelected) 2.dp else 1.dp, if (focused && isSelected) UltraTokens.Accent else UltraTokens.Line, RoundedCornerShape(12.dp))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).background(if (isSelected) UltraTokens.Accent else UltraTokens.Fg4, CircleShape))
                        Spacer(Modifier.width(9.dp)); Text(item.title, color = if (isSelected) UltraTokens.Fg else UltraTokens.Fg2, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        item.count?.let { Text(it.toString(), color = UltraTokens.Fg4, fontFamily = UltraFonts.Mono, fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelListPanel(channels: List<ChannelEntity>, focusedId: Long?, categoryId: String, panelFocused: Boolean, requester: FocusRequester, state: androidx.compose.foundation.lazy.LazyListState, nowNext: Map<Long, Pair<EpgEntity?, EpgEntity?>>, locked: Set<String>, showNumbers: Boolean, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit, onLongPress: (ChannelEntity) -> Unit) {
    Column(Modifier.width(500.dp).fillMaxHeight().focusRequester(requester)) {
        Text("Canales", color = UltraTokens.Fg, fontFamily = UltraFonts.Serif, fontSize = 28.sp)
        Spacer(Modifier.height(10.dp))
        if (channels.isEmpty()) Box(Modifier.fillMaxSize().background(UltraTokens.Surface1, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text(if (categoryId == CATEGORY_RECENTS || categoryId == CATEGORY_HISTORY) "Aún no hay canales vistos en esta categoría." else "No hay canales disponibles.", color = UltraTokens.Fg3) }
        else LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(channels, key = { it.id }) { ch ->
                ChannelRow(ch, channels.indexOf(ch) + 1, focusedId == ch.id && panelFocused, lockedKey(ch) in locked, showNumbers, nowNext[ch.id]?.first, nowNext[ch.id]?.second, onFocus, onPlay, onLongPress)
            }
        }
    }
}

@Composable
private fun ChannelRow(ch: ChannelEntity, number: Int, focused: Boolean, locked: Boolean, showNumber: Boolean, now: EpgEntity?, next: EpgEntity?, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit, onLongPress: (ChannelEntity) -> Unit) {
    val scale = if (focused) 1.025f else 1f
    Card(onClick = { onPlay(ch) }, shape = CardDefaults.shape(RoundedCornerShape(16.dp)), colors = CardDefaults.colors(containerColor = if (focused) UltraTokens.Surface2 else UltraTokens.Surface1), modifier = Modifier.fillMaxWidth().scale(scale).border(if (focused) 2.dp else 1.dp, if (focused) UltraTokens.Accent else UltraTokens.Line, RoundedCornerShape(16.dp)).onFocusEventCompat { onFocus(ch) }.onPreviewKeyEvent { e -> if (e.nativeKeyEvent.keyCode == AndroidKeyEvent.KEYCODE_DPAD_CENTER && e.nativeKeyEvent.isLongPress) { onLongPress(ch); true } else false }) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showNumber) Text("%03d".format(number), color = UltraTokens.Fg4, fontFamily = UltraFonts.Mono, fontSize = 12.sp, modifier = Modifier.width(42.dp))
            ChannelLogo(ch.name, ch.logo, null, ch.name.hashCode(), null, 42.dp, false)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(ch.name, color = UltraTokens.Fg, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1); if (locked) Text("  Bloqueado", color = UltraTokens.Accent, fontSize = 10.sp) }
                ProgramInformation(now, next, compact = true)
            }
            StreamStatusIndicator(locked = locked, focused = focused)
        }
    }
}

private fun Modifier.onFocusEventCompat(block: () -> Unit) = this.then(Modifier.onFocusChanged { if (it.isFocused) block() })

@Composable
private fun LivePreviewPanel(channel: ChannelEntity?, epg: Pair<EpgEntity?, EpgEntity?>?, locked: Boolean, focused: Boolean, requester: FocusRequester, vm: LiveViewModel, onPlay: () -> Unit) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(if (locked) PreviewState.Locked else PreviewState.Idle) }
    val debouncer = remember { PreviewDebouncer() }
    val player = remember { ExoPlayer.Builder(context).build().apply { volume = 0f; playWhenReady = true } }
    DisposableEffect(player) { onDispose { player.release() } }
    DisposableEffect(player) { val l = object : Player.Listener { override fun onPlaybackStateChanged(playbackState: Int) { state = when (playbackState) { Player.STATE_BUFFERING -> PreviewState.Buffering; Player.STATE_READY -> PreviewState.Ready; else -> state } }; override fun onPlayerError(error: androidx.media3.common.PlaybackException) { state = PreviewState.Error } }; player.addListener(l); onDispose { player.removeListener(l) } }
    LaunchedEffect(channel?.id, locked) {
        if (channel == null || locked) { player.stop(); state = if (locked) PreviewState.Locked else PreviewState.Idle; return@LaunchedEffect }
        if (debouncer.activeChannelId() == channel.id) return@LaunchedEffect
        while (!debouncer.request(channel.id, System.currentTimeMillis())) delay(75)
        state = PreviewState.Loading
        runCatching { vm.resolvePreviewUrl(channel) }.onSuccess { url -> player.setMediaItem(MediaItem.fromUri(url)); player.prepare(); debouncer.activate(channel.id) }.onFailure { state = PreviewState.Error }
    }
    Column(Modifier.weight(1f).fillMaxHeight().focusRequester(requester)) {
        Box(Modifier.fillMaxWidth().aspectRatio(16 / 9f).clip(RoundedCornerShape(22.dp)).background(Color.Black).border(if (focused) 2.dp else 1.dp, if (focused) UltraTokens.Accent else UltraTokens.Line2, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
            AndroidView(factory = { PlayerView(it).apply { useController = false; this.player = player } }, modifier = Modifier.fillMaxSize())
            if (state != PreviewState.Ready) Text(previewText(state), color = UltraTokens.Fg, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(18.dp))
        Text(channel?.name ?: "Selecciona un canal", color = UltraTokens.Fg, fontFamily = UltraFonts.Serif, fontSize = 30.sp, maxLines = 2)
        Spacer(Modifier.height(10.dp)); ProgramInformation(epg?.first, epg?.second, compact = false)
        Spacer(Modifier.height(12.dp)); EpgProgressBar(epg?.first)
        Spacer(Modifier.weight(1f)); Card(onClick = { if (channel != null && !locked) onPlay() }, colors = CardDefaults.colors(containerColor = if (locked) UltraTokens.Surface2 else UltraTokens.Accent)) { Text(if (locked) "Canal bloqueado" else "OK para reproducir", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
    }
}
private fun previewText(s: PreviewState) = when (s) { PreviewState.Idle -> "Vista previa"; PreviewState.Loading -> "Conectando…"; PreviewState.Buffering -> "Buffering…"; PreviewState.Error -> "No se pudo cargar la vista previa"; PreviewState.Locked -> "Canal bloqueado"; PreviewState.Ready -> "" }

@Composable
private fun ProgramInformation(now: EpgEntity?, next: EpgEntity?, compact: Boolean) { Column { Text(now?.title ?: "Sin EPG ahora", color = if (now == null) UltraTokens.Fg4 else UltraTokens.Fg2, fontSize = if (compact) 12.sp else 16.sp, maxLines = if (compact) 1 else 2); next?.let { Text("Luego: ${it.title}", color = UltraTokens.Fg4, fontSize = if (compact) 11.sp else 13.sp, maxLines = 1) }; if (!compact && now != null) Text("${fmt(now.startMs)} - ${fmt(now.endMs)}", color = UltraTokens.Fg3, fontFamily = UltraFonts.Mono, fontSize = 12.sp) } }
@Composable
private fun EpgProgressBar(now: EpgEntity?) { val progress = now?.let { ((System.currentTimeMillis() - it.startMs).toFloat() / (it.endMs - it.startMs).coerceAtLeast(1)).coerceIn(0f, 1f) } ?: 0f; Box(Modifier.fillMaxWidth().height(6.dp).background(UltraTokens.Line, RoundedCornerShape(99.dp))) { Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(UltraTokens.Accent, RoundedCornerShape(99.dp))) } }
@Composable
private fun StreamStatusIndicator(locked: Boolean, focused: Boolean) { Box(Modifier.size(10.dp).background(if (locked) UltraTokens.Accent else if (focused) UltraTokens.Live else UltraTokens.Fg4, CircleShape)) }
@Composable
private fun ChannelContextMenu(channel: ChannelEntity, locked: Boolean, onToggleLock: () -> Unit, onDismiss: () -> Unit) { BackHandler { onDismiss() }; Box(Modifier.fillMaxSize().background(Color(0x99000000)), contentAlignment = Alignment.Center) { Column(Modifier.width(360.dp).background(UltraTokens.Surface1, RoundedCornerShape(18.dp)).border(1.dp, UltraTokens.Line2, RoundedCornerShape(18.dp)).padding(18.dp)) { Text(channel.name, color = UltraTokens.Fg, fontSize = 20.sp, fontFamily = UltraFonts.Serif); Spacer(Modifier.height(14.dp)); Card(onClick = onToggleLock, colors = CardDefaults.colors(containerColor = UltraTokens.Surface2)) { Text(if (locked) "Desbloquear canal" else "Bloquear canal", color = UltraTokens.Fg, modifier = Modifier.fillMaxWidth().padding(12.dp)) } } } }
private fun fmt(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
