package su.nnedition.kord.data.model

data class ServerHealthInfo(
    val status: String,
    val isUp: Boolean,
    val components: List<ServerHealthComponent>
)
