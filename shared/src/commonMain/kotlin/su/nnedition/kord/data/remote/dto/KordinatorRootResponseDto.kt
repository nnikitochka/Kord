package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KordinatorRootResponseDto(
    @SerialName("kordinator-response")
    val response: KordinatorResponseDto
)
