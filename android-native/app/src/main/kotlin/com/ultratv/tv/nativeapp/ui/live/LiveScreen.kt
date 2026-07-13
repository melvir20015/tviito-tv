package com.ultratv.tv.nativeapp.ui.live

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
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
    val nav = remember { LiveNavigationCoordinator() }
    var level by remember { mutableStateOf(nav.level) }
    var focusedChannelId by remember { mutableStateOf<Long?>(null) }
    var previewChannelId by remember { mutableStateOf<Long?>(null) }
    var contextChannel by remember { mutableStateOf<ChannelEntity?>(null) }
    val catRequester = remember { FocusRequester() }
    val chanRequester = remember { FocusRequester() }
    val previewRequester = remember { FocusRequester() }
    val categoryListState = rememberLazyListState()
    val channelListState = rememberLazyListState()
    val rememberedScroll = remember { mutableStateMapOf<String, Int>() }
    val categories = remember(realCats, selected, channels.size) {
        buildList {
            add(LiveCategoryUi(CATEGORY_ALL, s.liveAllChannels, if (selected == CATEGORY_ALL) channels.size else null))
            add(LiveCategoryUi(CATEGORY_FAVORITES, "Favoritos", if (selected == CATEGORY_FAVORITES) channels.size else null))
            realCats.forEach { add(LiveCategoryUi(it.remoteId, prettyCategoryName(it.name))) }
        }
    }
    val categoryTitle = categories.firstOrNull { it.id == selected }?.title ?: "Canales"
    val previewChannel = channels.firstOrNull { it.id == previewChannelId }
    fun requestFocusFor(newLevel: LiveLevel) {
        runCatching {
            when (newLevel) {
                LiveLevel.Categories -> catRequester
                LiveLevel.Channels -> chanRequester
                LiveLevel.Preview, LiveLevel.Fullscreen -> previewRequester
            }.requestFocus()
        }
    }

    fun enterChannels(categoryId: String) {
        rememberedScroll[selected] = channelListState.firstVisibleItemIndex
        focusedChannelId = nav.selectCategory(categoryId, channels.firstOrNull()?.id)
        vm.selectCategory(categoryId)
        level = nav.level
    }

    fun openFullscreen(channel: ChannelEntity) {
        nav.restoreFromFullscreen()
        level = nav.level
        vm.resolveAndPlay(channel, onPlay)
    }

    BackHandler(enabled = contextChannel != null || level != LiveLevel.Categories) {
        if (contextChannel != null) {
            contextChannel = null
        } else {
            rememberedScroll[selected] = channelListState.firstVisibleItemIndex
            nav.back()
            level = nav.level
        }
    }

    LaunchedEffect(selected, channels) {
        val restored = nav.restoreChannel(selected, channels.firstOrNull()?.id)
        focusedChannelId = restored?.takeIf { id -> channels.any { it.id == id } } ?: channels.firstOrNull()?.id
        val scrollTarget = rememberedScroll[selected]
            ?: focusedChannelId?.let { id -> channels.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
            ?: 0
        if (channels.isNotEmpty()) channelListState.scrollToItem(scrollTarget.coerceIn(0, channels.lastIndex))
    }

    LaunchedEffect(level) {
        delay(90)
        requestFocusFor(level)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = UltraTokens.EdgeGutter, vertical = 28.dp)
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionLeft, Key.Back -> {
                        if (level != LiveLevel.Categories) {
                            rememberedScroll[selected] = channelListState.firstVisibleItemIndex
                            nav.back()
                            level = nav.level
                            true
                        } else {
                            false
                        }
                    }
                    Key.DirectionRight -> {
                        if (level == LiveLevel.Channels && focusedChannelId != null) {
                            previewChannelId = focusedChannelId
                            nav.clickChannel(focusedChannelId!!)
                            level = nav.level
                            true
                        } else {
                            false
                        }
                    }
                    Key.DirectionUp, Key.DirectionDown -> false
                    Key.Enter, Key.DirectionCenter -> false
                    else -> false
                }
            },
    ) {
        Crossfade(targetState = level, animationSpec = tween(durationMillis = 160), label = "live-level") { currentLevel ->
            when (currentLevel) {
                LiveLevel.Categories -> CategoryPanel(categories, selected, catRequester, categoryListState, Modifier.fillMaxSize(), ::enterChannels)
                LiveLevel.Channels -> ChannelListPanel(
                    channels = channels,
                    focusedId = focusedChannelId,
                    categoryTitle = categoryTitle,
                    requester = chanRequester,
                    state = channelListState,
                    nowNext = nowNext,
                    locked = locked,
                    showNumbers = showNumbers,
                    modifier = Modifier.fillMaxSize(),
                    onFocus = { ch -> focusedChannelId = ch.id; nav.rememberChannel(selected, ch.id) },
                    onBackToCategories = { nav.back(); level = nav.level },
                    onChannelClick = { ch ->
                        if (lockedKey(ch) in locked) contextChannel = ch else {
                            when (nav.clickChannel(ch.id)) {
                                LiveNavigationAction.OpenFullscreen -> openFullscreen(ch)
                                else -> { previewChannelId = ch.id; level = nav.level }
                            }
                        }
                    },
                )
                LiveLevel.Preview, LiveLevel.Fullscreen -> LivePreviewPanel(
                    channel = previewChannel,
                    epg = nowNext[previewChannel?.id],
                    locked = lockedKey(previewChannel) in locked,
                    focused = true,
                    requester = previewRequester,
                    vm = vm,
                    modifier = Modifier.fillMaxSize(),
                    onBack = { nav.back(); level = nav.level },
                    onPlay = { previewChannel?.let(::openFullscreen) },
                )
            }
        }
    }
    contextChannel?.let { ch -> ChannelContextMenu(ch, lockedKey(ch) in locked, onToggleLock = { vm.toggleLock(ch); contextChannel = null }, onDismiss = { contextChannel = null }) }
}

private fun lockedKey(ch: ChannelEntity?) = ch?.let { "${it.providerId}:${it.remoteId}" } ?: ""

@Composable
private fun CategoryPanel(items: List<LiveCategoryUi>, selected: String, requester: FocusRequester, state: LazyListState, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    Column(modifier.focusRequester(requester), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("TV EN VIVO", color = UltraTokens.Fg, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text("Elige una categoría", color = UltraTokens.Fg3, fontSize = 13.sp)
        LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 28.dp), modifier = Modifier.fillMaxSize()) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.id == selected
                Card(onClick = { onSelect(item.id) }, shape = CardDefaults.shape(RoundedCornerShape(16.dp)), colors = CardDefaults.colors(containerColor = if (isSelected) UltraTokens.AccentSoft else UltraTokens.Surface1), modifier = Modifier.fillMaxWidth().border(if (isSelected) 2.dp else 1.dp, if (isSelected) UltraTokens.Accent else UltraTokens.Line, RoundedCornerShape(16.dp))) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(if (isSelected) UltraTokens.Accent else UltraTokens.Fg4, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(item.title, color = UltraTokens.Fg, fontSize = 18.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        item.count?.let { Text(it.toString(), color = UltraTokens.Fg3, fontFamily = UltraFonts.Mono, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelListPanel(channels: List<ChannelEntity>, focusedId: Long?, categoryTitle: String, requester: FocusRequester, state: LazyListState, nowNext: Map<Long, Pair<EpgEntity?, EpgEntity?>>, locked: Set<String>, showNumbers: Boolean, modifier: Modifier = Modifier, onFocus: (ChannelEntity) -> Unit, onBackToCategories: () -> Unit, onChannelClick: (ChannelEntity) -> Unit) {
    Column(modifier.focusRequester(requester)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(categoryTitle, color = UltraTokens.Fg, fontFamily = UltraFonts.Serif, fontSize = 32.sp, maxLines = 1)
                Text("${channels.size} canales · Back para volver a categorías", color = UltraTokens.Fg3, fontSize = 12.sp)
            }
            Card(onClick = onBackToCategories, colors = CardDefaults.colors(containerColor = UltraTokens.Surface2)) { Text("Categorías", color = UltraTokens.Fg, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) }
        }
        Spacer(Modifier.height(16.dp))
        if (channels.isEmpty()) Box(Modifier.fillMaxSize().background(UltraTokens.Surface1, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("No hay canales disponibles.", color = UltraTokens.Fg3) }
        else LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(channels, key = { it.id }) { ch ->
                ChannelRow(ch, channels.indexOf(ch) + 1, focusedId == ch.id, lockedKey(ch) in locked, showNumbers, nowNext[ch.id]?.first, nowNext[ch.id]?.second, onFocus, onChannelClick)
            }
        }
    }
}

@Composable
private fun ChannelRow(ch: ChannelEntity, number: Int, focused: Boolean, locked: Boolean, showNumber: Boolean, now: EpgEntity?, next: EpgEntity?, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit) {
    val scale = if (focused) 1.015f else 1f
    Card(onClick = { onPlay(ch) }, shape = CardDefaults.shape(RoundedCornerShape(16.dp)), colors = CardDefaults.colors(containerColor = if (focused) UltraTokens.Surface2 else UltraTokens.Surface1), modifier = Modifier.fillMaxWidth().scale(scale).border(if (focused) 2.dp else 1.dp, if (focused) UltraTokens.Accent else UltraTokens.Line, RoundedCornerShape(16.dp)).onFocusEventCompat { onFocus(ch) }) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showNumber) Text("%03d".format(number), color = UltraTokens.Fg4, fontFamily = UltraFonts.Mono, fontSize = 12.sp, modifier = Modifier.width(48.dp))
            ChannelLogo(ch.name, ch.logo, null, ch.name.hashCode(), null, 46.dp, false)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(ch.name, color = UltraTokens.Fg, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1); if (locked) Text("  Bloqueado", color = UltraTokens.Accent, fontSize = 10.sp) }
                ProgramInformation(now, next, compact = true)
            }
            StreamStatusIndicator(locked = locked, focused = focused)
        }
    }
}

private fun Modifier.onFocusEventCompat(block: () -> Unit) = this.then(Modifier.onFocusChanged { if (it.isFocused) block() })

@Composable
private fun LivePreviewPanel(channel: ChannelEntity?, epg: Pair<EpgEntity?, EpgEntity?>?, locked: Boolean, focused: Boolean, requester: FocusRequester, vm: LiveViewModel, modifier: Modifier = Modifier, onBack: () -> Unit, onPlay: () -> Unit) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build().apply { volume = 0f; playWhenReady = true } }
    val coordinator = vm.previewCoordinator
    val state by coordinator.state.collectAsState()
    val controller = remember(player) {
        object : PreviewPlayerController {
            override fun stop() { player.stop(); player.clearMediaItems() }
            override fun play(url: String) { player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url)); player.prepare() }
        }
    }
    DisposableEffect(player) { onDispose { coordinator.clear(controller); player.release() } }
    DisposableEffect(player) { val l = object : Player.Listener { override fun onPlaybackStateChanged(playbackState: Int) { when (playbackState) { Player.STATE_BUFFERING -> coordinator.onBuffering(); Player.STATE_READY -> coordinator.onPlaying() } }; override fun onPlayerError(error: androidx.media3.common.PlaybackException) { coordinator.onError(error.message) } }; player.addListener(l); onDispose { player.removeListener(l) } }
    val scope = rememberCoroutineScope()
    LaunchedEffect(channel?.id, locked) {
        coordinator.request(scope, channel, locked, controller)
    }
    Row(modifier.focusRequester(requester), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(0.66f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            LivePreviewSurface(
                player = player,
                state = state,
                focused = focused,
                modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
            )
        }
        Column(Modifier.weight(0.34f).fillMaxHeight()) {
            Text("Vista previa", color = UltraTokens.Fg3, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (channel != null) ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 54.dp, false)
                Spacer(Modifier.width(14.dp))
                Text(channel?.name ?: "Selecciona un canal", color = UltraTokens.Fg, fontFamily = UltraFonts.Serif, fontSize = 30.sp, maxLines = 3)
            }
            Spacer(Modifier.height(18.dp)); ProgramInformation(epg?.first, epg?.second, compact = false)
            Spacer(Modifier.height(14.dp)); EpgProgressBar(epg?.first)
            Spacer(Modifier.weight(1f))
            Text("Back vuelve a canales · OK abre pantalla completa", color = UltraTokens.Fg3, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(onClick = onBack, colors = CardDefaults.colors(containerColor = UltraTokens.Surface2)) { Text("Canales", color = UltraTokens.Fg, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
                Card(onClick = { if (channel != null && !locked) onPlay() }, colors = CardDefaults.colors(containerColor = if (locked) UltraTokens.Surface2 else UltraTokens.Accent)) { Text(if (locked) "Canal bloqueado" else "Reproducir", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
            }
        }
    }
}
@Composable
private fun LivePreviewSurface(
    player: ExoPlayer,
    state: PreviewPlaybackState,
    focused: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
            .border(
                if (focused) 2.dp else 1.dp,
                if (focused) UltraTokens.Accent else UltraTokens.Line2,
                RoundedCornerShape(24.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { PlayerView(it).apply { useController = false; this.player = player } },
            modifier = Modifier.fillMaxSize(),
        )
        if (state != PreviewPlaybackState.Playing) {
            Text(previewText(state), color = UltraTokens.Fg, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun previewText(s: PreviewPlaybackState) = when (s) {
    PreviewPlaybackState.Idle -> "Vista previa"
    PreviewPlaybackState.Connecting -> "Conectando…"
    PreviewPlaybackState.Buffering -> "Buffering…"
    PreviewPlaybackState.Playing -> ""
    PreviewPlaybackState.Reconnecting -> "Reconectando…"
    is PreviewPlaybackState.Failed -> "No se pudo cargar la vista previa"
    is PreviewPlaybackState.Unsupported -> "Stream no soportado"
    PreviewPlaybackState.Locked -> "Canal bloqueado"
}

@Composable
private fun ProgramInformation(now: EpgEntity?, next: EpgEntity?, compact: Boolean) { Column { Text(now?.title ?: "Sin EPG ahora", color = if (now == null) UltraTokens.Fg4 else UltraTokens.Fg2, fontSize = if (compact) 12.sp else 16.sp, maxLines = if (compact) 1 else 2); next?.let { Text("Luego: ${it.title}", color = UltraTokens.Fg4, fontSize = if (compact) 11.sp else 13.sp, maxLines = 1) }; if (!compact && now != null) Text("${fmt(now.startMs)} - ${fmt(now.endMs)}", color = UltraTokens.Fg3, fontFamily = UltraFonts.Mono, fontSize = 12.sp) } }
@Composable
private fun EpgProgressBar(now: EpgEntity?) { val progress = now?.let { ((System.currentTimeMillis() - it.startMs).toFloat() / (it.endMs - it.startMs).coerceAtLeast(1)).coerceIn(0f, 1f) } ?: 0f; Box(Modifier.fillMaxWidth().height(6.dp).background(UltraTokens.Line, RoundedCornerShape(99.dp))) { Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(UltraTokens.Accent, RoundedCornerShape(99.dp))) } }
@Composable
private fun StreamStatusIndicator(locked: Boolean, focused: Boolean) { Box(Modifier.size(10.dp).background(if (locked) UltraTokens.Accent else if (focused) UltraTokens.Live else UltraTokens.Fg4, CircleShape)) }
@Composable
private fun ChannelContextMenu(channel: ChannelEntity, locked: Boolean, onToggleLock: () -> Unit, onDismiss: () -> Unit) { BackHandler { onDismiss() }; Box(Modifier.fillMaxSize().background(UltraTokens.ScrimStrong), contentAlignment = Alignment.Center) { Column(Modifier.width(360.dp).background(UltraTokens.Surface1, RoundedCornerShape(18.dp)).border(1.dp, UltraTokens.Line2, RoundedCornerShape(18.dp)).padding(18.dp)) { Text(channel.name, color = UltraTokens.Fg, fontSize = 20.sp, fontFamily = UltraFonts.Serif); Spacer(Modifier.height(14.dp)); Card(onClick = onToggleLock, colors = CardDefaults.colors(containerColor = UltraTokens.Surface2)) { Text(if (locked) "Desbloquear canal" else "Bloquear canal", color = UltraTokens.Fg, modifier = Modifier.fillMaxWidth().padding(12.dp)) } } } }
private fun fmt(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
