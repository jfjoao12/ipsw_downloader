package operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL
import kotlin.collections.forEach

class ApiCalls {

    suspend fun fetchDevices(): List<DeviceNameID> {
        val url = "https://api.ipsw.me/v4/devices"

        val jsonString = withContext(Dispatchers.IO) {
            URL(url).readText()
        }

        val json = Json { ignoreUnknownKeys = true }

        val devices: List<DeviceNameID> = json.decodeFromString(jsonString)

        val filterForIphones = "iPhone*".toRegex()

        val devicesList: MutableList<DeviceNameID>  = mutableListOf()

        // Reducing the records for faster search
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


    suspend fun grabLatestIOSVersion(identifier: String): ApiDevice? {
        val url = "https://api.ipsw.me/v4/device/${identifier}?type=ipsw"

        val jsonString = withContext(Dispatchers.IO) {
            URL(url).readText()
        }

        val json = Json { ignoreUnknownKeys = true }

        val latestVersion: ApiDevice? = try{
            json.decodeFromString(jsonString)
        } catch(e: NullPointerException) {
            null
        }

        return latestVersion
    }

    suspend fun fetchiPhoneIdentifierFromVersion(
        version: String,
        fileName: String
    ): String {
        val url = "https://api.ipsw.me/v4/ipsw/$version"
        val jsonString = withContext(Dispatchers.IO) {
            URL(url).readText()
        }

        // 1) Decode into a List<Firmware>
        val firmwares: List<Firmware> = Json { ignoreUnknownKeys = true }
            .decodeFromString(jsonString)

        // 2) Find matching entry by URL (or just take the first one)
        val match = firmwares.firstOrNull { fw ->
            fw.url.substringAfterLast("/") == fileName
        } ?: error("No firmware entry matching $fileName")

        // 3) Return whatever you need
        return match.identifier
    }
}