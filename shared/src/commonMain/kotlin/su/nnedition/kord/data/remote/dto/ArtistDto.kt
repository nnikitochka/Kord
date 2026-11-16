package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtistDto(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val coverArt: String? = null,
    val artistImageUrl: String? = null,
    val starred: String? = null
)
