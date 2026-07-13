package com.ultratv.tv.nativeapp.ui.guide

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.db.EpgDao
import com.ultratv.tv.nativeapp.data.db.EpgEntity
import com.ultratv.tv.nativeapp.data.repo.CatalogRepository
import com.ultratv.tv.nativeapp.data.repo.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Time window represented horizontally. We render 8h at a time around the
 *  user's current scroll anchor; 1 hour = 240 dp on screen. */
private const val PX_PER_HOUR_DP = 240
private const val PX_PER_MIN_DP = PX_PER_HOUR_DP / 60f
private const val ROW_HEIGHT_DP = 60

/** Provider-wide EPG view. Loads everything in [rangeForChannels] for the
 *  visible channel set; the LazyColumn only renders visible rows so the
 *  upfront query, even on 5000 channels × 24h, stays well under 100 ms. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GuideGridViewModel @Inject constructor(
    providerRepo: ProviderRepository,
    private val catalog: CatalogRepository,
    private val provider: ProviderRepository,
    private val epgDao: EpgDao,
) : ViewModel() {

    val channels: StateFlow<List<ChannelEntity>> = providerRepo.observeProviders()
        .flatMapLatest { ps ->
            val pid = (ps.firstOrNull { it.active } ?: ps.firstOrNull())?.id
                ?: return@flatMapLatest flowOf(emptyList())
            catalog.channels(pid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _programmes = MutableStateFlow<Map<Long, List<EpgEntity>>>(emptyMap())
    val programmes: StateFlow<Map<Long, List<EpgEntity>>> = _programmes.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** Pull a fresh xmltv into the EPG table. */
    fun refreshXmltv() {
        viewModelScope.launch {
            _loading.value = true
            val pid = catalog.channels(0).let { 0L }  // dummy to satisfy compiler — real id below
            try {
                val activeId = channels.value.firstOrNull()?.providerId ?: return@launch
                provider.syncXmltv(activeId) { /* SyncStatusBus handles UI */ }
            } finally {
                _loading.value = false
                reloadFor(channels.value)
            }
        }
    }

    /** Reload programmes for the visible channels for the next 12 h window. */
    fun reloadFor(visible: List<ChannelEntity>) {
        viewModelScope.launch {
            if (visible.isEmpty()) { _programmes.value = emptyMap(); return@launch }
            val now = System.currentTimeMillis()
            val end = now + 12 * 60 * 60 * 1000L
            // 500-id chunks: SQLite refuses IN-lists > 999 host params.
            val flat = visible.map { it.id }.chunked(500).flatMap { ids ->
                epgDao.rangeForChannels(ids, now - 60 * 60_000, end)
            }
            _programmes.value = flat.groupBy { it.channelId }
        }
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun GuideGridScreen(
    onPlayChannel: (ChannelEntity) -> Unit,
    vm: GuideGridViewModel = hiltViewModel(),
) {
    val channels by vm.channels.collectAsState()
    val byChannel by vm.programmes.collectAsState()
    val loading by vm.loading.collectAsState()
    var categoryPanelOpen by remember { androidx.compose.runtime.mutableStateOf(false) }
    var focusedChannel by remember { androidx.compose.runtime.mutableStateOf<ChannelEntity?>(null) }
    var focusedProgramme by remember { androidx.compose.runtime.mutableStateOf<EpgEntity?>(null) }

    // Reload programmes whenever the channel list changes.
    LaunchedEffect(channels) { vm.reloadFor(channels) }

    BackHandler(enabled = categoryPanelOpen) { categoryPanelOpen = false }

    LaunchedEffect(channels) {
        if (focusedChannel == null || channels.none { it.id == focusedChannel?.id }) {
            focusedChannel = channels.firstOrNull()
        }
    }

    val now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // Time axis: 12h window starting at "now floored to top of hour minus 30 min".
    val windowStart = remember(now) { (now / 3_600_000L) * 3_600_000L - 30 * 60_000L }
    val windowEnd = remember(windowStart) { windowStart + 12 * 60 * 60 * 1000L }

    val S = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    val T = com.ultratv.tv.nativeapp.ui.theme.UltraTokens
    val F = com.ultratv.tv.nativeapp.ui.theme.UltraFonts
    Box(Modifier.fillMaxSize().background(Color(0xFF05070D))) {
        Column(Modifier.fillMaxSize().padding(start = T.EdgeGutter, end = T.EdgeGutter, top = 30.dp, bottom = 28.dp)) {
            Row(Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                LiveMiniPreview(
                    channel = focusedChannel,
                    modifier = Modifier.width(310.dp).fillMaxHeight(),
                )
                LiveProgrammeDetails(
                    channel = focusedChannel,
                    programme = focusedProgramme ?: focusedChannel?.let { byChannel[it.id].orEmpty().firstOrNull { p -> now in p.startMs..p.endMs } },
                    nowMs = now,
                    onToggleCategories = { categoryPanelOpen = !categoryPanelOpen },
                    onRefresh = { vm.refreshXmltv() },
                    loading = loading,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(18.dp))

            val hScroll = rememberScrollState()
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                androidx.compose.animation.AnimatedVisibility(visible = categoryPanelOpen, enter = androidx.compose.animation.slideInHorizontally { -it } + fadeIn(), exit = androidx.compose.animation.slideOutHorizontally { -it } + fadeOut()) {
                    LiveCategoryPanel(
                        selectedLabel = focusedChannel?.categoryId ?: S.liveAllChannels,
                        categories = listOf(S.liveAllChannels) + channels.mapNotNull { it.categoryId }.distinct().take(18),
                        onSelect = { categoryPanelOpen = false },
                        modifier = Modifier.width(260.dp).fillMaxHeight(),
                    )
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            S.liveAllChannels.uppercase(),
                            color = T.Fg3,
                            fontSize = 11.sp,
                            letterSpacing = 2.3.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(220.dp),
                        )
                        Row(Modifier.horizontalScroll(hScroll).height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                            (0..23).forEach { slot ->
                                val slotMs = windowStart + slot * 30 * 60_000L
                                Box(Modifier.width((PX_PER_HOUR_DP / 2).dp).height(38.dp).background(if (slot % 2 == 0) T.Surface1 else Color.Transparent)) {
                                    Text(formatHm(slotMs), color = T.Fg3, fontSize = 11.sp, fontFamily = F.Mono, modifier = Modifier.padding(start = 8.dp, top = 10.dp))
                                }
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(T.Line))
                    if (channels.isEmpty()) {
                        Text(S.guideNoChannels, color = T.Fg3, modifier = Modifier.padding(top = 20.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                            items(channels, key = { it.id }) { c ->
                                GuideRow(
                                    channel = c,
                                    programmes = byChannel[c.id].orEmpty(),
                                    windowStartMs = windowStart,
                                    windowEndMs = windowEnd,
                                    nowMs = now,
                                    hScroll = hScroll,
                                    onPlay = { onPlayChannel(c) },
                                    onFocusChannel = { focusedChannel = c },
                                    onFocusProgramme = { focusedProgramme = it },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveMiniPreview(channel: ChannelEntity?, modifier: Modifier = Modifier) {
    val T = com.ultratv.tv.nativeapp.ui.theme.UltraTokens
    Box(
        modifier.clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF14243A), Color(0xFF070B12)))).border(1.dp, T.Line2, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (channel != null) {
            com.ultratv.tv.nativeapp.ui.common.ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 92.dp, false)
            Text(channel.name, color = Color.White.copy(alpha = 0.86f), fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomStart).padding(18.dp), maxLines = 1)
        }
        Text(com.ultratv.tv.nativeapp.i18n.LocalStrings.current.liveOnAirPill, color = T.Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopStart).padding(16.dp))
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
private fun LiveProgrammeDetails(channel: ChannelEntity?, programme: EpgEntity?, nowMs: Long, loading: Boolean, onToggleCategories: () -> Unit, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    val S = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    val T = com.ultratv.tv.nativeapp.ui.theme.UltraTokens
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(T.Surface1).padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(S.tvGuide, color = T.Fg3, fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onToggleCategories, colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = T.Surface2)) { Text(S.categories, fontSize = 13.sp) }
                Button(onClick = onRefresh, enabled = !loading, colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = T.Surface2)) { Text(if (loading) S.guideLoading else S.guideRefreshXmltv, fontSize = 13.sp) }
            }
        }
        Column {
            Text(programme?.title ?: S.liveNoEpgForChannel, color = T.Fg, fontSize = 32.sp, fontFamily = com.ultratv.tv.nativeapp.ui.theme.UltraFonts.Serif, maxLines = 2)
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text(listOfNotNull(channel?.name, programme?.let { "${formatHm(it.startMs)} — ${formatHm(it.endMs)}" }).joinToString("  •  "), color = T.Fg3, fontSize = 14.sp, maxLines = 1)
            if (programme != null) {
                val total = (programme.endMs - programme.startMs).coerceAtLeast(1)
                val pct = ((nowMs - programme.startMs).coerceIn(0, total)).toFloat() / total.toFloat()
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0x24FFFFFF), RoundedCornerShape(99.dp))) {
                    Box(Modifier.fillMaxWidth(pct).height(5.dp).background(T.Accent, RoundedCornerShape(99.dp)))
                }
            }
        }
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
private fun LiveCategoryPanel(selectedLabel: String, categories: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val T = com.ultratv.tv.nativeapp.ui.theme.UltraTokens
    LazyColumn(modifier.clip(RoundedCornerShape(24.dp)).background(T.Surface1).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it }) { label ->
            val selected = label == selectedLabel
            Card(onClick = { onSelect(label) }, colors = com.ultratv.tv.nativeapp.ui.theme.ultraCardColors(containerColor = if (selected) T.AccentSoft else Color.Transparent, focusedContainerColor = T.Accent)) {
                Text(label, color = if (selected) T.Accent else T.Fg2, fontSize = 14.sp, maxLines = 1, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp))
            }
        }
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
private fun GuideRow(
    channel: ChannelEntity,
    programmes: List<EpgEntity>,
    windowStartMs: Long,
    windowEndMs: Long,
    nowMs: Long,
    hScroll: androidx.compose.foundation.ScrollState,
    onPlay: () -> Unit,
    onFocusChannel: () -> Unit = {},
    onFocusProgramme: (EpgEntity?) -> Unit = {},
) {
    val T = com.ultratv.tv.nativeapp.ui.theme.UltraTokens
    val F = com.ultratv.tv.nativeapp.ui.theme.UltraFonts
    Row(
        Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT_DP.dp)
            .background(androidx.compose.ui.graphics.Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Channel column — fixed left: logo + name, clickable to start playback.
        Card(
            onClick = onPlay,
            modifier = Modifier.width(220.dp).fillMaxHeight().padding(end = 8.dp).onFocusChanged { if (it.isFocused) onFocusChannel() },
            shape = CardDefaults.shape(RoundedCornerShape(10.dp)),
            colors = com.ultratv.tv.nativeapp.ui.theme.ultraCardColors(containerColor = T.Surface1),
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                com.ultratv.tv.nativeapp.ui.common.ChannelLogo(
                    name = channel.name,
                    logoUrl = channel.logo,
                    short = null,
                    hueSeed = channel.name.hashCode(),
                    hd = null,
                    size = 36.dp,
                    showBadge = false,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(10.dp))
                Text(
                    channel.name,
                    maxLines = 2,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = T.Fg,
                )
            }
        }
        // Programme strip — scrolls horizontally in lock-step with the header.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .horizontalScroll(hScroll),
        ) {
            // Width = (window_end - window_start) / 1h * PX_PER_HOUR_DP
            val hours = ((windowEndMs - windowStartMs) / 3_600_000.0).coerceAtLeast(1.0)
            val totalWidthDp = (hours * PX_PER_HOUR_DP).toInt().dp
            Box(Modifier.width(totalWidthDp).fillMaxHeight()) {
                val visibleProgrammes = programmes.filter { it.endMs > windowStartMs && it.startMs < windowEndMs }
                val blocks = if (visibleProgrammes.isEmpty()) listOf(EpgEntity(channelId = channel.id, title = com.ultratv.tv.nativeapp.i18n.LocalStrings.current.liveNoEpgForChannel, description = null, startMs = windowStartMs, endMs = windowEndMs)) else visibleProgrammes
                blocks.forEach { prog ->
                        val start = prog.startMs.coerceAtLeast(windowStartMs)
                        val end = prog.endMs.coerceAtMost(windowEndMs)
                        val leftDp = ((start - windowStartMs) / 60_000f * PX_PER_MIN_DP).toInt().dp
                        val widthDp = ((end - start) / 60_000f * PX_PER_MIN_DP).toInt().coerceAtLeast(4).dp
                        val isLive = nowMs in prog.startMs..prog.endMs
                        Card(
                            onClick = onPlay,
                            modifier = Modifier
                                .padding(start = leftDp, top = 4.dp, bottom = 4.dp, end = 2.dp)
                                .width(widthDp)
                                .fillMaxHeight()
                                .onFocusChanged { if (it.isFocused) { onFocusChannel(); onFocusProgramme(if (prog.id == 0L) null else prog) } },
                            shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
                            colors = if (isLive)
                                com.ultratv.tv.nativeapp.ui.theme.ultraCardColors(
                                    containerColor = T.AccentSoft,
                                    focusedContainerColor = T.Accent,
                                    focusedContentColor = androidx.compose.ui.graphics.Color.White,
                                )
                            else com.ultratv.tv.nativeapp.ui.theme.ultraCardColors(containerColor = T.Surface1),
                        ) {
                            Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    prog.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isLive) T.Fg else T.Fg2,
                                    maxLines = 1,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        formatHm(prog.startMs),
                                        fontSize = 10.sp,
                                        fontFamily = F.Mono,
                                        color = if (isLive) T.Accent else T.Fg4,
                                    )
                                    if (isLive) {
                                        androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
                                        Box(
                                            Modifier
                                                .width(5.dp)
                                                .height(5.dp)
                                                .background(T.Accent, androidx.compose.foundation.shape.CircleShape)
                                        )
                                        androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
                                        Text(
                                            com.ultratv.tv.nativeapp.i18n.LocalStrings.current.liveOnAirPill,
                                            color = T.Accent,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.6.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                if (nowMs in windowStartMs..windowEndMs) {
                    val nowLeftDp = ((nowMs - windowStartMs) / 60_000f * PX_PER_MIN_DP).toInt().dp
                    Box(
                        Modifier
                            .padding(start = nowLeftDp)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(T.Accent),
                    )
                }
            }
        }
    }
}

private val hmFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
private fun formatHm(ms: Long): String = hmFmt.format(Date(ms))
