package su.nnedition.kord.data.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val coverArtUrl: String? = null,
    val songCount: Int = 0,
    val durationSeconds: Long = 0L,
    val year: Int? = null,
    val genre: String? = null,
    val accentColor: String? = null,
    val tracks: List<Track> = emptyList()
)
