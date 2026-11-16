package su.nnedition.kord.data.model

data class User(
    val username: String,
    val email: String? = null,
    val isAdmin: Boolean = false,
    val canStream: Boolean = true,
    val canDownload: Boolean = false,
    val canUpload: Boolean = false,
    val canSettings: Boolean = false,
    val allowedFolderIds: List<Int> = emptyList(),
    val maxBitRate: Int? = null
)
