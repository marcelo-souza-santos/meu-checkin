package br.com.meucheckin.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Armazenamento de credenciais/tokens (implementação segura no Android; fake nos testes). */
interface SessionStore {
    var email: String?
    var password: String?
    var accessToken: String?
    var refreshToken: String?
    fun clear()
}

class AuthException(message: String) : Exception(message)

class ApiClient(
    private val store: SessionStore,
    private val baseUrl: String = "https://academia-jiu-jitsu-api.onrender.com",
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS) // Render pode ter cold start
        .readTimeout(90, TimeUnit.SECONDS)
        .build()
    private val json = "application/json; charset=utf-8".toMediaType()
    private val lock = Any()

    private val defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/157.0.0.0 Safari/537.36"
    private val defaultReferer = "https://ct-pablo-forlan-bjj.pages.dev/"
    private val defaultOrigin = "https://ct-pablo-forlan-bjj.pages.dev"

    fun hasSavedCredentials() = !store.email.isNullOrBlank() && !store.password.isNullOrBlank()

    /** Login com e-mail/senha. Salva credenciais e tokens somente se der certo. */
    fun login(email: String, password: String) {
        val body = JSONObject().put("email", email).put("password", password)
        val resp = post("/api/auth/login?photoMode=metadata", body)
        when (resp.first) {
            200 -> {
                storeTokens(JSONObject(resp.second))
                store.email = email
                store.password = password
            }
            400, 401, 403, 422 -> throw AuthException("E-mail ou senha inválidos")
            else -> throw IOException("Falha no login (HTTP ${resp.first})")
        }
    }

    fun logout() = store.clear()

    /** POST autenticado. Em 401: renova e tenta de novo. */
    fun postAuthenticated(path: String, body: JSONObject): Pair<Int, String> {
        var token = store.accessToken
        if (token.isNullOrBlank()) token = renew(null)
        var resp = rawPost(path, body, token)
        if (resp.first == 401) {
            token = renew(token)
            resp = rawPost(path, body, token)
        }
        if (resp.first == 401) throw AuthException("Sessão expirada")
        return resp
    }

    /** DELETE autenticado. */
    fun deleteAuthenticated(path: String): Pair<Int, String> {
        var token = store.accessToken
        if (token.isNullOrBlank()) token = renew(null)
        var resp = rawDelete(path, token)
        if (resp.first == 401) {
            token = renew(token)
            resp = rawDelete(path, token)
        }
        if (resp.first == 401) throw AuthException("Sessão expirada")
        return resp
    }

    /** GET autenticado. Em 401: refresh → relogin com senha salva → tenta novamente uma vez. */
    fun get(path: String): JSONObject {
        var token = store.accessToken
        if (token.isNullOrBlank()) token = renew(null)
        var resp = rawGet(path, token)
        if (resp.first == 401) {
            token = renew(token)
            resp = rawGet(path, token)
        }
        if (resp.first == 401) throw AuthException("Sessão expirada")
        if (resp.first !in 200..299) throw IOException("HTTP ${resp.first} em $path")
        return JSONObject(resp.second)
    }

    /** Renova o token, uma única vez por vez (outra thread que chegou depois reaproveita o resultado). */
    private fun renew(failedToken: String?): String = synchronized(lock) {
        val current = store.accessToken
        if (!current.isNullOrBlank() && current != failedToken) return current

        val refresh = store.refreshToken
        if (!refresh.isNullOrBlank()) {
            val r = post("/api/auth/refresh?photoMode=metadata", JSONObject().put("refresh_token", refresh))
            if (r.first == 200) {
                storeTokens(JSONObject(r.second))
                return store.accessToken!!
            }
        }
        val email = store.email
        val password = store.password
        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            val r = post("/api/auth/login?photoMode=metadata", JSONObject().put("email", email).put("password", password))
            if (r.first == 200) {
                storeTokens(JSONObject(r.second))
                return store.accessToken!!
            }
        }
        store.accessToken = null
        store.refreshToken = null
        throw AuthException("Faça login novamente")
    }

    private fun storeTokens(o: JSONObject) {
        store.accessToken = o.getString("access_token")
        store.refreshToken = o.optString("refresh_token").ifBlank { store.refreshToken }
    }

    private fun newBaseRequestBuilder(path: String): Request.Builder {
        return Request.Builder()
            .url(baseUrl + path)
            .header("User-Agent", defaultUserAgent)
            .header("Referer", defaultReferer)
            .header("Origin", defaultOrigin)
            .header("Accept", "application/json, text/plain, */*")
    }

    private fun post(path: String, body: JSONObject): Pair<Int, String> {
        val req = newBaseRequestBuilder(path)
            .post(body.toString().toRequestBody(json)).build()
        http.newCall(req).execute().use { return it.code to (it.body?.string() ?: "") }
    }

    private fun rawGet(path: String, token: String): Pair<Int, String> {
        val req = newBaseRequestBuilder(path)
            .header("Authorization", "Bearer $token").build()
        http.newCall(req).execute().use { return it.code to (it.body?.string() ?: "") }
    }

    private fun rawPost(path: String, body: JSONObject, token: String): Pair<Int, String> {
        val req = newBaseRequestBuilder(path)
            .header("Authorization", "Bearer $token")
            .post(body.toString().toRequestBody(json)).build()
        http.newCall(req).execute().use { return it.code to (it.body?.string() ?: "") }
    }

    private fun rawDelete(path: String, token: String): Pair<Int, String> {
        val req = newBaseRequestBuilder(path)
            .header("Authorization", "Bearer $token")
            .delete().build()
        http.newCall(req).execute().use { return it.code to (it.body?.string() ?: "") }
    }
}
