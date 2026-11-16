package su.nnedition.kord.data.model

data class StarredCollection(
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val tracks: List<Track> = emptyList()
)
