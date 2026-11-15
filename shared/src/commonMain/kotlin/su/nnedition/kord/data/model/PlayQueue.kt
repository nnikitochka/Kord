package su.nnedition.kord.data.model

data class PlayQueue(
    val currentTrackId: String? = null,
    val positionMs: Long = 0L,
    val username: String? = null,
    val changed: String? = null,
    val changedBy: String? = null,
    val tracks: List<Track> = emptyList()
)
