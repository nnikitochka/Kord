package su.nnedition.kord.data.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String?,
    val coverArtUrl: String?,
    val songCount: Int,
    val durationSeconds: Long,
    val year: Int?,
    val genre: String?,
    val tracks: List<Track> = emptyList()
)
