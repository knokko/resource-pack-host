package resourcepackhost

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.lang.Thread.sleep
import java.net.InetSocketAddress

class ResourcePackServer(
    val port: Int,
    folder: File
) {

    private val cache = ResourcePackCache(folder)

    init {
        println("Preparing resource pack server at port $port and folder $folder")
    }

    fun start() {
        // TODO Make the backlog configurable
        val httpServer = HttpServer.create(InetSocketAddress(port), 0)
        println("And the hostname is ${httpServer.address.hostName}")

        httpServer.createContext("/", GetUploadFormHandler())
        httpServer.createContext(GET_RESOURCE_PACK_PREFIX, GetResourcePackHandler(this.cache))
        httpServer.createContext("/upload-resource-pack/", UploadResourcePackHandler(this.cache))

        httpServer.start()

        var isRunning = true

        val cacheThread = Thread {
            while (isRunning) {
                cache.update()
                sleep(60 * 1000)
            }
        }
        cacheThread.start()

        println("The server will stop upon receiving any line in System.in")
        readLine()
        println("Stopping server...")
        isRunning = false
        cacheThread.interrupt()
        httpServer.stop(2)
        println("Stopped server")
    }
}
