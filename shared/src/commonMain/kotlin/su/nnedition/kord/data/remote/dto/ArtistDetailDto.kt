package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtistDetailDto(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val coverArt: String? = null,
    val artistImageUrl: String? = null,
    val album: List<AlbumDto> = emptyList()
)
