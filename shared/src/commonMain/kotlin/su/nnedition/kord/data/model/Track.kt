package su.nnedition.kord.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val album: String = "",
    val albumId: String? = null,
    val durationSeconds: Long = 0L,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val coverArtUrl: String? = null,
    val streamUrl: String = "",
    val bitRate: Int? = null,
    val contentType: String? = null
)
