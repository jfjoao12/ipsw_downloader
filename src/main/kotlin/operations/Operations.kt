package operations

import java.io.File

class Operations {
    fun extractIdentifierPart(file: String): FileInfo {
        val name = file
            .removeSuffix(".ipsw").split("_")

        val splitName = name[0]
        val splitVersion = name[1]

        if (name[0] ==  "iPhone"){
            // do logic here to grab the phone info by buildId
        }

        val fileInfo = FileInfo(
            name = splitName,
            version = splitVersion,
        )
        return fileInfo
    }

    fun extractFileNameFromString(url: String): String {
        val name = url.removeSuffix(".ipsw").split("/").last()
        return name
    }
}