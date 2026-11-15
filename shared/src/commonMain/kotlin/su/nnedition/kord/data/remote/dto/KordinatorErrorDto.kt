package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class KordinatorErrorDto(
    val code: Int,
    val message: String
)
