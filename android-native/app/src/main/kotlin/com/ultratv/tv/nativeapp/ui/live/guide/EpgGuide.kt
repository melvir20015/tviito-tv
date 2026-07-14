package com.ultratv.tv.nativeapp.ui.live.guide

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.db.EpgEntity
import com.ultratv.tv.nativeapp.ui.common.ChannelLogo
import com.ultratv.tv.nativeapp.ui.theme.UltraFonts
import com.ultratv.tv.nativeapp.ui.theme.UltraTokens
import com.ultratv.tv.nativeapp.ui.theme.ultraCardColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChannelWidth = 232.dp
private val RowHeight = 66.dp
private val HeaderHeight = 42.dp
private const val HourWidthDp = 260
private const val MinuteWidthDp = HourWidthDp / 60f

/** Guía EPG TV-first. La pantalla recibe canales y programas ya cargados con EpgDao.rangeForChannels(). */
@Composable
fun EpgGuide(
    channels: List<ChannelEntity>,
    programsByChannel: Map<Long, List<EpgEntity>>,
    windowStartMs: Long,
    windowEndMs: Long,
    nowMs: Long,
    selectedProgramId: Long? = null,
    playingChannelId: Long? = null,
    favoriteChannelIds: Set<Long> = emptySet(),
    onChannelFocus: (ChannelEntity) -> Unit = {},
    onProgramFocus: (ChannelEntity, EpgEntity?) -> Unit = { _, _ -> },
    onPlayChannel: (ChannelEntity, EpgEntity?) -> Unit,
    onOpenChannelMenu: (ChannelEntity) -> Unit = {},
    onOpenProgramMenu: (EpgEntity) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val hScroll = rememberScrollState()
    Column(modifier.background(Color(0xFF060812))) {
        EpgHeader(windowStartMs, windowEndMs, hScroll)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            items(channels, key = { it.id }) { channel ->
                EpgChannelRow(
                    channel = channel,
                    programs = programsByChannel[channel.id].orEmpty(),
                    windowStartMs = windowStartMs,
                    windowEndMs = windowEndMs,
                    nowMs = nowMs,
                    horizontalScrollState = hScroll,
                    isFavorite = channel.id in favoriteChannelIds,
                    isPlaying = channel.id == playingChannelId,
                    selectedProgramId = selectedProgramId,
                    onChannelFocus = { onChannelFocus(channel) },
                    onProgramFocus = { onProgramFocus(channel, it) },
                    onOpenChannelMenu = { onOpenChannelMenu(channel) },
                    onOpenProgramMenu = onOpenProgramMenu,
                    onPlay = { onPlayChannel(channel, it) },
                )
            }
        }
    }
}

@Composable
fun EpgHeader(
    windowStartMs: Long,
    windowEndMs: Long,
    horizontalScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().height(HeaderHeight), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Canal",
            color = UltraTokens.Fg3,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(ChannelWidth).padding(start = 12.dp),
        )
        Box(Modifier.fillMaxHeight().horizontalScroll(horizontalScrollState)) {
            val totalWidth = timelineWidth(windowStartMs, windowEndMs)
            Box(Modifier.width(totalWidth).fillMaxHeight()) {
                halfHourSlots(windowStartMs, windowEndMs).forEachIndexed { index, slotMs ->
                    Box(
                        Modifier
                            .offset(x = timeOffset(slotMs, windowStartMs))
                            .width((HourWidthDp / 2).dp)
                            .fillMaxHeight()
                            .background(if (index % 2 == 0) UltraTokens.Surface1 else Color.Transparent),
                    ) {
                        Text(formatGuideTime(slotMs), color = UltraTokens.Fg3, fontFamily = UltraFonts.Mono, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EpgChannelRow(
    channel: ChannelEntity,
    programs: List<EpgEntity>,
    windowStartMs: Long,
    windowEndMs: Long,
    nowMs: Long,
    horizontalScrollState: ScrollState,
    isFavorite: Boolean,
    isPlaying: Boolean,
    selectedProgramId: Long?,
    onChannelFocus: () -> Unit,
    onProgramFocus: (EpgEntity?) -> Unit,
    onOpenChannelMenu: () -> Unit,
    onOpenProgramMenu: (EpgEntity) -> Unit,
    onPlay: (EpgEntity?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().height(RowHeight), verticalAlignment = Alignment.CenterVertically) {
        Card(
            onClick = { onPlay(null) },
            modifier = Modifier.width(ChannelWidth).fillMaxHeight().padding(end = 8.dp, bottom = 4.dp).onFocusChanged { if (it.isFocused) onChannelFocus() }.onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Menu) { onOpenChannelMenu(); true } else false },
            shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
            colors = ultraCardColors(containerColor = if (isPlaying) UltraTokens.AccentSoft else UltraTokens.Surface1, focusedContainerColor = UltraTokens.Accent),
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                ChannelLogo(channel.name, channel.logo, null, channel.name.hashCode(), null, 38.dp, false)
                Spacer(Modifier.width(10.dp))
                Text(channel.name, color = UltraTokens.Fg, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, modifier = Modifier.weight(1f))
                if (isFavorite) Text("★", color = UltraTokens.Accent, fontSize = 15.sp)
                if (isPlaying) Text(" ▶", color = UltraTokens.Accent, fontSize = 13.sp)
            }
        }
        Box(Modifier.fillMaxHeight().horizontalScroll(horizontalScrollState)) {
            val visibleBlocks = remember(programs, windowStartMs, windowEndMs) {
                buildEpgTimelineBlocks(channel.id, programs, windowStartMs, windowEndMs)
            }
            Box(Modifier.width(timelineWidth(windowStartMs, windowEndMs)).fillMaxHeight()) {
                visibleBlocks.forEach { block ->
                    EpgProgramCell(
                        program = block.program,
                        blockStartMs = block.startMs,
                        blockEndMs = block.endMs,
                        windowStartMs = windowStartMs,
                        nowMs = nowMs,
                        selected = block.program?.id == selectedProgramId,
                        playing = isPlaying && nowMs >= block.startMs && nowMs < block.endMs,
                        noInformation = block.program == null,
                        catchUpAvailable = channel.catchupSource != null || channel.catchupDays > 0,
                        recordingPlaceholder = false,
                        reminderPlaceholder = false,
                        onFocus = { onProgramFocus(block.program) },
                        onOpenMenu = { block.program?.let(onOpenProgramMenu) },
                        onClick = { onPlay(block.program) },
                    )
                }
                CurrentTimeIndicator(windowStartMs, windowEndMs, nowMs)
            }
        }
    }
}

@Composable
fun EpgProgramCell(
    program: EpgEntity?,
    blockStartMs: Long,
    blockEndMs: Long,
    windowStartMs: Long,
    nowMs: Long,
    selected: Boolean,
    playing: Boolean,
    noInformation: Boolean,
    catchUpAvailable: Boolean,
    recordingPlaceholder: Boolean,
    reminderPlaceholder: Boolean,
    onFocus: () -> Unit,
    onOpenMenu: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val start = blockStartMs
    val end = blockEndMs
    val status = when {
        noInformation -> EpgProgramVisualState.NO_INFORMATION
        nowMs >= end -> EpgProgramVisualState.PAST
        nowMs in start until end -> EpgProgramVisualState.CURRENT
        else -> EpgProgramVisualState.FUTURE
    }
    val color = when (status) {
        EpgProgramVisualState.PAST -> Color(0xFF151927)
        EpgProgramVisualState.CURRENT -> UltraTokens.AccentSoft
        EpgProgramVisualState.FUTURE -> UltraTokens.Surface1
        EpgProgramVisualState.NO_INFORMATION -> Color(0xFF1B1B22)
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .offset(x = timeOffset(start.coerceAtLeast(windowStartMs), windowStartMs))
            .width(timeWidth(start, end))
            .fillMaxHeight()
            .padding(top = 4.dp, bottom = 4.dp, end = 3.dp)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Menu) { onOpenMenu(); true } else false }
            .then(if (selected) Modifier.border(2.dp, UltraTokens.Accent, RoundedCornerShape(10.dp)) else Modifier),
        shape = CardDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ultraCardColors(containerColor = color, focusedContainerColor = UltraTokens.Accent, focusedContentColor = Color.White),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 7.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(program?.title ?: "Sin información", color = if (noInformation) UltraTokens.Fg4 else UltraTokens.Fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${formatGuideTime(start)}–${formatGuideTime(end)}", color = UltraTokens.Fg3, fontFamily = UltraFonts.Mono, fontSize = 10.sp)
                if (playing) Text("▶", color = UltraTokens.Accent, fontSize = 10.sp)
                if (catchUpAvailable) Text("↺", color = UltraTokens.Fg3, fontSize = 10.sp)
                if (recordingPlaceholder) Text("●", color = Color(0xFFFF6B6B), fontSize = 10.sp)
                if (reminderPlaceholder) Text("⏰", color = UltraTokens.Fg3, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun CurrentTimeIndicator(windowStartMs: Long, windowEndMs: Long, nowMs: Long, modifier: Modifier = Modifier) {
    if (nowMs !in windowStartMs..windowEndMs) return
    Box(
        modifier
            .offset(x = timeOffset(nowMs, windowStartMs))
            .width(2.dp)
            .fillMaxHeight()
            .background(UltraTokens.Accent),
    )
}

enum class EpgProgramVisualState { PAST, CURRENT, FUTURE, NO_INFORMATION }

data class EpgTimelineBlock(val program: EpgEntity?, val startMs: Long, val endMs: Long)

fun buildEpgTimelineBlocks(channelId: Long, programs: List<EpgEntity>, windowStartMs: Long, windowEndMs: Long): List<EpgTimelineBlock> {
    require(channelId >= 0L) { "channelId debe ser no negativo" }
    val visible = programs.filter { it.endMs > windowStartMs && it.startMs < windowEndMs }.sortedBy { it.startMs }
    if (visible.isEmpty()) return listOf(EpgTimelineBlock(null, windowStartMs, windowEndMs))
    val blocks = mutableListOf<EpgTimelineBlock>()
    var cursor = windowStartMs
    visible.forEach { program ->
        val start = program.startMs.coerceAtLeast(windowStartMs)
        val end = program.endMs.coerceAtMost(windowEndMs)
        if (start > cursor) blocks += EpgTimelineBlock(null, cursor, start)
        if (end > start) blocks += EpgTimelineBlock(program, start, end)
        cursor = maxOf(cursor, end)
    }
    if (cursor < windowEndMs) blocks += EpgTimelineBlock(null, cursor, windowEndMs)
    return blocks
}

private fun timelineWidth(windowStartMs: Long, windowEndMs: Long): Dp = (((windowEndMs - windowStartMs) / 60_000f) * MinuteWidthDp).dp
private fun timeOffset(ms: Long, windowStartMs: Long): Dp = (((ms - windowStartMs) / 60_000f) * MinuteWidthDp).dp
private fun timeWidth(startMs: Long, endMs: Long): Dp = (((endMs - startMs).coerceAtLeast(5 * 60_000L) / 60_000f) * MinuteWidthDp).dp
private fun halfHourSlots(windowStartMs: Long, windowEndMs: Long): List<Long> = generateSequence(windowStartMs) { it + 30 * 60_000L }.takeWhile { it < windowEndMs }.toList()
private val guideTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private fun formatGuideTime(ms: Long): String = guideTimeFormat.format(Date(ms))


enum class EpgProgramAction { PAST, CURRENT, FUTURE }

fun epgProgramAction(startMs: Long, endMs: Long, nowMs: Long): EpgProgramAction = when {
    nowMs >= endMs -> EpgProgramAction.PAST
    nowMs in startMs until endMs -> EpgProgramAction.CURRENT
    else -> EpgProgramAction.FUTURE
}
