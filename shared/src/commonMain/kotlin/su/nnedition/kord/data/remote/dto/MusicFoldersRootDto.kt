package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MusicFoldersRootDto(
    val musicFolder: List<MusicFolderDto> = emptyList()
)
