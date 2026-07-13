package com.ultratv.tv.nativeapp.data.xtream

import com.ultratv.tv.nativeapp.data.db.CategoryEntity
import com.ultratv.tv.nativeapp.data.db.ChannelEntity
import com.ultratv.tv.nativeapp.data.db.EpgEntity
import com.ultratv.tv.nativeapp.data.db.EpisodeEntity
import com.ultratv.tv.nativeapp.data.db.MovieEntity
import com.ultratv.tv.nativeapp.data.db.ProviderEntity
import com.ultratv.tv.nativeapp.data.db.SeriesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Xtream-Codes player_api.php client. Endpoints used:
 *   action= get_live_categories | get_live_streams
 *           get_vod_categories  | get_vod_streams | get_vod_info
 *           get_series_categories | get_series   | get_series_info
 *           get_short_epg (channel-level EPG)
 *
 * Stream URLs:
 *   Live:    {base}/live/{user}/{pass}/{stream_id}.ts
 *   Movies:  {base}/movie/{user}/{pass}/{stream_id}.{container_extension}
 *   Episode: {base}/series/{user}/{pass}/{episode_id}.{container_extension}
 */
@Singleton
class XtreamClient @Inject constructor(private val ok: OkHttpClient) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    sealed class XtreamException(message: String) : RuntimeException(message) {
        class Network(message: String) : XtreamException(message)
        class Http(code: Int) : XtreamException("Error HTTP $code al contactar el servidor Xtream.")
        class InvalidJson : XtreamException("El servidor Xtream devolvió una respuesta JSON inválida.")
        class UnsupportedResponse(message: String) : XtreamException(message)
        class InvalidCredentials : XtreamException("Credenciales Xtream inválidas o cuenta no autorizada.")
        class ExpiredAccount : XtreamException("La cuenta Xtream está expirada.")
        class EmptyCatalog : XtreamException("La sincronización Xtream terminó con cero canales, películas y series. No se reemplazó el catálogo existente.")
    }

    suspend fun validateAccount(p: ProviderEntity) {
        val root = fetchJson(p, null)
        val obj = root as? JsonObject
            ?: throw XtreamException.UnsupportedResponse("El servidor Xtream no devolvió una respuesta de cuenta válida.")
        val userInfo = obj["user_info"] as? JsonObject
        val serverInfo = obj["server_info"] as? JsonObject
        if (userInfo == null && serverInfo == null) {
            throw describeObjectError(obj) ?: XtreamException.UnsupportedResponse("El servidor no parece compatible con Xtream Codes.")
        }
        val auth = userInfo?.get("auth")
        val authOk = when {
            auth == null -> true
            auth.str() == "1" -> true
            auth.str()?.equals("true", ignoreCase = true) == true -> true
            auth.jsonPrimitiveOrNull()?.booleanOrNull == true -> true
            else -> false
        }
        if (!authOk) throw XtreamException.InvalidCredentials()
        val status = userInfo?.get("status")?.str()?.trim().orEmpty()
        if (status.equals("Expired", ignoreCase = true)) throw XtreamException.ExpiredAccount()
        if (status.equals("Disabled", ignoreCase = true) || status.equals("Banned", ignoreCase = true)) {
            throw XtreamException.InvalidCredentials()
        }
        val exp = userInfo?.get("exp_date")?.str()?.toLongOrNull()
            ?: userInfo?.get("exp_date")?.jsonPrimitiveOrNull()?.longOrNull
        if (exp != null && exp > 0 && exp * 1000L < System.currentTimeMillis()) {
            throw XtreamException.ExpiredAccount()
        }
    }

    // ---- Live ----

    suspend fun fetchLiveCategories(p: ProviderEntity): List<CategoryEntity> = arrAt(p, "get_live_categories") { o ->
        val rid = o["category_id"]?.str() ?: return@arrAt null
        val name = o["category_name"]?.str() ?: return@arrAt null
        CategoryEntity(providerId = p.id, kind = "LIVE", remoteId = rid, name = name)
    }

    suspend fun fetchLiveStreams(p: ProviderEntity): List<ChannelEntity> = arrAt(p, "get_live_streams") { o ->
        val sid = o["stream_id"]?.str() ?: return@arrAt null
        val name = o["name"]?.str() ?: return@arrAt null
        val url = "${p.baseUrl}/live/${p.username.urlEnc()}/${p.password.urlEnc()}/$sid.ts"
        val tvArchive = o["tv_archive"]?.let { e -> e.str()?.toIntOrNull() ?: 0 } ?: 0
        val archiveDuration = o["tv_archive_duration"]?.let { e -> e.str()?.toIntOrNull() ?: 0 } ?: 0
        ChannelEntity(
            providerId = p.id,
            remoteId = sid,
            name = name,
            logo = o["stream_icon"]?.str(),
            categoryId = o["category_id"]?.str(),
            streamUrl = url,
            // Xtream exposes the xmltv ID either as epg_channel_id or, on some
            // panels, the same value embedded in tv_archive_duration JSON. We
            // take the canonical field and fall back to None.
            epgChannelId = o["epg_channel_id"]?.str()?.takeIf { it.isNotBlank() },
            // tv_archive == 1 means the provider keeps recordings; we synth
            // the timeshift URL from Catchup.synthesizeXtreamTimeshift().
            catchupSource = null,
            catchupDays = if (tvArchive >= 1) archiveDuration.coerceAtLeast(1) else 0,
        )
    }

    // ---- VOD (Movies) ----

    suspend fun fetchVodCategories(p: ProviderEntity): List<CategoryEntity> = arrAt(p, "get_vod_categories") { o ->
        val rid = o["category_id"]?.str() ?: return@arrAt null
        val name = o["category_name"]?.str() ?: return@arrAt null
        CategoryEntity(providerId = p.id, kind = "MOVIE", remoteId = rid, name = name)
    }

    suspend fun fetchVodStreams(p: ProviderEntity): List<MovieEntity> = arrAt(p, "get_vod_streams") { o ->
        val sid = o["stream_id"]?.str() ?: return@arrAt null
        val name = o["name"]?.str() ?: return@arrAt null
        val cont = o["container_extension"]?.str() ?: "mp4"
        val url = "${p.baseUrl}/movie/${p.username.urlEnc()}/${p.password.urlEnc()}/$sid.$cont"
        MovieEntity(
            providerId = p.id,
            remoteId = sid,
            name = name,
            poster = o["stream_icon"]?.str(),
            categoryId = o["category_id"]?.str(),
            streamUrl = url,
            container = cont,
            year = o["releaseDate"]?.str()?.take(4)?.toIntOrNull() ?: o["year"]?.str()?.toIntOrNull(),
            rating = o["rating"]?.str()?.toDoubleOrNull(),
            plot = null,
        )
    }

    // ---- Series ----

    suspend fun fetchSeriesCategories(p: ProviderEntity): List<CategoryEntity> = arrAt(p, "get_series_categories") { o ->
        val rid = o["category_id"]?.str() ?: return@arrAt null
        val name = o["category_name"]?.str() ?: return@arrAt null
        CategoryEntity(providerId = p.id, kind = "SERIES", remoteId = rid, name = name)
    }

    suspend fun fetchSeries(p: ProviderEntity): List<SeriesEntity> = arrAt(p, "get_series") { o ->
        val rid = o["series_id"]?.str() ?: return@arrAt null
        val name = o["name"]?.str() ?: return@arrAt null
        SeriesEntity(
            providerId = p.id,
            remoteId = rid,
            name = name,
            poster = o["cover"]?.str(),
            categoryId = o["category_id"]?.str(),
            year = o["releaseDate"]?.str()?.take(4)?.toIntOrNull(),
            rating = o["rating"]?.str()?.toDoubleOrNull(),
            plot = o["plot"]?.str(),
        )
    }

    /** Pull all episodes for one series. Returns pairs (season, episode_entity_without_id). */
    suspend fun fetchSeriesEpisodes(p: ProviderEntity, seriesRemoteId: String, seriesLocalId: Long): List<EpisodeEntity> {
        val body = get("${p.baseUrl}/player_api.php?username=${p.username.urlEnc()}&password=${p.password.urlEnc()}&action=get_series_info&series_id=$seriesRemoteId")
        val root = runCatching { json.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return emptyList()
        val episodes = root["episodes"] as? JsonObject ?: return emptyList()
        val out = mutableListOf<EpisodeEntity>()
        episodes.forEach { (seasonKey, listEl) ->
            val seasonNo = seasonKey.toIntOrNull() ?: 0
            val list = listEl as? JsonArray ?: return@forEach
            list.forEach { ep ->
                val o = ep as? JsonObject ?: return@forEach
                val rid = o["id"]?.str() ?: return@forEach
                val episodeNo = o["episode_num"]?.str()?.toIntOrNull() ?: 0
                val title = o["title"]?.str() ?: "Episode $episodeNo"
                val cont = o["container_extension"]?.str() ?: "mkv"
                val url = "${p.baseUrl}/series/${p.username.urlEnc()}/${p.password.urlEnc()}/$rid.$cont"
                out += EpisodeEntity(
                    seriesId = seriesLocalId,
                    remoteId = rid,
                    season = seasonNo,
                    episode = episodeNo,
                    title = title,
                    streamUrl = url,
                    container = cont,
                    plot = (o["info"] as? JsonObject)?.get("plot")?.str(),
                )
            }
        }
        return out
    }

    // ---- EPG ----

    /** Short EPG (next ~5 programmes) for a single channel. */
    suspend fun fetchShortEpg(p: ProviderEntity, channelRemoteId: String, channelLocalId: Long): List<EpgEntity> {
        val body = get("${p.baseUrl}/player_api.php?username=${p.username.urlEnc()}&password=${p.password.urlEnc()}&action=get_short_epg&stream_id=$channelRemoteId")
        val root = runCatching { json.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return emptyList()
        val listings = root["epg_listings"] as? JsonArray ?: return emptyList()
        return listings.mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            val title = decodeBase64(o["title"]?.str()) ?: return@mapNotNull null
            val desc = decodeBase64(o["description"]?.str())
            val start = o["start_timestamp"]?.str()?.toLongOrNull()?.times(1000)
                ?: o["start"]?.str()?.let { parseDate(it) } ?: return@mapNotNull null
            val end = o["stop_timestamp"]?.str()?.toLongOrNull()?.times(1000)
                ?: o["end"]?.str()?.let { parseDate(it) } ?: (start + 30 * 60_000)
            EpgEntity(channelId = channelLocalId, title = title, description = desc, startMs = start, endMs = end)
        }
    }

    // ---- Helpers ----

    private suspend inline fun <T : Any> arrAt(p: ProviderEntity, action: String, transform: (JsonObject) -> T?): List<T> {
        val root = fetchJson(p, action)
        val arr = root as? JsonArray ?: throw describeObjectError(root as? JsonObject)
            ?: XtreamException.UnsupportedResponse("El servidor Xtream devolvió una respuesta inesperada para $action.")
        return arr.mapNotNull { (it as? JsonObject)?.let(transform) }
    }

    private suspend fun fetchJson(p: ProviderEntity, action: String?): JsonElement {
        val url = buildApiUrl(p, action)
        val body = get(url)
        return try {
            json.parseToJsonElement(body)
        } catch (_: Throwable) {
            throw XtreamException.InvalidJson()
        }
    }

    private fun buildApiUrl(p: ProviderEntity, action: String?): String = buildString {
        append(p.baseUrl)
        append("/player_api.php?username=")
        append(p.username.urlEnc())
        append("&password=")
        append(p.password.urlEnc())
        if (action != null) {
            append("&action=")
            append(action)
        }
    }

    private fun describeObjectError(obj: JsonObject?): XtreamException? {
        if (obj == null) return null
        val message = listOf("message", "error", "status")
            .firstNotNullOfOrNull { key -> obj[key]?.str()?.takeIf { it.isNotBlank() } }
            .orEmpty()
        val userInfo = obj["user_info"] as? JsonObject
        val auth = obj["auth"] ?: userInfo?.get("auth")
        val status = userInfo?.get("status")?.str() ?: obj["status"]?.str()
        val looksLikeCredentialProblem = auth?.str() == "0" ||
            auth?.jsonPrimitiveOrNull()?.booleanOrNull == false ||
            message.contains("auth", ignoreCase = true) ||
            message.contains("credential", ignoreCase = true) ||
            message.contains("password", ignoreCase = true) ||
            message.contains("username", ignoreCase = true) ||
            message.contains("invalid", ignoreCase = true) ||
            status.equals("Disabled", ignoreCase = true) ||
            status.equals("Banned", ignoreCase = true)
        if (status.equals("Expired", ignoreCase = true) || message.contains("expir", ignoreCase = true)) {
            return XtreamException.ExpiredAccount()
        }
        if (looksLikeCredentialProblem) return XtreamException.InvalidCredentials()
        if (obj.keys.any { it in setOf("user_info", "server_info", "message", "error", "auth", "status") }) {
            return XtreamException.UnsupportedResponse(
                message.ifBlank { "El servidor Xtream devolvió un objeto de estado en lugar de un catálogo." }
            )
        }
        return null
    }

    private fun JsonElement.str(): String? = (this as? JsonPrimitive)?.contentOrNull
    private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? = this as? JsonPrimitive

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        try {
            ok.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful) throw XtreamException.Http(resp.code)
                resp.body?.string().orEmpty()
            }
        } catch (t: XtreamException) {
            throw t
        } catch (_: Throwable) {
            throw XtreamException.Network("No se pudo conectar con el servidor Xtream.")
        }
    }

    private fun String.urlEnc(): String = java.net.URLEncoder.encode(this, "UTF-8")

    private fun decodeBase64(s: String?): String? = s?.let {
        runCatching { String(android.util.Base64.decode(it, android.util.Base64.DEFAULT), Charsets.UTF_8) }.getOrNull()
    }

    private fun parseDate(s: String): Long? = runCatching {
        // Xtream sends "yyyy-MM-dd HH:mm:ss" in UTC.
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
        fmt.parse(s)?.time
    }.getOrNull()
}
