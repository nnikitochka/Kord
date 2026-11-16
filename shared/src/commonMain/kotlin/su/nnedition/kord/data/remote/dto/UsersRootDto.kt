package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsersRootDto(
    val user: List<UserDto> = emptyList()
)
