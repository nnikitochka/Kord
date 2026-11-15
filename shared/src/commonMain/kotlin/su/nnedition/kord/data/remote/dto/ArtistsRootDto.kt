package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtistsRootDto(
    val ignoredArticles: String? = null,
    val index: List<ArtistIndexDto> = emptyList()
)
