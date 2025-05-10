package operations

import kotlinx.coroutines.DelicateCoroutinesApi

/***
 *  ----- Main goal: Check if files are updated or not -----
 * 	Class: ValidateFile
 * 		purpose: validate if is up do date
 * 	    API Link:
 *
 *
 *
 * 	class: Fetch latest file and download
 *
 */




class Operations {
    private val deviceCache = mutableMapOf<String, Device>()
    private val api = ApiCalls()



    suspend fun extractiPhoneInfo(file: String): FileInfo {
        val name = file
            .removeSuffix("_Restore.ipsw").split("_")

        val fileBuildId = name[name.size - 1]
        val fileVersion = name[name.size - 2]
        var identifier = name[0]

        if(identifier.count() >= 10) {
            val strippedIdentifier = identifier.split(",")
            identifier = strippedIdentifier[0] + "," + strippedIdentifier[1]
        }

        if (identifier == "iPhone") {
            identifier = ApiCalls().fetchiPhoneIdentifierFromVersion(fileVersion, file)

        }


        val fileInfo = FileInfo(
            identifier = identifier,
            version = fileVersion,
        )
        return fileInfo
    }

    suspend fun fetchModelName(){

    }

    /**
      * Does string manipulation to retrieve the identifier from the
      * file name (iPhone10,3) then calls api with that. Retrieves iOS
      * version and compares with 1st record of JSON array. If iOS version
      * is the same, then true, false otherwise.
     **/
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun buildDevice(fileName: String): Device {
        var isUpdated = false

        val name = fileName
            .removeSuffix("_Restore.ipsw").split("_")

        val fileBuildId = name[name.size - 1]
        val fileVersion = name[name.size - 2]
        var identifier = name[0]

        if(identifier.count() >= 10) {
            val strippedIdentifier = identifier.split(",")
            identifier = strippedIdentifier[0] + "," + strippedIdentifier[1]
        }

        if (identifier == "iPhone") {
            identifier = ApiCalls().fetchiPhoneIdentifierFromVersion(fileVersion, fileName)
        }


        println("Identifier: $identifier")

        val deviceResponse = ApiCalls().grabLatestIOSVersion(identifier)
        var latestVersion: String? = null

        if (deviceResponse != null) {
            latestVersion = deviceResponse.firmwares.firstOrNull()?.version
            println("Current version: $fileVersion")
            println("Latest Version: $latestVersion")
        } else {
            latestVersion = "error"
        }

        if (fileVersion == latestVersion) {
            isUpdated = true
        }

        val apiDevice = ApiDevice(
            name = deviceResponse!!.name,
            identifier = deviceResponse.identifier,
            firmwares = deviceResponse.firmwares,
        )

        val device = Device(
            apiDevice = apiDevice,
            currentVersion= fileVersion,
            latestVersion = latestVersion ?: "",
            isUpToDate= isUpdated,
        )

        return device

    }

    fun version(fileName: String): String {
        val name = fileName
            .removeSuffix("_Restore.ipsw")

        val splitBuildNumber = name.split("_")
        val version = splitBuildNumber[splitBuildNumber.size -1]

        return version
    }

    fun extractFileNameFromString(url: String): String {


        val name = url.removeSuffix(".ipsw").split("/").last()
        return name
    }


}