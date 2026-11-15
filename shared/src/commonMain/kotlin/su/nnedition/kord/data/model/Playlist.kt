package su.nnedition.kord.data.model

data class Playlist(
    val id: String,
    val name: String,
    val comment: String? = null,
    val owner: String? = null,
    val isPublic: Boolean = false,
    val songCount: Int = 0,
    val durationSeconds: Long = 0L,
    val created: String? = null,
    val changed: String? = null,
    val coverArtUrl: String? = null,
    val tracks: List<Track> = emptyList()
)
