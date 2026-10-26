package com.wesee.app.data

import android.content.Context
import android.net.Uri
import com.wesee.app.network.AuthSession
import java.io.File

/** Sessão + perfil local do usuário. Os dados de login vêm do backend (/auth/login). */
class UserStore(context: Context) {
    private val prefs = context.getSharedPreferences("wesee_user", Context.MODE_PRIVATE)

    val token: String? get() = prefs.getString("token", null)
    val tipo: String? get() = prefs.getString("tipo", null)
    val email: String get() = prefs.getString("email", "") ?: ""
    val name: String get() = prefs.getString("name", "") ?: ""
    val firstName: String get() = name.trim().split(" ").firstOrNull().orEmpty().ifBlank { "usuário" }
    /** Inicial usada no avatar quando não há foto. */
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "U"
    val isLoggedIn: Boolean get() = token != null

    fun saveSession(s: AuthSession) {
        prefs.edit().putString("token", s.token).putString("name", s.nome).putString("email", s.email).putString("tipo", s.tipo).apply()
    }

    fun updateName(name: String) { prefs.edit().putString("name", name).apply() }

    /** Encerra a sessão: apaga token, dados do usuário e a foto local. */
    fun logout() {
        avatarPath?.let { File(it).delete() }
        prefs.edit().clear().apply()
    }

    var avatarPath: String?
        get() = prefs.getString("avatar", null)?.takeIf { File(it).exists() }
        private set(value) { prefs.edit().putString("avatar", value).apply() }

    /** Copia a imagem escolhida para o armazenamento do app e a define como avatar. Chamar fora da thread principal. */
    fun saveAvatar(context: Context, uri: Uri): String? = try {
        val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { input.copyTo(it) } } ?: return null
        avatarPath?.let { File(it).delete() }
        avatarPath = file.absolutePath
        file.absolutePath
    } catch (e: Exception) { null }
}
