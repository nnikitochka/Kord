package su.nnedition.kord.data.model

data class ServerHealthComponent(
    val name: String,
    val status: String,
    val isUp: Boolean
)
