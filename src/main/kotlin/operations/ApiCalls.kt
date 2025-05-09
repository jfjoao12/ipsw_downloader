package operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL
import kotlin.collections.forEach

class ApiCalls {

    suspend fun grabAlliPhonesVersions(): List<Device> {
        val url = "https://api.ipsw.me/v4/devices"

        val jsonString = withContext(Dispatchers.IO) {
            URL(url).readText()
        }

        val json = Json { ignoreUnknownKeys = true }

        val devices: List<Device> = json.decodeFromString(jsonString)

        val filterForIphones = "iPhone*".toRegex()

        val devicesList: MutableList<Device>  = mutableListOf()

        devices.forEach { device ->
            if(device.name.contains("iPad")){
                println("> ${device.name} (id: ${device.identifier})")

                devicesList += device
            } else if(device.name.contains("iPhone")){
                println("> ${device.name} (id: ${device.identifier})")

                devicesList += device

            } else if(device.name.contains("iPod")){
                println("> ${device.name} (id: ${device.identifier})")

                devicesList += device
            }
        }
        return devicesList
    }

    suspend fun grabLatestIOSVersion(identifier: String): LatestVersion {
        val url = "https://api.ipsw.me/v4/device/${identifier}?type=ipsw"

        val jsonString = withContext(Dispatchers.IO) {
            URL(url).readText()
        }

        val json = Json { ignoreUnknownKeys = true }

        val latestVersion: List<LatestVersion> = json.decodeFromString(jsonString)



    }
}