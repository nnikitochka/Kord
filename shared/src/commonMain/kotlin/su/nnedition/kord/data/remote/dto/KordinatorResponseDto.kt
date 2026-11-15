package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class KordinatorResponseDto(
    val status: String,
    val version: String? = null,
    val type: String? = null,
    val serverVersion: String? = null,
    val openSubsonic: Boolean? = null,
    val error: KordinatorErrorDto? = null,
    val artists: ArtistsRootDto? = null,
    val artist: ArtistDetailDto? = null,
    val albumList2: AlbumList2Dto? = null,
    val album: AlbumDetailDto? = null,
    val song: SongDto? = null,
    val searchResult3: SearchResult3Dto? = null
)
