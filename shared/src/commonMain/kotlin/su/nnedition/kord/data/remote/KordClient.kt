package su.nnedition.kord.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

data class ServerConfig(
    val baseUrl: String = "",
    val username: String = "",
    val password: String = ""
)

class KordClient(
    private var config: ServerConfig = ServerConfig()
) {
    val httpClient = HttpClient()

    fun updateConfig(newConfig: ServerConfig) {
        config = newConfig
    }

    fun getConfig(): ServerConfig = config

    suspend fun ping(baseUrl: String? = null): Boolean {
        val targetBase = (baseUrl ?: config.baseUrl).trim().trimEnd('/')
        if (targetBase.isEmpty()) return false

        val pingUrl = "$targetBase/rest/ping.view?v=1.15.0&c=kord&f=json"

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

    fun close() {
        httpClient.close()
    }

    companion object {
        val default: KordClient by lazy { KordClient() }
    }
}
