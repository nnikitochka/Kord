package su.nnedition.kord.data.model

data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val coverArtUrl: String?,
    val albums: List<Album> = emptyList()
)
