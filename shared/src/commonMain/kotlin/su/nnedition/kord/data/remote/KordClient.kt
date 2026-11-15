package su.nnedition.kord.data.remote

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.withTimeoutOrNull
import su.nnedition.kord.util.KermitKtorLogger
import kotlin.time.Duration.Companion.milliseconds

class KordClient(
    private var config: ServerConfig = ServerConfig()
) {
    val logger = Logger.withTag("KordClient")

    val httpClient = HttpClient {
        install(Logging) {
            logger = KermitKtorLogger("KordHttpClient")
            level = LogLevel.ALL
        }
    }

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
        logger.d { "Пинг сервера: $pingUrl" }

        return withTimeoutOrNull(4000L.milliseconds) {
            try {
                val resp = httpClient.get(pingUrl)
                val ok = resp.status.value in 200..499
                logger.d { "Результат пинга $pingUrl: HTTP ${resp.status.value} ($ok)" }
                ok
            } catch (e: Exception) {
                logger.w { "Ошибка пинга $pingUrl: ${e.message}, пробуем корень $targetBase" }
                try {
                    val rootResp = httpClient.get(targetBase)
                    val ok = rootResp.status.value in 200..499
                    logger.d { "Результат пинга корня $targetBase: HTTP ${rootResp.status.value} ($ok)" }
                    ok
                } catch (e2: Exception) {
                    logger.w { "Корень $targetBase недоступен: ${e2.message}" }
                    false
                }
            }
        } ?: run {
            logger.w { "Таймаут пинга для $targetBase" }
            false
        }
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<String> = runCatching {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val loginUrl = "$cleanBase/rest/kord/auth/login?u=$username&p=$password&v=1.15.0&c=kord&f=json"
        val maskedUrl = "$cleanBase/rest/kord/auth/login?u=$username&p=******&v=1.15.0&c=kord&f=json"
        logger.i { "Отправка запроса логина: $maskedUrl" }

        val response = httpClient.get(loginUrl)
        val body = response.bodyAsText().trim()
        logger.i { "Ответ сервера: HTTP ${response.status.value} ${response.status.description}" }
        logger.d { "Тело ответа: $body" }

        if (response.status.value in 200..299) {
            body
        } else {
            error("Ошибка авторизации (${response.status.value}): $body")
        }
    }.onFailure { error ->
        logger.e(error) { "Сбой при вызове login(): ${error.message}" }
    }

    fun close() {
        httpClient.close()
    }

    companion object {
        val default: KordClient by lazy { KordClient() }
    }
}
