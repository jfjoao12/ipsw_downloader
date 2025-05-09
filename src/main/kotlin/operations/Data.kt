package operations

import kotlinx.serialization.Serializable
import java.io.File

// All devices pulled from API
@Serializable
data class Device(
    val name: String,
    val identifier: String
)

data class FileInfo(
    val name: String,
    val version: String
)


data class IpswFileStatus(
    var file: File,
    var isUpToDate: Boolean? = null  // null = not yet checked
)

data class LatestVersion(
    val name: String,
    val version: String,
    val url: String,
)
