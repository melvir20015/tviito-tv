package com.ultratv.tv.nativeapp.ui.live.guide

import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.db.EpgDao
import com.ultratv.tv.nativeapp.data.db.EpgEntity

/** Carga programas para la guía usando la consulta Room existente EpgDao.rangeForChannels(). */
class EpgGuideLoader(private val epgDao: EpgDao) {
    suspend fun loadRangeForChannels(
        channels: List<ChannelEntity>,
        windowStartMs: Long,
        windowEndMs: Long,
        chunkSize: Int = 500,
    ): Map<Long, List<EpgEntity>> {
        if (channels.isEmpty()) return emptyMap()
        return channels
            .map { it.id }
            .chunked(chunkSize)
            .flatMap { ids -> epgDao.rangeForChannels(ids, windowStartMs, windowEndMs) }
            .groupBy { it.channelId }
    }
}
