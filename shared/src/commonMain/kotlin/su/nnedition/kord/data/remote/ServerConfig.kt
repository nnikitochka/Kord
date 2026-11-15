package su.nnedition.kord.data.remote

data class ServerConfig(
    val baseUrl: String = "",
    val username: String = "",
    val token: String? = null
)
