package com.ultratv.tv.nativeapp.ui.live.guide

/** Capas de foco independientes de la guía EPG en Live TV. */
enum class EpgFocusLayer {
    ROOT_NAVIGATION,
    PLAYLIST,
    GROUP,
    CHANNEL_ROW,
    PROGRAM_CELL,
}

enum class EpgGuideDirection { UP, DOWN, LEFT, RIGHT }

data class EpgGuideUiState(
    val channelIds: List<Long> = emptyList(),
    val programIdsByChannel: Map<Long, List<Long>> = emptyMap(),
    val focusedChannelIndex: Int = 0,
    val focusedProgramIndex: Int = 0,
    val focusLayer: EpgFocusLayer = EpgFocusLayer.CHANNEL_ROW,
    val horizontalWindowOffset: Int = 0,
) {
    val focusedChannelId: Long? get() = channelIds.getOrNull(focusedChannelIndex)
    val focusedProgramId: Long? get() = focusedChannelId
        ?.let { programIdsByChannel[it].orEmpty().getOrNull(focusedProgramIndex) }
}

sealed interface EpgGuideAction {
    data class ChannelsChanged(val channelIds: List<Long>, val programIdsByChannel: Map<Long, List<Long>>) : EpgGuideAction
    data class Dpad(val direction: EpgGuideDirection) : EpgGuideAction
    data object FocusChannelRow : EpgGuideAction
    data object FocusProgramCell : EpgGuideAction
    data object FocusGroup : EpgGuideAction
    data object FocusPlaylist : EpgGuideAction
    data object FocusRootNavigation : EpgGuideAction
}

object EpgGuideReducer {
    fun reduce(state: EpgGuideUiState, action: EpgGuideAction): EpgGuideUiState = when (action) {
        is EpgGuideAction.ChannelsChanged -> state.copy(
            channelIds = action.channelIds,
            programIdsByChannel = action.programIdsByChannel,
        ).coerceFocus()
        is EpgGuideAction.Dpad -> state.onDpad(action.direction)
        EpgGuideAction.FocusChannelRow -> state.copy(focusLayer = EpgFocusLayer.CHANNEL_ROW).coerceFocus()
        EpgGuideAction.FocusProgramCell -> state.copy(focusLayer = EpgFocusLayer.PROGRAM_CELL).coerceFocus()
        EpgGuideAction.FocusGroup -> state.copy(focusLayer = EpgFocusLayer.GROUP)
        EpgGuideAction.FocusPlaylist -> state.copy(focusLayer = EpgFocusLayer.PLAYLIST)
        EpgGuideAction.FocusRootNavigation -> state.copy(focusLayer = EpgFocusLayer.ROOT_NAVIGATION)
    }

    private fun EpgGuideUiState.onDpad(direction: EpgGuideDirection): EpgGuideUiState = when (direction) {
        EpgGuideDirection.UP -> moveChannel(-1)
        EpgGuideDirection.DOWN -> moveChannel(1)
        EpgGuideDirection.LEFT -> when (focusLayer) {
            EpgFocusLayer.PROGRAM_CELL -> if (focusedProgramIndex <= 0) copy(focusLayer = EpgFocusLayer.CHANNEL_ROW) else moveProgram(-1)
            EpgFocusLayer.CHANNEL_ROW -> copy(focusLayer = EpgFocusLayer.GROUP)
            EpgFocusLayer.GROUP -> copy(focusLayer = EpgFocusLayer.PLAYLIST)
            EpgFocusLayer.PLAYLIST -> copy(focusLayer = EpgFocusLayer.ROOT_NAVIGATION)
            EpgFocusLayer.ROOT_NAVIGATION -> this
        }.coerceFocus()
        EpgGuideDirection.RIGHT -> when (focusLayer) {
            EpgFocusLayer.ROOT_NAVIGATION -> copy(focusLayer = EpgFocusLayer.PLAYLIST)
            EpgFocusLayer.PLAYLIST -> copy(focusLayer = EpgFocusLayer.GROUP)
            EpgFocusLayer.GROUP, EpgFocusLayer.CHANNEL_ROW -> copy(focusLayer = EpgFocusLayer.PROGRAM_CELL)
            EpgFocusLayer.PROGRAM_CELL -> moveProgram(1)
        }.coerceFocus()
    }

    private fun EpgGuideUiState.moveChannel(step: Int): EpgGuideUiState {
        if (channelIds.isEmpty()) return this
        val next = (focusedChannelIndex + step).coerceIn(channelIds.indices)
        return copy(focusedChannelIndex = next, focusedProgramIndex = horizontalWindowOffset).coerceFocus()
    }

    private fun EpgGuideUiState.moveProgram(step: Int): EpgGuideUiState {
        val count = focusedChannelId?.let { programIdsByChannel[it].orEmpty().size } ?: 0
        if (count <= 0) return copy(focusLayer = EpgFocusLayer.CHANNEL_ROW, focusedProgramIndex = 0)
        val next = (focusedProgramIndex + step).coerceIn(0, count - 1)
        return copy(
            focusLayer = EpgFocusLayer.PROGRAM_CELL,
            focusedProgramIndex = next,
            horizontalWindowOffset = next,
        )
    }

    private fun EpgGuideUiState.coerceFocus(): EpgGuideUiState {
        if (channelIds.isEmpty()) return copy(focusedChannelIndex = 0, focusedProgramIndex = 0)
        val safeChannel = focusedChannelIndex.coerceIn(channelIds.indices)
        val programCount = programIdsByChannel[channelIds[safeChannel]].orEmpty().size
        val safeProgram = if (programCount == 0) 0 else focusedProgramIndex.coerceIn(0, programCount - 1)
        return copy(focusedChannelIndex = safeChannel, focusedProgramIndex = safeProgram)
    }
}
