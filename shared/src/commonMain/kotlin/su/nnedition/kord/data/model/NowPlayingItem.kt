package su.nnedition.kord.data.model

data class NowPlayingItem(
    val username: String,
    val title: String,
    val artist: String,
    val album: String,
    val playerName: String,
    val minutesAgo: Int,
    val coverArtUrl: String? = null
)
