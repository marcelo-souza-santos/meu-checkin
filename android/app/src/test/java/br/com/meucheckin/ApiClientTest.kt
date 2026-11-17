package br.com.meucheckin

import br.com.meucheckin.data.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeStore : SessionStore {
    override var email: String? = null
    override var password: String? = null
    override var accessToken: String? = null
    override var refreshToken: String? = null
    override fun clear() { email = null; password = null; accessToken = null; refreshToken = null }
}

class ApiClientTest {
    private lateinit var server: MockWebServer
    private lateinit var store: FakeStore
    private lateinit var api: ApiClient

    private fun tokens(a: String, r: String) = """{"access_token":"$a","refresh_token":"$r"}"""

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        store = FakeStore()
        api = ApiClient(store, server.url("/").toString().trimEnd('/'))
    }
    @After fun tearDown() = server.shutdown()

    @Test fun loginOk_savesTokensAndCredentials() {
        server.enqueue(MockResponse().setBody(tokens("A1", "R1")))
        api.login("a@b.com", "pw")
        assertEquals("A1", store.accessToken); assertEquals("pw", store.password)
    }

    @Test fun loginWrongPassword_throwsAndSavesNothing() {
        server.enqueue(MockResponse().setResponseCode(401))
        assertThrows(AuthException::class.java) { api.login("a@b.com", "x") }
        assertNull(store.password)
    }

    @Test fun expiredToken_refreshesAndRetries() {
        store.accessToken = "old"; store.refreshToken = "R1"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody(tokens("A2", "R2")))
        server.enqueue(MockResponse().setBody("""{"ok":true}"""))
        assertTrue(api.get("/api/x").getBoolean("ok"))
        assertEquals("A2", store.accessToken)
        assertEquals("/api/auth/refresh?photoMode=metadata", server.takeRequest().let { server.takeRequest().path })
    }

    @Test fun refreshFails_fallsBackToRelogin() {
        store.accessToken = "old"; store.refreshToken = "dead"; store.email = "a@b.com"; store.password = "pw"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody(tokens("A3", "R3")))
        server.enqueue(MockResponse().setBody("""{"ok":true}"""))
        assertTrue(api.get("/api/x").getBoolean("ok"))
        assertEquals("A3", store.accessToken)
    }

    @Test fun everythingFails_throwsAuthException() {
        store.accessToken = "old"; store.refreshToken = "dead"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))
        assertThrows(AuthException::class.java) { api.get("/api/x") }
    }
}

