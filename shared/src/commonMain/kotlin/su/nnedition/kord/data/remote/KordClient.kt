package su.nnedition.kord.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class KordClient(
    private var config: ServerConfig = ServerConfig()
) {
    val httpClient = HttpClient()

    fun setSession(baseUrl: String, username: String, token: String) {
        config = ServerConfig(
            baseUrl = baseUrl.trim().trimEnd('/'),
            username = username,
            token = token
        )
    }

    fun clearSession() {
        config = ServerConfig()
    }

    fun updateConfig(newConfig: ServerConfig) {
        config = newConfig
    }

    fun getConfig(): ServerConfig = config

    suspend fun ping(baseUrl: String? = null): Boolean {
        val targetBase = (baseUrl ?: config.baseUrl).trim().trimEnd('/')
        if (targetBase.isEmpty()) return false

        val pingUrl = "$targetBase/rest/ping?v=1.15.0&c=kord&f=json"

        return withTimeoutOrNull(4000L.milliseconds) {
            try {
                val resp = httpClient.get(pingUrl)
                resp.status.value in 200..499
            } catch (_: Exception) {
                try {
                    val rootResp = httpClient.get(targetBase)
                    rootResp.status.value in 200..499
                } catch (_: Exception) {
                    false
                }
            }
        } ?: false
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<String> = runCatching {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val loginUrl = "$cleanBase/rest/kord/auth/login?u=$username&p=$password&v=1.15.0&c=kord&f=json"
        val response = httpClient.get(loginUrl)
        if (response.status.value in 200..299) {
            response.bodyAsText().trim()
        } else {
            error("Ошибка авторизации: код ${response.status.value}")
        }
    }

    fun close() {
        httpClient.close()
    }

    companion object {
        val default: KordClient by lazy { KordClient() }
    }
}
