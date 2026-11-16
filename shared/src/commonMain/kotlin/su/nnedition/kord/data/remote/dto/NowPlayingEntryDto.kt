package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NowPlayingEntryDto(
    val username: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val playerName: String? = null,
    val minutesAgo: Int? = null,
    val coverArt: String? = null
)
