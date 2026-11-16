package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val id: String,
    val title: String? = null,
    val album: String? = null,
    val albumId: String? = null,
    val artist: String? = null,
    val artistId: String? = null,
    val track: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val coverArt: String? = null,
    val size: Long? = null,
    val contentType: String? = null,
    val suffix: String? = null,
    val duration: Long? = null,
    val bitRate: Int? = null,
    val path: String? = null,
    val accentColor: String? = null,
    val starred: String? = null
)
