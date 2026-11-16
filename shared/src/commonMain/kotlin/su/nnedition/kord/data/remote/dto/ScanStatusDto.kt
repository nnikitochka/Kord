package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScanStatusDto(
    val scanning: Boolean = false,
    val count: Long = 0L
)
