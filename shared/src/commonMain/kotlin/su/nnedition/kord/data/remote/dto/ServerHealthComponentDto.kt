package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ServerHealthComponentDto(
    val name: String,
    val status: String
)
