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
}
