package su.nnedition.kord.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String?,
    val album: String,
    val albumId: String?,
    val durationSeconds: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val genre: String?,
    val coverArtUrl: String?,
    val streamUrl: String,
    val bitRate: Int?,
    val contentType: String?
)
