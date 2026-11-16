package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AlbumDto(
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val artistId: String? = null,
    val coverArt: String? = null,
    val songCount: Int? = null,
    val duration: Long? = null,
    val year: Int? = null,
    val genre: String? = null,
    val accentColor: String? = null,
    val starred: String? = null
)
