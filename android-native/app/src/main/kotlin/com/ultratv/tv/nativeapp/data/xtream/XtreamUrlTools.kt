package com.ultratv.tv.nativeapp.data.xtream

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Safe helpers for Xtream-compatible URLs. They never log or expose credentials. */
object XtreamUrlTools {
    data class NormalizedInput(
        val baseUrl: String,
        val username: String? = null,
        val password: String? = null,
    )

    fun normalizeInput(input: String): NormalizedInput {
        val trimmed = input.trim()
        val url = trimmed.toHttpUrlOrNull()
            ?: ("http://$trimmed").toHttpUrlOrNull()
            ?: return NormalizedInput(trimmed.trimEnd('/'))

        val lowerSegments = url.pathSegments.map { it.lowercase() }
        val apiIndex = lowerSegments.indexOfFirst { it == "player_api.php" || it == "get.php" }
        val basePathSegments = if (apiIndex >= 0) url.pathSegments.take(apiIndex) else url.pathSegments
        val cleanSegments = basePathSegments.filter { it.isNotBlank() }
        val builder = url.newBuilder()
            .query(null)
            .fragment(null)
            .encodedPath("/")
        cleanSegments.forEach { builder.addPathSegment(it) }
        val base = builder.build().toString().trimEnd('/')
        return NormalizedInput(
            baseUrl = base,
            username = url.queryParameter("username")?.takeIf { it.isNotBlank() },
            password = url.queryParameter("password")?.takeIf { it.isNotBlank() },
        )
    }

    fun buildApiUrl(baseUrl: String, username: String, password: String, action: String?): String {
        val normalized = normalizeInput(baseUrl).baseUrl
        val builder = ("$normalized/player_api.php").toHttpUrlOrNull()
            ?.newBuilder()
            ?: error("URL Xtream inválida")
        builder.addQueryParameter("username", username)
        builder.addQueryParameter("password", password)
        if (action != null) builder.addQueryParameter("action", action)
        return builder.build().toString()
    }

    fun classifyNonJsonResponse(body: String, contentType: String?): XtreamClient.XtreamException? {
        val sample = body.trimStart().take(512)
        if (body.isBlank()) return XtreamClient.XtreamException.EmptyResponse()
        if (contentType.orEmpty().contains("html", ignoreCase = true) || looksLikeHtml(sample)) {
            return XtreamClient.XtreamException.HtmlResponse()
        }
        if (sample.contains("blocked", ignoreCase = true) || sample.contains("forbidden", ignoreCase = true)) {
            return XtreamClient.XtreamException.UnsupportedResponse(
                "El servidor devolvió una respuesta de bloqueo o acceso denegado. Revisa la URL base o si el proveedor bloquea este cliente."
            )
        }
        return null
    }

    /**
     * Algunos paneles Xtream detrás de PHP/proxies devuelven avisos o texto plano
     * alrededor del JSON válido. Este helper conserva el camino estricto para
     * respuestas limpias y solo recorta cuando encuentra un objeto/arreglo JSON
     * balanceado dentro del cuerpo. No registra ni expone credenciales.
     */
    fun extractJsonPayload(body: String): String? {
        val trimmed = body.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) return trimmed
        val start = body.indexOfFirst { it == '{' || it == '[' }
        if (start < 0) return null
        val opening = body[start]
        val closing = if (opening == '{') '}' else ']'
        var depth = 0
        var inString = false
        var escaped = false
        for (index in start until body.length) {
            val char = body[index]
            if (escaped) {
                escaped = false
                continue
            }
            if (char == '\\' && inString) {
                escaped = true
                continue
            }
            if (char == '"') {
                inString = !inString
                continue
            }
            if (inString) continue
            if (char == opening) depth++
            if (char == closing) {
                depth--
                if (depth == 0) return body.substring(start, index + 1).trim()
            }
        }
        return null
    }

    private fun looksLikeHtml(sample: String): Boolean {
        val lower = sample.lowercase()
        return lower.startsWith("<!doctype") || lower.startsWith("<html") || lower.startsWith("<head") ||
            lower.startsWith("<body") || lower.contains("<html") || lower.contains("<head")
    }
}
