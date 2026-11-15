package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AlbumList2Dto(
    val album: List<AlbumDto> = emptyList()
)
