package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayQueueDto(
    val current: Int? = null,
    val position: Long? = null,
    val username: String? = null,
    val changed: String? = null,
    val changedBy: String? = null,
    val entry: List<SongDto> = emptyList()
)
