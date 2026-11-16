package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val username: String,
    val email: String? = null,
    val scrobblingEnabled: Boolean? = null,
    val adminRole: Boolean = false,
    val settingsRole: Boolean = false,
    val downloadRole: Boolean = false,
    val uploadRole: Boolean = false,
    val playlistRole: Boolean = false,
    val coverArtRole: Boolean = false,
    val commentRole: Boolean = false,
    val podcastRole: Boolean = false,
    val streamRole: Boolean = false,
    val jukeboxRole: Boolean = false,
    val shareRole: Boolean = false,
    val videoConversionRole: Boolean = false
)
