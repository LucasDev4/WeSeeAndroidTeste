package com.wesee.app.network

import com.wesee.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object UserType {
    const val PCD_VISUAL = "PCD_VISUAL"
    const val RESPONSAVEL = "RESPONSAVEL"
}

data class AuthSession(val token: String, val nome: String, val email: String, val tipo: String)
data class RegisterBody(
    val nome: String, val email: String, val senha: String,
    val cpf: String? = null, val telefone: String? = null, val tipo: String = UserType.PCD_VISUAL,
)
/** Vínculo (GET /api/vinculos): "o outro lado" da relação. status: ATIVO | PENDENTE (reservado) | REVOGADO. */
data class VinculoItem(val id: Long, val relacionadoId: Long, val nome: String, val tipo: String, val status: String)
/** Código temporário gerado pelo backend (POST /api/vinculos/codigos). */
data class CodigoGerado(val codigo: String, val validadeSegundos: Long)
data class Profile(val id: Long, val nome: String, val cpf: String?, val email: String, val telefone: String?, val tipo: String)

/** code = -1 → sem conexão com o servidor. [fields] vem do erro 400 de validação do backend. */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Error(val code: Int, val message: String, val fields: Map<String, String> = emptyMap()) : ApiResult<Nothing>
}

/** Cliente do backend Backend-atlas (Spring Boot + JWT). Endpoints: /auth/register, /auth/login, /user/me. */
object AtlasApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
    private val json = "application/json; charset=utf-8".toMediaType()

    private suspend fun call(path: String, body: JSONObject?, token: String?, post: Boolean = body != null): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val builder = Request.Builder().url(BuildConfig.API_BASE_URL.trimEnd('/') + path).header("Accept", "application/json")
            token?.let { builder.header("Authorization", "Bearer $it") }
            if (post) builder.post((body?.toString() ?: "").toRequestBody(json)) else builder.get()
            client.newCall(builder.build()).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                val obj = try { JSONObject(text) } catch (e: JSONException) { JSONObject() }
                if (resp.isSuccessful) ApiResult.Success(obj) else {
                    val f = obj.optJSONObject("fields")
                    val fields = f?.let { o -> o.keys().asSequence().associateWith { k -> o.optString(k) } } ?: emptyMap()
                    ApiResult.Error(resp.code, obj.optString("message").ifBlank { "Erro ${resp.code}" }, fields)
                }
            }
        } catch (e: IOException) {
            ApiResult.Error(-1, "Sem conexão com o servidor (${BuildConfig.API_BASE_URL}).")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(-1, "Endereço do servidor inválido (API_BASE_URL).")
        }
    }

    private fun JSONObject.str(key: String): String? = if (isNull(key) || !has(key)) null else getString(key)

    private fun parseProfile(o: JSONObject) = Profile(
        o.optLong("id"), o.optString("nome"), o.str("cpf"), o.optString("email"), o.str("telefone"),
        o.str("tipo") ?: UserType.PCD_VISUAL,
    )

    suspend fun register(b: RegisterBody): ApiResult<Profile> {
        val body = JSONObject().put("nome", b.nome).put("email", b.email).put("senha", b.senha).put("tipo", b.tipo)
        b.cpf?.let { body.put("cpf", it) }
        b.telefone?.let { body.put("telefone", it) }
        return when (val r = call("/auth/register", body, null)) {
            is ApiResult.Success -> ApiResult.Success(parseProfile(r.value))
            is ApiResult.Error -> r
        }
    }

    suspend fun login(email: String, senha: String): ApiResult<AuthSession> {
        val body = JSONObject().put("email", email).put("senha", senha)
        return when (val r = call("/auth/login", body, null)) {
            is ApiResult.Success -> ApiResult.Success(
                AuthSession(r.value.optString("token"), r.value.optString("nome"), r.value.optString("email"),
                    r.value.str("tipo") ?: UserType.PCD_VISUAL))
            is ApiResult.Error -> r
        }
    }

    suspend fun me(token: String): ApiResult<Profile> = when (val r = call("/user/me", null, token)) {
        is ApiResult.Success -> ApiResult.Success(parseProfile(r.value))
        is ApiResult.Error -> r
    }

    // ---- Vínculos PCD <-> Responsável (identidade sempre pelo token) ----

    private fun parseVinculo(o: JSONObject) = VinculoItem(
        o.optLong("id"), o.optLong("relacionadoId"), o.optString("nomeRelacionado"),
        o.optString("tipoRelacionado"), o.optString("status"),
    )

    /** PCD: seus responsáveis. Responsável: seus PCDs. (backend: apenas vínculos ativos) */
    suspend fun listarVinculos(token: String): ApiResult<List<VinculoItem>> = when (val r = call("/api/vinculos", null, token)) {
        is ApiResult.Success -> {
            val arr = r.value.optJSONArray("vinculos")
            ApiResult.Success(if (arr == null) emptyList() else (0 until arr.length()).map { parseVinculo(arr.getJSONObject(it)) })
        }
        is ApiResult.Error -> r
    }

    /** PCD: pede um código temporário. O código só é devolvido aqui, uma vez. */
    suspend fun gerarCodigo(token: String): ApiResult<CodigoGerado> = when (val r = call("/api/vinculos/codigos", null, token, post = true)) {
        is ApiResult.Success -> ApiResult.Success(CodigoGerado(r.value.optString("codigo"), r.value.optLong("validadeSegundos")))
        is ApiResult.Error -> r
    }

    /** Responsável: usa o código recebido. O servidor decide se o vínculo fica ATIVO (ou PENDENTE). */
    suspend fun solicitarVinculo(token: String, codigo: String): ApiResult<VinculoItem> =
        when (val r = call("/api/vinculos/solicitar", JSONObject().put("codigo", codigo), token)) {
            is ApiResult.Success -> ApiResult.Success(parseVinculo(r.value))
            is ApiResult.Error -> r
        }
}

/** Frase falada quando o servidor não respondeu. No build debug inclui o endereço usado, para facilitar o diagnóstico. */
fun ApiResult.Error.spokenFailure(): String =
    (if (BuildConfig.DEBUG) message else "Não consegui falar com o servidor.") + " Toque no botão para tentar de novo."
