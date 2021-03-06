package resourcepackhost

import java.io.File
import java.io.IOException
import java.lang.Integer.parseInt

fun main(args: Array<String>) {

    fixMemoryLeaks()

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

fun fixMemoryLeaks() {
    // By default, HttpServer will keep stuck connections in memory indefinitely, which causes the memory usage to
    // pile up until it runs out of memory. By overriding this, stuck connections will be aborted after 600 seconds.
    // See https://github.com/nextgenhealthcare/connect/issues/4657
    if (System.getProperty("sun.net.httpserver.maxReqTime") == null) {
        System.setProperty("sun.net.httpserver.maxReqTime", "600")
    }

    if (System.getProperty("sun.net.httpserver.maxRspTime") == null) {
        System.setProperty("sun.net.httpserver.maxRspTime", "600")
    }

    // By default, all threads will have an NIO buffer cache that can hold up to 250MB of memory. If we have a lot of
    // threads, this is basically a massive memory leak. By limiting this to 50KB, it becomes barely noticeable.
    if (System.getProperty("jdk.nio.maxCachedBufferSize") == null) {
        System.setProperty("jdk.nio.maxCachedBufferSize", "50000")
    }
}