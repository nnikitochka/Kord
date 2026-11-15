package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult3Dto(
    val artist: List<ArtistDto> = emptyList(),
    val album: List<AlbumDto> = emptyList(),
    val song: List<SongDto> = emptyList()
)
