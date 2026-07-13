package com.ultratv.tv.nativeapp.ui.live

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

private object LiveTvColors {
    val background: Color @Composable get() = MaterialTheme.colorScheme.background
    val textPrimary = UltraTokens.Fg
    val textSecondary = UltraTokens.Fg2
    val textMuted = UltraTokens.Fg3
    val textSubtle = UltraTokens.Fg4
    val surface = UltraTokens.Surface1
    val surfaceRaised = UltraTokens.Surface2
    val accent = UltraTokens.Accent
    val accentSoft = UltraTokens.AccentSoft
    val outline = UltraTokens.Line
    val outlineStrong = UltraTokens.Line2
    val live = UltraTokens.Live
    val scrim = UltraTokens.ScrimStrong
}

private object LiveTvTypography {
    val title = 32.sp
    val section = 24.sp
    val body = 16.sp
    val metadata = 13.sp
    val caption = 12.sp
    val micro = 11.sp
}

private object LiveTvSpacing {
    val screenHorizontal = UltraTokens.EdgeGutter
    val screenVertical = 28.dp
    val panelGap = 24.dp
    val itemGap = 10.dp
    val itemPadding = 14.dp
    val cardHorizontal = 18.dp
    val cardVertical = 16.dp
}

private object LiveTvShapes {
    val panel = RoundedCornerShape(24.dp)
    val card = RoundedCornerShape(16.dp)
    val menu = RoundedCornerShape(18.dp)
    val pill = RoundedCornerShape(99.dp)
}

private object LiveTvElevation {
    val focusedBorder = 2.dp
    val restingBorder = 1.dp
}

private object LiveTvMotion {
    const val panelCrossfadeMillis = 160
    const val focusRestoreDelayMillis = 90L
}

private object LiveTvDimensions {
    val compactPreviewWidthFraction = 0.60f
    val expandedPreviewWidthFraction = 0.66f
    val numberColumnWidth = 48.dp
    val logo = 46.dp
    val previewLogo = 54.dp
    val contextMenuWidth = 360.dp
}

private enum class LiveTvFormFactor { Hd, FullHd, UltraHd }

private fun liveTvFormFactor(maxWidth: Dp): LiveTvFormFactor = when {
    maxWidth >= 3000.dp -> LiveTvFormFactor.UltraHd
    maxWidth >= 1500.dp -> LiveTvFormFactor.FullHd
    else -> LiveTvFormFactor.Hd
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun LiveScreen(onPlay: (url: String, title: String) -> Unit, vm: LiveViewModel = hiltViewModel()) = LiveTvScreen(onPlay = onPlay, vm = vm)

private enum class LiveOverlay { Channels, Guide }

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvScreen(onPlay: (url: String, title: String) -> Unit, vm: LiveViewModel = hiltViewModel()) {
    val realCats by vm.categories.collectAsState()
    val channels by vm.channels.collectAsState()
    val selected by vm.selectedCategory.collectAsState()
    val locked by vm.lockedChannels.collectAsState()
    val nowNext by vm.nowNext.collectAsState()
    val showNumbers by vm.showChannelNumbers.collectAsState()
    val favorites by vm.favoriteRemoteIds.collectAsState()
    val s = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    var overlay by remember { mutableStateOf<LiveOverlay?>(null) }
    var focusedChannelId by remember { mutableStateOf<Long?>(null) }
    var activeChannel by remember { mutableStateOf<ChannelEntity?>(null) }
    var contextChannel by remember { mutableStateOf<ChannelEntity?>(null) }
    var contextProgram by remember { mutableStateOf<EpgEntity?>(null) }
    var showInfoBar by remember { mutableStateOf(true) }
    val livePrefs by vm.livePreferences.collectAsState()
    val resolving by vm.resolving.collectAsState()
    val categoryRequester = remember { FocusRequester() }
    val channelRequester = remember { FocusRequester() }
    val guideRequester = remember { FocusRequester() }
    val categoryListState = rememberLazyListState()
    val channelListState = rememberLazyListState()
    val guideListState = rememberLazyListState()
    val categories = remember(realCats, selected, channels.size) {
        buildList {
            add(LiveCategoryUi(CATEGORY_ALL, s.liveAllChannels, if (selected == CATEGORY_ALL) channels.size else null))
            add(LiveCategoryUi(CATEGORY_FAVORITES, "Favoritos", if (selected == CATEGORY_FAVORITES) channels.size else null))
            add(LiveCategoryUi(CATEGORY_RECENTS, "Recientes"))
            realCats.forEach { add(LiveCategoryUi(it.remoteId, prettyCategoryName(it.name))) }
        }
    }
    val categoryTitle = categories.firstOrNull { it.id == selected }?.title ?: "Canales"
    val focusedChannel = channels.firstOrNull { it.id == focusedChannelId }
    val videoChannel = activeChannel ?: focusedChannel ?: channels.firstOrNull()

    fun playInPlace(ch: ChannelEntity) {
        if (lockedKey(ch) in locked) { contextChannel = ch; return }
        activeChannel = ch
        focusedChannelId = ch.id
        showInfoBar = true
        vm.resolveAndPlay(ch) { _, _ -> }
    }

    BackHandler(enabled = contextChannel != null || contextProgram != null || overlay != null || showInfoBar) {
        when {
            contextProgram != null -> contextProgram = null
            contextChannel != null -> contextChannel = null
            overlay == LiveOverlay.Guide -> overlay = LiveOverlay.Channels
            overlay == LiveOverlay.Channels -> overlay = null
            showInfoBar -> showInfoBar = false
        }
    }

    LaunchedEffect(selected, channels, livePrefs.liveLastChannelRemoteId) {
        if (channels.isNotEmpty()) {
            val restoredId = livePrefs.liveLastChannelRemoteId
            val restoredChannel = channels.firstOrNull { it.remoteId == restoredId }
            focusedChannelId = focusedChannelId?.takeIf { id -> channels.any { it.id == id } } ?: restoredChannel?.id ?: channels.firstOrNull()?.id
            val index = channels.indexOfFirst { it.id == focusedChannelId }.coerceAtLeast(0)
            channelListState.scrollToItem(index)
            guideListState.scrollToItem(index)
            if (activeChannel == null) activeChannel = restoredChannel ?: channels.getOrNull(index)
        }
    }
    LaunchedEffect(overlay) {
        if (overlay != null) {
            delay(LiveTvMotion.focusRestoreDelayMillis)
            runCatching { if (overlay == LiveOverlay.Guide) guideRequester.requestFocus() else channelRequester.requestFocus() }
        }
    }
    LaunchedEffect(showInfoBar, activeChannel?.id) {
        if (showInfoBar && overlay == null && contextChannel == null && contextProgram == null) {
            delay(livePrefs.liveControlsTimeoutMs.coerceIn(4_000L, 6_000L))
            showInfoBar = false
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> { overlay = LiveOverlay.Channels; showInfoBar = false; true }
                    Key.DirectionRight -> { overlay = LiveOverlay.Guide; showInfoBar = false; true }
                    Key.DirectionUp -> { channels.previousFrom(activeChannel)?.let(::playInPlace); true }
                    Key.DirectionDown -> { channels.nextFrom(activeChannel)?.let(::playInPlace); true }
                    Key.DirectionCenter, Key.Enter -> { if (overlay == null) { showInfoBar = !showInfoBar; true } else { focusedChannel?.let(::playInPlace); true } }
                    Key.Menu -> { focusedChannel?.let { contextChannel = it }; true }
                    else -> false
                }
            },
    ) {
        val formFactor = liveTvFormFactor(maxWidth)
        LiveBackgroundPlayer(channel = videoChannel, locked = lockedKey(videoChannel) in locked, vm = vm)
        if (overlay != null) Box(Modifier.fillMaxSize().background(LiveTvColors.scrim.copy(alpha = 0.44f)))
        Crossfade(targetState = overlay, animationSpec = tween(LiveTvMotion.panelCrossfadeMillis), label = "live-overlay") { current ->
            if (current == LiveOverlay.Channels) {
                Row(Modifier.fillMaxSize().padding(horizontal = LiveTvSpacing.screenHorizontal, vertical = LiveTvSpacing.screenVertical), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.width(330.dp).fillMaxHeight().background(LiveTvColors.surface.copy(alpha = 0.94f), LiveTvShapes.panel).padding(18.dp)) {
                        RootNavigationRail(formFactor, Modifier.align(Alignment.CenterStart))
                        PlaylistPanel(categories, selected, categoryRequester, categoryListState, Modifier.padding(start = 16.dp).fillMaxSize()) { id -> vm.selectCategory(id) }
                    }
                    Box(Modifier.width(520.dp).fillMaxHeight().background(LiveTvColors.surface.copy(alpha = 0.92f), LiveTvShapes.panel).padding(18.dp)) {
                        ChannelListOverlay(
                            channels = channels,
                            focusedId = focusedChannelId,
                            categoryTitle = categoryTitle,
                            requester = channelRequester,
                            state = channelListState,
                            nowNext = nowNext,
                            locked = locked,
                            showNumbers = showNumbers,
                            favorites = favorites,
                            activeId = activeChannel?.id,
                            modifier = Modifier.fillMaxSize(),
                            onFocus = { ch -> focusedChannelId = ch.id },
                            onBackToCategories = { categoryRequester.requestFocus() },
                            onChannelClick = ::playInPlace,
                            onOpenMenu = { contextChannel = it },
                        )
                    }
                    LiveNowPanel(videoChannel, nowNext[videoChannel?.id], lockedKey(videoChannel) in locked, Modifier.weight(1f).fillMaxHeight(), onGuide = { overlay = LiveOverlay.Guide })
                }
            } else if (current == LiveOverlay.Guide) {
                EpgOverlayGuide(
                    channels = channels,
                    activeId = activeChannel?.id,
                    focusedId = focusedChannelId,
                    nowNext = nowNext,
                    favorites = favorites,
                    locked = locked,
                    requester = guideRequester,
                    state = guideListState,
                    onFocus = { focusedChannelId = it.id },
                    onPlay = ::playInPlace,
                    onProgramMenu = { contextProgram = it },
                    onChannelMenu = { contextChannel = it },
                    modifier = Modifier.fillMaxSize().padding(horizontal = LiveTvSpacing.screenHorizontal, vertical = LiveTvSpacing.screenVertical),
                )
            }
        }
        AnimatedVisibility(
            visible = showInfoBar && overlay == null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            LiveChannelInfoBar(
                channel = videoChannel,
                epg = nowNext[videoChannel?.id],
                locked = lockedKey(videoChannel) in locked,
                favorite = videoChannel?.remoteId in favorites,
                resolving = resolving,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.32f),
            )
        }
    }
    contextChannel?.let { ch ->
        ChannelContextMenu(
            channel = ch,
            locked = lockedKey(ch) in locked,
            favorite = ch.remoteId in favorites,
            now = nowNext[ch.id]?.first,
            onToggleFavorite = { vm.toggleFavorite(ch) },
            onToggleLock = { vm.toggleLock(ch) },
            onPlay = { playInPlace(ch); contextChannel = null },
            onDismiss = { contextChannel = null },
        )
    }
    contextProgram?.let { program -> ProgramContextMenu(program = program, onReminder = { videoChannel?.let { vm.addReminder(it, program) } }, onDismiss = { contextProgram = null }) }
}

private fun lockedKey(ch: ChannelEntity?) = ch?.let { "${it.providerId}:${it.remoteId}" } ?: ""

@Composable
private fun PlaylistPanel(items: List<LiveCategoryUi>, selected: String, requester: FocusRequester, state: LazyListState, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    Column(modifier.focusRequester(requester), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("TV EN VIVO", color = LiveTvColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text("Elige una categoría", color = LiveTvColors.textMuted, fontSize = 13.sp)
        LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 28.dp), modifier = Modifier.fillMaxSize()) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.id == selected
                Card(onClick = { onSelect(item.id) }, shape = CardDefaults.shape(RoundedCornerShape(16.dp)), colors = CardDefaults.colors(containerColor = if (isSelected) LiveTvColors.accentSoft else LiveTvColors.surface), modifier = Modifier.fillMaxWidth().border(if (isSelected) 2.dp else 1.dp, if (isSelected) LiveTvColors.accent else LiveTvColors.outline, RoundedCornerShape(16.dp))) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(if (isSelected) LiveTvColors.accent else LiveTvColors.textSubtle, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(item.title, color = LiveTvColors.textPrimary, fontSize = 18.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        item.count?.let { Text(it.toString(), color = LiveTvColors.textMuted, fontFamily = UltraFonts.Mono, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RootNavigationRail(formFactor: LiveTvFormFactor, modifier: Modifier = Modifier) {
    val railWidth = when (formFactor) {
        LiveTvFormFactor.Hd -> 4.dp
        LiveTvFormFactor.FullHd -> 5.dp
        LiveTvFormFactor.UltraHd -> 7.dp
    }
    Box(modifier.width(railWidth).fillMaxHeight().background(LiveTvColors.accent, LiveTvShapes.pill))
}

@Composable
private fun ChannelListOverlay(channels: List<ChannelEntity>, focusedId: Long?, categoryTitle: String, requester: FocusRequester, state: LazyListState, nowNext: Map<Long, Pair<EpgEntity?, EpgEntity?>>, locked: Set<String>, showNumbers: Boolean, favorites: Set<String>, activeId: Long?, modifier: Modifier = Modifier, onFocus: (ChannelEntity) -> Unit, onBackToCategories: () -> Unit, onChannelClick: (ChannelEntity) -> Unit, onOpenMenu: (ChannelEntity) -> Unit) {
    GroupPanel(channels, focusedId, categoryTitle, requester, state, nowNext, locked, showNumbers, favorites, activeId, modifier, onFocus, onBackToCategories, onChannelClick, onOpenMenu)
}

@Composable
private fun PlayerControlsOverlay(channel: ChannelEntity?, locked: Boolean, onBack: () -> Unit, onPlay: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(LiveTvSpacing.itemGap)) {
        Card(onClick = onBack, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text("Canales", color = LiveTvColors.textPrimary, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
        Card(onClick = { if (channel != null && !locked) onPlay() }, colors = CardDefaults.colors(containerColor = if (locked) LiveTvColors.surfaceRaised else LiveTvColors.accent)) { Text(if (locked) "Canal bloqueado" else "Reproducir", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
    }
}

@Composable
private fun EpgGuide(now: EpgEntity?) {
    EpgProgressBar(now)
}

@Composable
private fun ProgramContextMenu(program: EpgEntity?, onReminder: () -> Unit, onDismiss: () -> Unit) {
    BackHandler { onDismiss() }
    Box(Modifier.fillMaxSize().background(LiveTvColors.scrim), contentAlignment = Alignment.Center) {
        Column(Modifier.width(380.dp).background(LiveTvColors.surface, LiveTvShapes.menu).border(1.dp, LiveTvColors.outlineStrong, LiveTvShapes.menu).padding(18.dp)) {
            Text(program?.title ?: "Programa sin información", color = LiveTvColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(program?.let { "${fmt(it.startMs)} - ${fmt(it.endMs)}" } ?: "Sin horario disponible", color = LiveTvColors.textMuted, fontSize = 13.sp)
            Spacer(Modifier.height(14.dp))
            Card(onClick = onReminder, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text("Crear recordatorio", color = LiveTvColors.textPrimary, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
        }
    }
}

@Composable
private fun GroupPanel(channels: List<ChannelEntity>, focusedId: Long?, categoryTitle: String, requester: FocusRequester, state: LazyListState, nowNext: Map<Long, Pair<EpgEntity?, EpgEntity?>>, locked: Set<String>, showNumbers: Boolean, favorites: Set<String>, activeId: Long?, modifier: Modifier = Modifier, onFocus: (ChannelEntity) -> Unit, onBackToCategories: () -> Unit, onChannelClick: (ChannelEntity) -> Unit, onOpenMenu: (ChannelEntity) -> Unit) {
    Column(modifier.focusRequester(requester)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(categoryTitle, color = LiveTvColors.textPrimary, fontFamily = UltraFonts.Serif, fontSize = 32.sp, maxLines = 1)
                Text("${channels.size} canales · Back para volver a categorías", color = LiveTvColors.textMuted, fontSize = 12.sp)
            }
            Card(onClick = onBackToCategories, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text("Categorías", color = LiveTvColors.textPrimary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) }
        }
        Spacer(Modifier.height(16.dp))
        if (channels.isEmpty()) Box(Modifier.fillMaxSize().background(LiveTvColors.surface, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("No hay canales disponibles.", color = LiveTvColors.textMuted) }
        else LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(channels, key = { it.id }) { ch ->
                ChannelRow(ch, channels.indexOf(ch) + 1, focusedId == ch.id, activeId == ch.id, lockedKey(ch) in locked, ch.remoteId in favorites, showNumbers, nowNext[ch.id]?.first, nowNext[ch.id]?.second, onFocus, onChannelClick, onOpenMenu)
            }
        }
    }
}

@Composable
private fun ChannelRow(ch: ChannelEntity, number: Int, focused: Boolean, active: Boolean, locked: Boolean, favorite: Boolean, showNumber: Boolean, now: EpgEntity?, next: EpgEntity?, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit, onOpenMenu: (ChannelEntity) -> Unit) {
    val scale = if (focused) 1.015f else 1f
    Card(onClick = { onPlay(ch) }, shape = CardDefaults.shape(RoundedCornerShape(16.dp)), colors = CardDefaults.colors(containerColor = if (focused) LiveTvColors.surfaceRaised else LiveTvColors.surface), modifier = Modifier.fillMaxWidth().scale(scale).border(if (focused || active) 2.dp else 1.dp, if (active) LiveTvColors.live else if (focused) LiveTvColors.accent else LiveTvColors.outline, RoundedCornerShape(16.dp)).onFocusEventCompat { onFocus(ch) }.onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Menu) { onOpenMenu(ch); true } else false }) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showNumber) Text("%03d".format(number), color = LiveTvColors.textSubtle, fontFamily = UltraFonts.Mono, fontSize = 12.sp, modifier = Modifier.width(48.dp))
            ChannelLogo(ch.name, ch.logo, null, ch.name.hashCode(), null, 46.dp, false)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(ch.name, color = LiveTvColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1); if (active) Text("  En vivo", color = LiveTvColors.live, fontSize = 10.sp); if (favorite) Text("  ★", color = LiveTvColors.accent, fontSize = 13.sp); if (locked) Text("  Bloqueado", color = LiveTvColors.accent, fontSize = 10.sp) }
                ProgramInformation(now, next, compact = true)
            }
            StreamStatusIndicator(locked = locked, focused = focused)
        }
    }
}

private fun Modifier.onFocusEventCompat(block: () -> Unit) = this.then(Modifier.onFocusChanged { if (it.isFocused) block() })


@Composable
private fun LiveBackgroundPlayer(channel: ChannelEntity?, locked: Boolean, vm: LiveViewModel) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build().apply { playWhenReady = true } }
    val coordinator = vm.previewCoordinator
    val controller = remember(player) {
        object : PreviewPlayerController {
            override fun stop() { player.stop(); player.clearMediaItems() }
            override fun play(url: String) { player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url)); player.prepare() }
        }
    }
    DisposableEffect(player) { onDispose { coordinator.clear(controller); player.release() } }
    val scope = rememberCoroutineScope()
    LaunchedEffect(channel?.id, locked) { coordinator.request(scope, channel, locked, controller) }
    AndroidView(factory = { PlayerView(it).apply { useController = false; this.player = player } }, modifier = Modifier.fillMaxSize())
}


@Composable
private fun LiveChannelInfoBar(channel: ChannelEntity?, epg: Pair<EpgEntity?, EpgEntity?>?, locked: Boolean, favorite: Boolean, resolving: Boolean, modifier: Modifier = Modifier) {
    val now = epg?.first
    val next = epg?.second
    Column(
        modifier
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.18f to LiveTvColors.surface.copy(alpha = 0.86f),
                    1f to LiveTvColors.surface.copy(alpha = 0.94f),
                ),
            )
            .padding(horizontal = 40.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (channel != null) ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 72.dp, false)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    listOfNotNull(channel?.providerPosition?.takeIf { it > 0 }?.let { "%03d".format(it) }, channel?.name).joinToString("  ·  ").ifBlank { "Selecciona un canal" },
                    color = LiveTvColors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(6.dp))
                Text(now?.title ?: if (locked) "Canal bloqueado" else "Sin programa actual", color = LiveTvColors.textPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Text(now?.let { "${fmt(it.startMs)} - ${fmt(it.endMs)}" } ?: "Horario no disponible", color = LiveTvColors.textSecondary, fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt(System.currentTimeMillis()), color = LiveTvColors.textPrimary, fontFamily = UltraFonts.Mono, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(listOfNotNull(if (favorite) "★ Favorito" else null, if (resolving) "Cargando…" else null, if (locked) "Bloqueo parental" else null, "HD").joinToString("  ·  "), color = LiveTvColors.textMuted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        EpgProgressBar(now)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(next?.let { "Siguiente: ${it.title}" } ?: "Siguiente programa no disponible", color = LiveTvColors.textSecondary, fontSize = 16.sp, maxLines = 1, modifier = Modifier.weight(1f))
            Text(next?.let { "${fmt(it.startMs)} - ${fmt(it.endMs)}" } ?: "", color = LiveTvColors.textMuted, fontFamily = UltraFonts.Mono, fontSize = 14.sp)
        }
    }
}

private fun List<ChannelEntity>.nextFrom(current: ChannelEntity?): ChannelEntity? {
    if (isEmpty()) return null
    val index = indexOfFirst { it.id == current?.id }.takeIf { it >= 0 } ?: 0
    return this[(index + 1) % size]
}

private fun List<ChannelEntity>.previousFrom(current: ChannelEntity?): ChannelEntity? {
    if (isEmpty()) return null
    val index = indexOfFirst { it.id == current?.id }.takeIf { it >= 0 } ?: 0
    return this[(index - 1 + size) % size]
}

@Composable
private fun LiveNowPanel(channel: ChannelEntity?, epg: Pair<EpgEntity?, EpgEntity?>?, locked: Boolean, modifier: Modifier = Modifier, onGuide: () -> Unit) {
    Column(modifier.padding(8.dp), verticalArrangement = Arrangement.Bottom) {
        Spacer(Modifier.weight(1f))
        Column(Modifier.fillMaxWidth().background(LiveTvColors.surface.copy(alpha = 0.72f), LiveTvShapes.panel).padding(20.dp)) {
            Text(if (locked) "Canal bloqueado" else "Reproduciendo ahora", color = LiveTvColors.textMuted, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (channel != null) ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 54.dp, false)
                Spacer(Modifier.width(14.dp))
                Text(channel?.name ?: "Selecciona un canal", color = LiveTvColors.textPrimary, fontFamily = UltraFonts.Serif, fontSize = 30.sp, maxLines = 2)
            }
            Spacer(Modifier.height(12.dp))
            ProgramInformation(epg?.first, epg?.second, compact = false)
            Spacer(Modifier.height(12.dp))
            EpgProgressBar(epg?.first)
            Spacer(Modifier.height(16.dp))
            Card(onClick = onGuide, colors = CardDefaults.colors(containerColor = LiveTvColors.accent)) { Text("Abrir guía EPG", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
        }
    }
}

@Composable
private fun EpgOverlayGuide(channels: List<ChannelEntity>, activeId: Long?, focusedId: Long?, nowNext: Map<Long, Pair<EpgEntity?, EpgEntity?>>, favorites: Set<String>, locked: Set<String>, requester: FocusRequester, state: LazyListState, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit, onProgramMenu: (EpgEntity) -> Unit, onChannelMenu: (ChannelEntity) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.background(LiveTvColors.surface.copy(alpha = 0.94f), LiveTvShapes.panel).padding(18.dp).focusRequester(requester)) {
        Text("Guía EPG", color = LiveTvColors.textPrimary, fontFamily = UltraFonts.Serif, fontSize = 32.sp)
        Text("Video persistente · OK reproduce · Menú abre acciones · Back vuelve a canales", color = LiveTvColors.textMuted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        EpgTimeHeader()
        Spacer(Modifier.height(8.dp))
        if (channels.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay canales disponibles.", color = LiveTvColors.textMuted) } else LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(channels, key = { it.id }) { ch ->
                val pair = nowNext[ch.id]
                EpgGuideRow(ch, channels.indexOf(ch) + 1, activeId == ch.id, focusedId == ch.id, ch.remoteId in favorites, lockedKey(ch) in locked, pair?.first, pair?.second, onFocus, onPlay, onProgramMenu, onChannelMenu)
            }
        }
    }
}

@Composable
private fun EpgTimeHeader() {
    Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Canal", color = LiveTvColors.textMuted, fontSize = 13.sp, modifier = Modifier.width(240.dp))
        val now = System.currentTimeMillis()
        repeat(4) { slot -> Text(fmt(now + slot * 30 * 60_000L), color = LiveTvColors.textMuted, fontFamily = UltraFonts.Mono, fontSize = 13.sp, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun EpgGuideRow(ch: ChannelEntity, number: Int, active: Boolean, focused: Boolean, favorite: Boolean, locked: Boolean, now: EpgEntity?, next: EpgEntity?, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit, onProgramMenu: (EpgEntity) -> Unit, onChannelMenu: (ChannelEntity) -> Unit) {
    Row(Modifier.fillMaxWidth().height(76.dp).background(if (focused) LiveTvColors.surfaceRaised else LiveTvColors.surface, RoundedCornerShape(14.dp)).border(if (active || focused) 2.dp else 1.dp, if (active) LiveTvColors.live else if (focused) LiveTvColors.accent else LiveTvColors.outline, RoundedCornerShape(14.dp)).onFocusEventCompat { onFocus(ch) }.onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Menu) { onChannelMenu(ch); true } else false }, verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.width(240.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("%03d".format(number), color = LiveTvColors.textSubtle, fontFamily = UltraFonts.Mono, fontSize = 12.sp, modifier = Modifier.width(42.dp))
            ChannelLogo(ch.name, ch.logo, null, ch.name.hashCode(), null, 38.dp, false)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text(ch.name, color = LiveTvColors.textPrimary, fontSize = 15.sp, maxLines = 1); Text(listOfNotNull(if (active) "En vivo" else null, if (favorite) "Favorito" else null, if (locked) "Bloqueado" else null).joinToString(" · ").ifBlank { "Canal" }, color = LiveTvColors.textMuted, fontSize = 10.sp, maxLines = 1) }
        }
        ProgramCell(now, true, Modifier.weight(1.25f), onClick = { onPlay(ch) }, onMenu = { now?.let(onProgramMenu) })
        ProgramCell(next, false, Modifier.weight(1f), onClick = { next?.let(onProgramMenu) ?: onPlay(ch) }, onMenu = { next?.let(onProgramMenu) })
        Box(Modifier.width(2.dp).fillMaxHeight().background(LiveTvColors.accent))
        Text("Ahora", color = LiveTvColors.accent, fontSize = 10.sp, modifier = Modifier.width(52.dp).padding(start = 8.dp))
    }
}

@Composable
private fun ProgramCell(program: EpgEntity?, current: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit, onMenu: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.colors(containerColor = if (current) LiveTvColors.accentSoft else LiveTvColors.surfaceRaised), modifier = modifier.fillMaxHeight().padding(vertical = 7.dp, horizontal = 5.dp).onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Menu) { onMenu(); true } else false }) {
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.Center) {
            Text(program?.title ?: "Sin información", color = LiveTvColors.textPrimary, fontSize = 13.sp, maxLines = 1)
            Text(program?.let { "${fmt(it.startMs)} - ${fmt(it.endMs)}" } ?: "EPG no disponible", color = LiveTvColors.textMuted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun LivePreviewSurface(channel: ChannelEntity?, epg: Pair<EpgEntity?, EpgEntity?>?, locked: Boolean, focused: Boolean, requester: FocusRequester, vm: LiveViewModel, modifier: Modifier = Modifier, onBack: () -> Unit, onPlay: () -> Unit) {
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
            LivePreviewVideoSurface(
                player = player,
                state = state,
                focused = focused,
                modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
            )
        }
        Column(Modifier.weight(0.34f).fillMaxHeight()) {
            Text("Vista previa", color = LiveTvColors.textMuted, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (channel != null) ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 54.dp, false)
                Spacer(Modifier.width(14.dp))
                Text(channel?.name ?: "Selecciona un canal", color = LiveTvColors.textPrimary, fontFamily = UltraFonts.Serif, fontSize = 30.sp, maxLines = 3)
            }
            Spacer(Modifier.height(18.dp)); ProgramInformation(epg?.first, epg?.second, compact = false)
            Spacer(Modifier.height(14.dp)); EpgGuide(epg?.first)
            Spacer(Modifier.weight(1f))
            Text("Back vuelve a canales · OK abre pantalla completa", color = LiveTvColors.textMuted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            PlayerControlsOverlay(channel = channel, locked = locked, onBack = onBack, onPlay = onPlay)
        }
    }
}
@Composable
private fun LivePreviewVideoSurface(
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
                if (focused) LiveTvColors.accent else LiveTvColors.outlineStrong,
                RoundedCornerShape(24.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { PlayerView(it).apply { useController = false; this.player = player } },
            modifier = Modifier.fillMaxSize(),
        )
        if (state != PreviewPlaybackState.Playing) {
            Text(previewText(state), color = LiveTvColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
private fun ProgramInformation(now: EpgEntity?, next: EpgEntity?, compact: Boolean) { Column { Text(now?.title ?: "Sin EPG ahora", color = if (now == null) LiveTvColors.textSubtle else LiveTvColors.textSecondary, fontSize = if (compact) 12.sp else 16.sp, maxLines = if (compact) 1 else 2); next?.let { Text("Luego: ${it.title}", color = LiveTvColors.textSubtle, fontSize = if (compact) 11.sp else 13.sp, maxLines = 1) }; if (!compact && now != null) Text("${fmt(now.startMs)} - ${fmt(now.endMs)}", color = LiveTvColors.textMuted, fontFamily = UltraFonts.Mono, fontSize = 12.sp) } }
@Composable
private fun EpgProgressBar(now: EpgEntity?) { val progress = now?.let { ((System.currentTimeMillis() - it.startMs).toFloat() / (it.endMs - it.startMs).coerceAtLeast(1)).coerceIn(0f, 1f) } ?: 0f; Box(Modifier.fillMaxWidth().height(6.dp).background(LiveTvColors.outline, RoundedCornerShape(99.dp))) { Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(LiveTvColors.accent, RoundedCornerShape(99.dp))) } }
@Composable
private fun StreamStatusIndicator(locked: Boolean, focused: Boolean) { Box(Modifier.size(10.dp).background(if (locked) LiveTvColors.accent else if (focused) LiveTvColors.live else LiveTvColors.textSubtle, CircleShape)) }
@Composable
private fun ChannelContextMenu(channel: ChannelEntity, locked: Boolean, favorite: Boolean, now: EpgEntity?, onToggleFavorite: () -> Unit, onToggleLock: () -> Unit, onPlay: () -> Unit, onDismiss: () -> Unit) {
    BackHandler { onDismiss() }
    Box(Modifier.fillMaxSize().background(LiveTvColors.scrim), contentAlignment = Alignment.Center) {
        Column(Modifier.width(LiveTvDimensions.contextMenuWidth).background(LiveTvColors.surface, RoundedCornerShape(18.dp)).border(1.dp, LiveTvColors.outlineStrong, RoundedCornerShape(18.dp)).padding(18.dp)) {
            Text(channel.name, color = LiveTvColors.textPrimary, fontSize = 20.sp, fontFamily = UltraFonts.Serif)
            Text(now?.title ?: "Sin programa actual", color = LiveTvColors.textMuted, fontSize = 13.sp, maxLines = 2)
            Spacer(Modifier.height(14.dp))
            Card(onClick = onPlay, colors = CardDefaults.colors(containerColor = LiveTvColors.accent)) { Text("Ver sin cerrar reproducción", color = Color.White, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
            Spacer(Modifier.height(8.dp))
            Card(onClick = onToggleFavorite, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text(if (favorite) "Quitar de favoritos" else "Agregar a favoritos", color = LiveTvColors.textPrimary, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
            Spacer(Modifier.height(8.dp))
            Card(onClick = onToggleLock, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text(if (locked) "Desbloquear canal" else "Bloquear canal", color = LiveTvColors.textPrimary, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
        }
    }
}
private fun fmt(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
