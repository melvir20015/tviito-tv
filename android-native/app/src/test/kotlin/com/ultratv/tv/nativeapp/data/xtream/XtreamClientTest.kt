package com.ultratv.tv.nativeapp.data.xtream

import com.ultratv.tv.nativeapp.data.db.ProviderEntity
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class XtreamClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: XtreamClient
    private lateinit var provider: ProviderEntity

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = XtreamClient(OkHttpClient())
        provider = ProviderEntity(
            id = 7,
            name = "Prueba",
            kind = "XTREAM",
            baseUrl = server.url("/").toString().trimEnd('/'),
            username = "usuario-secreto",
            password = "clave-secreta",
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun fetchLiveStreams_conArrayValido_devuelveCanales() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            [{"stream_id":"10","name":"Canal Uno","category_id":"1","stream_icon":""}]
        """.trimIndent()))

        val result = client.fetchLiveStreams(provider)

        assertEquals(1, result.size)
        assertEquals("Canal Uno", result.single().name)
        assertEquals("10", result.single().remoteId)
    }

    @Test
    fun fetchLiveStreams_conArrayVacioValido_devuelveListaVacia() = runBlocking {
        server.enqueue(MockResponse().setBody("[]"))

        val result = client.fetchLiveStreams(provider)

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun validateAccount_conObjetoDeCredencialesInvalidas_lanzaErrorSeguro() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            {"user_info":{"auth":0,"status":"Disabled"},"server_info":{}}
        """.trimIndent()))

        val error = assertFailsWith<XtreamClient.XtreamException.InvalidCredentials> {
            client.validateAccount(provider)
        }

        assertFalse(error.message.orEmpty().contains(provider.username))
        assertFalse(error.message.orEmpty().contains(provider.password))
    }

    @Test
    fun fetchLiveStreams_conHttpNoExitoso_lanzaErrorHttpSeguro() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("fallo"))

        val error = assertFailsWith<XtreamClient.XtreamException.Http> {
            client.fetchLiveStreams(provider)
        }

        assertFalse(error.message.orEmpty().contains(provider.username))
        assertFalse(error.message.orEmpty().contains(provider.password))
    }

    @Test
    fun fetchLiveStreams_conJsonInvalido_lanzaInvalidJson() = runBlocking {
        server.enqueue(MockResponse().setBody("esto no es json"))

        assertFailsWith<XtreamClient.XtreamException.InvalidJson> {
            client.fetchLiveStreams(provider)
        }
    }

    @Test
    fun fetchLiveStreams_conObjetoDeEstado_lanzaErrorComprensible() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            {"message":"Invalid username or password","auth":0}
        """.trimIndent()))

        assertFailsWith<XtreamClient.XtreamException.InvalidCredentials> {
            client.fetchLiveStreams(provider)
        }
    }

    @Test
    fun normalizeInput_conVariantesComunes_extraeBaseSinCredenciales() {
        val cases = mapOf(
            "http://host.example:8080" to "http://host.example:8080",
            "http://host.example:8080/" to "http://host.example:8080",
            "http://host.example:8080/player_api.php" to "http://host.example:8080",
            "http://host.example:8080/get.php?username=secreto&password=privado&type=m3u_plus" to "http://host.example:8080",
            "http://host.example:8080/player_api.php?username=secreto&password=privado" to "http://host.example:8080",
        )

        cases.forEach { (input, expected) ->
            assertEquals(expected, XtreamUrlTools.normalizeInput(input).baseUrl)
        }
    }

    @Test
    fun normalizeInput_conGetPhp_extraeCredencialesParaAutocompletarCamposVacios() {
        val result = XtreamUrlTools.normalizeInput(
            "http://host.example:8080/get.php?username=usuario&password=clave&type=m3u_plus"
        )

        assertEquals("http://host.example:8080", result.baseUrl)
        assertEquals("usuario", result.username)
        assertEquals("clave", result.password)
    }

    @Test
    fun buildApiUrl_noDuplicaPlayerApiNiQueryString() {
        val url = XtreamUrlTools.buildApiUrl(
            "http://host.example:8080/get.php?username=viejo&password=vieja&type=m3u_plus",
            "usuario nuevo",
            "clave nueva",
            "get_live_streams",
        )

        assertEquals(
            "http://host.example:8080/player_api.php?username=usuario%20nuevo&password=clave%20nueva&action=get_live_streams",
            url,
        )
        assertFalse(url.contains("get.php/player_api.php"))
        assertFalse(url.contains("player_api.php/player_api.php"))
    }

    @Test
    fun fetchLiveStreams_conHtml_lanzaErrorHtmlSeguro() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/html; charset=utf-8")
                .setBody("<html><body>usuario-secreto clave-secreta blocked</body></html>")
        )

        val error = assertFailsWith<XtreamClient.XtreamException.HtmlResponse> {
            client.fetchLiveStreams(provider)
        }

        assertFalse(error.message.orEmpty().contains(provider.username))
        assertFalse(error.message.orEmpty().contains(provider.password))
    }

    @Test
    fun fetchLiveStreams_conCuerpoVacio_lanzaErrorVacioSeguro() = runBlocking {
        server.enqueue(MockResponse().setBody(""))

        val error = assertFailsWith<XtreamClient.XtreamException.EmptyResponse> {
            client.fetchLiveStreams(provider)
        }

        assertFalse(error.message.orEmpty().contains(provider.username))
        assertFalse(error.message.orEmpty().contains(provider.password))
    }

    @Test
    fun fetchLiveStreams_enviaCabecerasGenericasPropias() = runBlocking {
        server.enqueue(MockResponse().setBody("[]"))

        client.fetchLiveStreams(provider)

        val request = server.takeRequest()
        assertEquals("TviitoTV/1.0 AndroidTV", request.getHeader("User-Agent"))
        assertEquals("application/json, text/plain, */*", request.getHeader("Accept"))
    }

}
