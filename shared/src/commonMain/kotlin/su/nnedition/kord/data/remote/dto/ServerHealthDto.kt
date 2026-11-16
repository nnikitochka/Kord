package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ServerHealthDto(
    val status: String,
    val component: List<ServerHealthComponentDto> = emptyList()
)
