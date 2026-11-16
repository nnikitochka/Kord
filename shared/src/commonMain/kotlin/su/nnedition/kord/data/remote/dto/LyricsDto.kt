package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LyricsDto(
    val artist: String? = null,
    val title: String? = null,
    val content: String? = null
)
