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
import androidx.compose.foundation.lazy.LazyRow
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
import com.ultratv.tv.nativeapp.ui.live.guide.EpgGuide as TimelineEpgGuide
import com.ultratv.tv.nativeapp.ui.live.guide.EpgProgramAction
import com.ultratv.tv.nativeapp.ui.live.guide.epgProgramAction
import com.ultratv.tv.nativeapp.ui.live.remote.RemoteActionMapper
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
fun LiveScreen(vm: LiveViewModel = hiltViewModel()) = LiveTvScreen(vm = vm)


@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvScreen(vm: LiveViewModel = hiltViewModel()) {
    val realCats by vm.categories.collectAsState()
    val channels by vm.channels.collectAsState()
    val hasLiveProvider by vm.hasLiveProvider.collectAsState()
    val selected by vm.selectedCategory.collectAsState()
    val locked by vm.lockedChannels.collectAsState()
    val nowNext by vm.nowNext.collectAsState()
    val guidePrograms by vm.guidePrograms.collectAsState()
    val showNumbers by vm.showChannelNumbers.collectAsState()
    val favorites by vm.favoriteRemoteIds.collectAsState()
    val s = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    val livePrefs by vm.livePreferences.collectAsState()
    val resolving by vm.resolving.collectAsState()
    var uiState by remember { mutableStateOf(LiveTvUiState()) }
    var activeChannel by remember { mutableStateOf<ChannelEntity?>(null) }
    var contextChannel by remember { mutableStateOf<ChannelEntity?>(null) }
    var contextProgram by remember { mutableStateOf<EpgEntity?>(null) }
    val categoryRequester = remember { FocusRequester() }
    val channelRequester = remember { FocusRequester() }
    val guideRequester = remember { FocusRequester() }
    val recentRequester = remember { FocusRequester() }
    val categoryListState = rememberLazyListState()
    val channelListState = rememberLazyListState()
    val guideListState = rememberLazyListState()
    var guideWindowStartMs by remember { mutableStateOf(0L) }
    var guideWindowEndMs by remember { mutableStateOf(0L) }
    var guideNowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    fun dispatch(action: LiveTvAction) { uiState = LiveTvReducer.reduce(uiState, action) }

    val categories = remember(realCats, selected, channels.size) {
        buildList {
            add(LiveCategoryUi(CATEGORY_ALL, s.liveAllChannels, if (selected == CATEGORY_ALL) channels.size else null))
            add(LiveCategoryUi(CATEGORY_FAVORITES, "Favoritos", if (selected == CATEGORY_FAVORITES) channels.size else null))
            add(LiveCategoryUi(CATEGORY_RECENTS, "Recientes"))
            realCats.forEach { add(LiveCategoryUi(it.remoteId, prettyCategoryName(it.name))) }
        }
    }
    val categoryTitle = categories.firstOrNull { it.id == selected }?.title ?: "Canales"
    val focusedChannel = channels.firstOrNull { it.id == uiState.focusedChannelId }
    val selectedChannel = channels.firstOrNull { it.id == uiState.selectedChannelId }
    val videoChannel = activeChannel ?: selectedChannel ?: focusedChannel ?: channels.firstOrNull()
    val contextSource = contextChannel ?: focusedChannel ?: videoChannel

    fun playInPlace(ch: ChannelEntity) {
        if (lockedKey(ch) in locked) { contextChannel = ch; dispatch(LiveTvAction.LongOk); return }
        activeChannel = ch
        dispatch(LiveTvAction.FocusChannel(ch.id))
        dispatch(LiveTvAction.PlayerLoading(ch.id))
        vm.resolveAndPlay(
            channel = ch,
            onReady = { _, _ -> dispatch(LiveTvAction.PlaybackReady) },
            onError = { message -> dispatch(LiveTvAction.PlaybackError(message)) },
        )
    }

    BackHandler(enabled = uiState.mode != LiveTvMode.FULLSCREEN_PLAYBACK) { dispatch(LiveTvAction.Back) }

    LaunchedEffect(channels.map { it.id }) { dispatch(LiveTvAction.ChannelsChanged(channels.map { it.id })) }
    LaunchedEffect(selected, channels, livePrefs.liveLastChannelRemoteId) {
        if (channels.isNotEmpty()) {
            val restoredId = livePrefs.liveLastChannelRemoteId
            val restoredChannel = channels.firstOrNull { it.remoteId == restoredId }
            val currentId = uiState.focusedChannelId?.takeIf { id -> channels.any { it.id == id } } ?: restoredChannel?.id ?: channels.first().id
            dispatch(LiveTvAction.FocusChannel(currentId))
            val index = channels.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
            channelListState.scrollToItem(index)
            guideListState.scrollToItem(index)
            if (activeChannel == null) activeChannel = restoredChannel ?: channels.getOrNull(index)
        }
    }
    LaunchedEffect(uiState.mode) {
        delay(LiveTvMotion.focusRestoreDelayMillis)
        runCatching {
            when (uiState.mode) {
                LiveTvMode.CATEGORY_PANEL_VISIBLE -> categoryRequester.requestFocus()
                LiveTvMode.CHANNEL_LIST_VISIBLE -> channelRequester.requestFocus()
                LiveTvMode.EPG_VISIBLE -> guideRequester.requestFocus()
                LiveTvMode.RECENT_CHANNELS_VISIBLE -> recentRequester.requestFocus()
                else -> Unit
            }
        }
    }
    LaunchedEffect(uiState.mode, channels.map { it.id }) {
        if (uiState.mode == LiveTvMode.EPG_VISIBLE) {
            val now = System.currentTimeMillis()
            guideNowMs = now
            guideWindowStartMs = now - 60 * 60_000L
            guideWindowEndMs = now + 5 * 60 * 60_000L
            vm.loadGuidePrograms(channels, guideWindowStartMs, guideWindowEndMs)
        }
    }
    LaunchedEffect(uiState.mode, activeChannel?.id) {
        if (uiState.mode == LiveTvMode.PROGRAM_INFO_VISIBLE) {
            delay(livePrefs.liveControlsTimeoutMs.coerceIn(4_000L, 6_000L))
            dispatch(LiveTvAction.Back)
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).apply { hide(WindowInsetsCompat.Type.systemBars()); systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
    }

    val visibleMode = uiState.openedFromMode.takeIf { uiState.mode == LiveTvMode.CONTEXT_MENU_VISIBLE } ?: uiState.mode

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black).onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        val action = RemoteActionMapper.map(event, RemoteActionMapper.surfaceFor(uiState.mode)) ?: return@onKeyEvent false
        val before = uiState.mode
        when (action) {
            LiveTvAction.Ok -> if (before == LiveTvMode.CHANNEL_LIST_VISIBLE || before == LiveTvMode.EPG_VISIBLE || before == LiveTvMode.RECENT_CHANNELS_VISIBLE) focusedChannel?.let(::playInPlace) ?: dispatch(action) else dispatch(action)
            LiveTvAction.LongOk -> { contextChannel = contextSource; contextProgram = null; dispatch(action) }
            LiveTvAction.ZapUp -> channels.previousFrom(videoChannel)?.let(::playInPlace) ?: dispatch(action)
            LiveTvAction.ZapDown -> channels.nextFrom(videoChannel)?.let(::playInPlace) ?: dispatch(action)
            else -> dispatch(action)
        }
        true
    }) {
        val formFactor = liveTvFormFactor(maxWidth)
        LiveBackgroundPlayer(
            channel = videoChannel,
            locked = lockedKey(videoChannel) in locked,
            vm = vm,
            onBuffering = { dispatch(LiveTvAction.PlayerLoading(videoChannel?.id)) },
            onReady = { dispatch(LiveTvAction.PlaybackReady) },
            onError = { dispatch(LiveTvAction.PlaybackError(it)) },
        )
        if (visibleMode != LiveTvMode.FULLSCREEN_PLAYBACK && visibleMode != LiveTvMode.PROGRAM_INFO_VISIBLE) Box(Modifier.fillMaxSize().background(LiveTvColors.scrim.copy(alpha = 0.44f)))
        val recentChannels = remember(channels, activeChannel?.id, livePrefs.livePreviousChannelRemoteId, livePrefs.liveLastChannelRemoteId) {
            val preferred = listOfNotNull(activeChannel?.remoteId, livePrefs.liveLastChannelRemoteId.takeIf { it.isNotBlank() }, livePrefs.livePreviousChannelRemoteId.takeIf { it.isNotBlank() })
            (preferred.mapNotNull { remoteId -> channels.firstOrNull { it.remoteId == remoteId } } + channels).distinctBy { it.id }.take(12)
        }
        Crossfade(targetState = visibleMode, animationSpec = tween(LiveTvMotion.panelCrossfadeMillis), label = "live-state-layer") { mode ->
            when (mode) {
                LiveTvMode.CHANNEL_LIST_VISIBLE -> Row(Modifier.fillMaxSize().padding(horizontal = LiveTvSpacing.screenHorizontal, vertical = LiveTvSpacing.screenVertical), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.width(520.dp).fillMaxHeight().background(LiveTvColors.surface.copy(alpha = 0.92f), LiveTvShapes.panel).padding(18.dp)) {
                        ChannelListOverlay(channels, uiState.focusedChannelId, categoryTitle, channelRequester, channelListState, nowNext, locked, showNumbers, favorites, activeChannel?.id, Modifier.fillMaxSize(), { ch -> dispatch(LiveTvAction.FocusChannel(ch.id)) }, { dispatch(LiveTvAction.Dpad(LiveTvDirection.LEFT)) }, ::playInPlace, { contextChannel = it; dispatch(LiveTvAction.LongOk) })
                    }
                    LiveNowPanel(videoChannel, nowNext[videoChannel?.id], lockedKey(videoChannel) in locked, Modifier.weight(1f).fillMaxHeight(), onGuide = { dispatch(LiveTvAction.OpenPanel(LiveTvMode.EPG_VISIBLE)) })
                }
                LiveTvMode.CATEGORY_PANEL_VISIBLE -> Row(Modifier.fillMaxSize().padding(horizontal = LiveTvSpacing.screenHorizontal, vertical = LiveTvSpacing.screenVertical), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.width(330.dp).fillMaxHeight().background(LiveTvColors.surface.copy(alpha = 0.94f), LiveTvShapes.panel).padding(18.dp)) {
                        RootNavigationRail(formFactor, Modifier.align(Alignment.CenterStart))
                        PlaylistPanel(categories, selected, categoryRequester, categoryListState, Modifier.padding(start = 16.dp).fillMaxSize()) { id -> if (id == CATEGORY_RECENTS) { dispatch(LiveTvAction.OpenPanel(LiveTvMode.RECENT_CHANNELS_VISIBLE)) } else { vm.selectCategory(id); dispatch(LiveTvAction.SelectGroup(id, channels.firstOrNull()?.id)) } }
                    }
                    Box(Modifier.width(520.dp).fillMaxHeight().background(LiveTvColors.surface.copy(alpha = 0.72f), LiveTvShapes.panel).padding(18.dp)) {
                        ChannelListOverlay(channels, uiState.focusedChannelId, categoryTitle, channelRequester, channelListState, nowNext, locked, showNumbers, favorites, activeChannel?.id, Modifier.fillMaxSize(), { ch -> dispatch(LiveTvAction.FocusChannel(ch.id)) }, { }, ::playInPlace, { contextChannel = it; dispatch(LiveTvAction.LongOk) })
                    }
                }
                LiveTvMode.EPG_VISIBLE -> EpgOverlayGuide(channels, activeChannel?.id, uiState.focusedChannelId, guidePrograms, guideWindowStartMs, guideWindowEndMs, guideNowMs, favorites, locked, guideRequester, { dispatch(LiveTvAction.FocusChannel(it.id)) }, ::playInPlace, { contextProgram = it; contextChannel = channels.firstOrNull { ch -> ch.id == it.channelId }; dispatch(LiveTvAction.LongOk) }, { contextChannel = it; dispatch(LiveTvAction.LongOk) }, Modifier.fillMaxSize().padding(horizontal = LiveTvSpacing.screenHorizontal, vertical = LiveTvSpacing.screenVertical))
                LiveTvMode.RECENT_CHANNELS_VISIBLE -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    RecentChannelsOverlay(recentChannels, uiState.focusedChannelId, activeChannel?.id, recentRequester, nowNext, locked, favorites, showNumbers, Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 34.dp), { dispatch(LiveTvAction.FocusChannel(it.id)) }, ::playInPlace, { contextChannel = it; dispatch(LiveTvAction.LongOk) })
                }
                else -> Unit
            }
        }
        AnimatedVisibility(visible = visibleMode == LiveTvMode.PROGRAM_INFO_VISIBLE, enter = slideInVertically(initialOffsetY = { it }), exit = slideOutVertically(targetOffsetY = { it }), modifier = Modifier.align(Alignment.BottomCenter)) {
            LiveChannelInfoBar(videoChannel, nowNext[videoChannel?.id], lockedKey(videoChannel) in locked, videoChannel?.remoteId in favorites, resolving || uiState.playerState.isLoading, Modifier.fillMaxWidth().fillMaxHeight(0.32f))
        }

        LivePlaybackStatusOverlay(
            hasProvider = hasLiveProvider,
            channelsLoading = !hasLiveProvider && channels.isEmpty() && realCats.isEmpty(),
            channelsEmpty = hasLiveProvider && channels.isEmpty(),
            locked = lockedKey(videoChannel) in locked,
            resolving = resolving,
            buffering = uiState.mode == LiveTvMode.BUFFERING || uiState.playerState.isLoading,
            errorMessage = uiState.playerState.errorMessage,
            modifier = Modifier.align(Alignment.Center),
        )
        if (uiState.mode == LiveTvMode.CONTEXT_MENU_VISIBLE) {
            LiveContextMenu(
                channel = contextSource,
                program = contextProgram,
                locked = lockedKey(contextSource) in locked,
                favorite = contextSource?.remoteId in favorites,
                now = contextSource?.let { nowNext[it.id]?.first },
                onReminder = { contextProgram?.let { program -> videoChannel?.let { vm.addReminder(it, program) } } },
                onToggleFavorite = { contextSource?.let(vm::toggleFavorite) },
                onToggleLock = { contextSource?.let(vm::toggleLock) },
                onPlay = { contextSource?.let { playInPlace(it); dispatch(LiveTvAction.Back) } },
                onDismiss = { dispatch(LiveTvAction.Back) },
            )
        }
    }
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
private fun RecentChannelsOverlay(
    channels: List<ChannelEntity>,
    focusedId: Long?,
    activeId: Long?,
    requester: FocusRequester,
    nowNext: Map<Long, Pair<EpgEntity?, EpgEntity?>>,
    locked: Set<String>,
    favorites: Set<String>,
    showNumbers: Boolean,
    modifier: Modifier = Modifier,
    onFocus: (ChannelEntity) -> Unit,
    onPlay: (ChannelEntity) -> Unit,
    onOpenMenu: (ChannelEntity) -> Unit,
) {
    Column(
        modifier
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.28f to LiveTvColors.surface.copy(alpha = 0.78f),
                    1f to LiveTvColors.surface.copy(alpha = 0.94f),
                ),
                LiveTvShapes.panel,
            )
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Text("Canales recientes", color = LiveTvColors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text("Muévete con izquierda/derecha; solo OK cambia la reproducción.", color = LiveTvColors.textMuted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        if (channels.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(104.dp), contentAlignment = Alignment.CenterStart) {
                Text("Todavía no hay canales recientes.", color = LiveTvColors.textMuted)
            }
        } else {
            LazyRow(
                modifier = Modifier.focusRequester(requester),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 24.dp),
            ) {
                items(channels, key = { it.id }) { ch ->
                    RecentChannelCard(
                        channel = ch,
                        number = channels.indexOf(ch) + 1,
                        focused = focusedId == ch.id,
                        active = activeId == ch.id,
                        locked = lockedKey(ch) in locked,
                        favorite = ch.remoteId in favorites,
                        showNumber = showNumbers,
                        now = nowNext[ch.id]?.first,
                        onFocus = onFocus,
                        onPlay = onPlay,
                        onOpenMenu = onOpenMenu,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentChannelCard(
    channel: ChannelEntity,
    number: Int,
    focused: Boolean,
    active: Boolean,
    locked: Boolean,
    favorite: Boolean,
    showNumber: Boolean,
    now: EpgEntity?,
    onFocus: (ChannelEntity) -> Unit,
    onPlay: (ChannelEntity) -> Unit,
    onOpenMenu: (ChannelEntity) -> Unit,
) {
    Card(
        onClick = { onPlay(channel) },
        shape = CardDefaults.shape(RoundedCornerShape(18.dp)),
        colors = CardDefaults.colors(containerColor = if (focused) LiveTvColors.surfaceRaised else LiveTvColors.surface.copy(alpha = 0.9f)),
        modifier = Modifier
            .width(260.dp)
            .height(124.dp)
            .border(
                if (focused || active) 2.dp else 1.dp,
                if (active) LiveTvColors.live else if (focused) LiveTvColors.accent else LiveTvColors.outline,
                RoundedCornerShape(18.dp),
            )
            .onFocusEventCompat { onFocus(channel) }
            .onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Menu) { onOpenMenu(channel); true } else false },
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showNumber) Text("%03d".format(number), color = LiveTvColors.textSubtle, fontFamily = UltraFonts.Mono, fontSize = 11.sp, modifier = Modifier.width(42.dp))
                ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 38.dp, false)
                Spacer(Modifier.width(10.dp))
                Text(channel.name, color = LiveTvColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                if (favorite) Text("★", color = LiveTvColors.accent, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(now?.title ?: if (locked) "Canal bloqueado" else "Sin EPG ahora", color = if (now == null) LiveTvColors.textSubtle else LiveTvColors.textSecondary, fontSize = 12.sp, maxLines = 2)
            Spacer(Modifier.weight(1f))
            Text(if (active) "Reproduciendo" else "OK para reproducir", color = if (active) LiveTvColors.live else LiveTvColors.textMuted, fontSize = 11.sp)
        }
    }
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
private fun LiveContextMenu(
    channel: ChannelEntity?,
    program: EpgEntity?,
    locked: Boolean,
    favorite: Boolean,
    now: EpgEntity?,
    onReminder: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLock: () -> Unit,
    onPlay: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler { onDismiss() }
    Box(Modifier.fillMaxSize().background(LiveTvColors.scrim.copy(alpha = 0.38f)), contentAlignment = Alignment.CenterEnd) {
        Column(Modifier.fillMaxHeight().width(LiveTvDimensions.contextMenuWidth).background(LiveTvColors.surface, LiveTvShapes.menu).border(1.dp, LiveTvColors.outlineStrong, LiveTvShapes.menu).padding(18.dp)) {
            Text(program?.title ?: channel?.name ?: "Opciones", color = LiveTvColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(8.dp))
            Text(program?.let { "${fmt(it.startMs)} - ${fmt(it.endMs)}" } ?: now?.title ?: "Sin programa actual", color = LiveTvColors.textMuted, fontSize = 13.sp, maxLines = 2)
            Spacer(Modifier.height(14.dp))
            if (program != null) {
                Card(onClick = onReminder, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text("Crear recordatorio", color = LiveTvColors.textPrimary, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
                Spacer(Modifier.height(8.dp))
            }
            if (channel != null) {
                Card(onClick = onPlay, colors = CardDefaults.colors(containerColor = LiveTvColors.accent)) { Text(if (locked) "Canal bloqueado" else "Reproducir canal", color = Color.White, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
                Spacer(Modifier.height(8.dp))
                Card(onClick = onToggleFavorite, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text(if (favorite) "Quitar de favoritos" else "Agregar a favoritos", color = LiveTvColors.textPrimary, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
                Spacer(Modifier.height(8.dp))
                Card(onClick = onToggleLock, colors = CardDefaults.colors(containerColor = LiveTvColors.surfaceRaised)) { Text(if (locked) "Desbloquear canal" else "Bloquear canal", color = LiveTvColors.textPrimary, modifier = Modifier.fillMaxWidth().padding(12.dp)) }
            }
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
private fun LiveBackgroundPlayer(
    channel: ChannelEntity?,
    locked: Boolean,
    vm: LiveViewModel,
    onBuffering: () -> Unit,
    onReady: () -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build().apply { playWhenReady = true } }
    val coordinator = vm.previewCoordinator
    val controller = remember(player) {
        object : PreviewPlayerController {
            override fun stop() { player.stop(); player.clearMediaItems() }
            override fun play(url: String) { player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url)); player.prepare() }
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> { coordinator.onBuffering(); onBuffering() }
                    Player.STATE_READY -> { coordinator.onPlaying(); onReady() }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                val safeMessage = error.message.sanitizeLivePlaybackMessage()
                coordinator.onError(safeMessage)
                onError(safeMessage)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            coordinator.clear(controller)
            player.release()
        }
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(channel?.id, locked) { coordinator.request(scope, channel, locked, controller) }
    AndroidView(factory = { PlayerView(it).apply { useController = false; this.player = player } }, modifier = Modifier.fillMaxSize())
}


@Composable
private fun LivePlaybackStatusOverlay(
    hasProvider: Boolean,
    channelsLoading: Boolean,
    channelsEmpty: Boolean,
    locked: Boolean,
    resolving: Boolean,
    buffering: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    val title = when {
        !hasProvider -> "Sin proveedor configurado"
        channelsLoading -> "Cargando canales…"
        channelsEmpty -> "Sin canales disponibles"
        locked -> "Canal bloqueado"
        errorMessage != null -> "Error de reproducción"
        resolving -> "Resolviendo stream…"
        buffering -> "Buffering…"
        else -> null
    } ?: return
    val detail = when {
        !hasProvider -> "Agrega o activa un proveedor para ver TV en vivo."
        channelsLoading -> "Buscando la lista local de canales."
        channelsEmpty -> "El proveedor activo no tiene canales visibles para esta categoría."
        locked -> "Este canal requiere desbloqueo antes de reproducirse."
        errorMessage != null -> errorMessage
        resolving -> "Preparando una URL reproducible sin mostrar datos sensibles."
        buffering -> "Esperando datos del canal seleccionado."
        else -> ""
    }
    Column(
        modifier
            .background(LiveTvColors.surface.copy(alpha = 0.90f), LiveTvShapes.panel)
            .border(1.dp, LiveTvColors.outline, LiveTvShapes.panel)
            .padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = LiveTvColors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(detail, color = LiveTvColors.textSecondary, fontSize = 15.sp, maxLines = 3)
    }
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
private fun EpgOverlayGuide(channels: List<ChannelEntity>, activeId: Long?, focusedId: Long?, programsByChannel: Map<Long, List<EpgEntity>>, windowStartMs: Long, windowEndMs: Long, nowMs: Long, favorites: Set<String>, locked: Set<String>, requester: FocusRequester, onFocus: (ChannelEntity) -> Unit, onPlay: (ChannelEntity) -> Unit, onProgramMenu: (EpgEntity) -> Unit, onChannelMenu: (ChannelEntity) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.background(LiveTvColors.surface.copy(alpha = 0.94f), LiveTvShapes.panel).padding(18.dp).focusRequester(requester)) {
        Text("Guía EPG", color = LiveTvColors.textPrimary, fontFamily = UltraFonts.Serif, fontSize = 32.sp)
        Text("Video persistente · OK reproduce el programa actual · Programas pasados/futuros abren acciones · Back vuelve a canales", color = LiveTvColors.textMuted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        if (channels.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay canales disponibles.", color = LiveTvColors.textMuted) }
        } else if (windowStartMs >= windowEndMs) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cargando guía EPG…", color = LiveTvColors.textMuted) }
        } else {
            TimelineEpgGuide(
                channels = channels,
                programsByChannel = programsByChannel,
                windowStartMs = windowStartMs,
                windowEndMs = windowEndMs,
                nowMs = nowMs,
                selectedProgramId = focusedId?.let { programsByChannel[it] }.orEmpty().firstOrNull { nowMs in it.startMs until it.endMs }?.id,
                playingChannelId = activeId,
                favoriteChannelIds = channels.filter { it.remoteId in favorites }.map { it.id }.toSet(),
                onChannelFocus = onFocus,
                onProgramFocus = { channel, _ -> onFocus(channel) },
                onPlayChannel = { channel, program ->
                    when (program?.let { epgProgramAction(it.startMs, it.endMs, nowMs) }) {
                        EpgProgramAction.PAST, EpgProgramAction.FUTURE -> onProgramMenu(program)
                        EpgProgramAction.CURRENT, null -> onPlay(channel)
                    }
                },
                onOpenChannelMenu = onChannelMenu,
                onOpenProgramMenu = onProgramMenu,
                modifier = Modifier.fillMaxSize(),
            )
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
private fun fmt(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
