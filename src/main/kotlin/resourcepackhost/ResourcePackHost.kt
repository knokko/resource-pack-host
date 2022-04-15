package resourcepackhost

import java.io.File
import java.io.IOException
import java.lang.Integer.parseInt

fun main(args: Array<String>) {
    var port = -1
    var backlog = -1
    var folderPath = ""

    for (arg in args) {
        println("arg is '$arg'")
        if (arg.startsWith("port=")) {
            if (port >= 0) {
                throw IllegalArgumentException("Multiple arguments try to specify the port")
            }
            try {
                port = parseInt(arg.substring(5))
                if (port < 0) {
                    throw IllegalArgumentException("Port can't be negative")
                }
            } catch (noInteger: NumberFormatException) {
                throw IllegalArgumentException("Can't parse port in '$arg'")
            }
        } else if (arg.startsWith("backlog=")) {
            if (backlog >= 0) {
                throw IllegalArgumentException("Multiple arguments try to specify the backlog")
            }
            try {
                backlog= parseInt(arg.substring(8))
                if (backlog < 0) {
                    throw IllegalArgumentException("Backlog can't be negative")
                }
            } catch (noInteger: NumberFormatException) {
                throw IllegalArgumentException("Can't parse backlog in '$arg'")
            }
        } else if (arg.startsWith("folder=")) {
            if (folderPath.isNotEmpty()) {
                throw IllegalArgumentException("Multiple arguments try to specify folder")
            }
            folderPath = arg.substring(7)
            if (folderPath.isEmpty()) {
                throw IllegalArgumentException("If you specify a folder path, it must not be empty")
            }
        } else {
            throw IllegalArgumentException("Unexpected argument '$arg'")
        }
    }

    if (port == -1) {
        port = 80
    }

    if (backlog == -1) {
        backlog = 0
    }

    if (folderPath.isEmpty()) {
        folderPath = "resource-packs/"
    }

    val folder = File(folderPath)
    if (!folder.isDirectory && !folder.mkdirs()) {
        throw IOException("Can't create folder $folder")
    }

    ResourcePackServer(port, backlog, folder).start()
}
