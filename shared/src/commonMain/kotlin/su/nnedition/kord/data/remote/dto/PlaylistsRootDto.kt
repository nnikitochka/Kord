package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistsRootDto(
    val playlist: List<PlaylistDto> = emptyList()
)
