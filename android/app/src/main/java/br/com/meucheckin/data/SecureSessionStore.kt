package br.com.meucheckin.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Guarda e-mail, senha e tokens criptografados (Android Keystore). */
class SecureSessionStore(context: Context) : SessionStore {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private fun prop(key: String) = object {
        fun get() = prefs.getString(key, null)
        fun set(v: String?) = prefs.edit().apply { if (v == null) remove(key) else putString(key, v) }.apply()
    }

    private val e = prop("email"); private val p = prop("password")
    private val a = prop("access"); private val r = prop("refresh")

    override var email: String? get() = e.get(); set(v) = e.set(v)
    override var password: String? get() = p.get(); set(v) = p.set(v)
    override var accessToken: String? get() = a.get(); set(v) = a.set(v)
    override var refreshToken: String? get() = r.get(); set(v) = r.set(v)
    override fun clear() { prefs.edit().clear().apply() }
}

