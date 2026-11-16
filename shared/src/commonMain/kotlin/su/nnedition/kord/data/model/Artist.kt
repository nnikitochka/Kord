package su.nnedition.kord.data.model

data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int = 0,
    val coverArtUrl: String? = null,
    val albums: List<Album> = emptyList(),
    val isStarred: Boolean = false
)
