package su.nnedition.kord.data.model

data class UserSession(
    val serverUrl: String,
    val username: String,
    val token: String,
    val isAdmin: Boolean = false
)
