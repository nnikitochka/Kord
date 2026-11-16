package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NowPlayingRootDto(
    val entry: List<NowPlayingEntryDto> = emptyList()
)
