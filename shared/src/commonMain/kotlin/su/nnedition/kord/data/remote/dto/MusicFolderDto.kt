package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MusicFolderDto(
    val id: Int,
    val name: String
)
