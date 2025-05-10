package operations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

// All devices pulled from API
@Serializable
data class DeviceNameID(
    val name: String,
    val identifier: String
)

data class FileInfo(
    val identifier: String,
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


@Serializable
data class ApiDevice(
    val name: String,
    val identifier: String,
    val firmwares: List<Firmware>
)

@Serializable
data class Firmware(
    val identifier: String,
    val version: String,
    @SerialName("buildid")
    val buildId: String,
    val url: String,
    val signed: Boolean,
    // add other fields you care about…
)

data class Device(
    val apiDevice: ApiDevice,
    val currentVersion: String,
    val latestVersion: String,
    val isUpToDate: Boolean,
)

