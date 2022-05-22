package resourcepackhost

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class ResourcePackServer(
    val port: Int,
    val backlog: Int,
    folder: File
) {

    private val cache = ResourcePackCache(folder)
    private val threadPool = Executors.newFixedThreadPool(50)

    init {
        println("Preparing resource pack server at port $port with backlog $backlog and folder $folder")
    }

    fun start() {
        val httpServer = HttpServer.create(InetSocketAddress(port), backlog)
        println("And the hostname is ${httpServer.address.hostName}")

        httpServer.createContext("/", GetUploadFormHandler(this.threadPool))
        httpServer.createContext(GET_RESOURCE_PACK_PREFIX, GetResourcePackHandler(this.cache, this.threadPool))
        httpServer.createContext("/upload-resource-pack/", UploadResourcePackHandler(this.cache, this.threadPool))

        httpServer.start()

        var isRunning = true

        val cacheThread = Thread {
            try {
                while (isRunning) {
                    cache.update()
                    sleep(60 * 1000)
                }
            } catch (interrupted: InterruptedException) {
                // When this happens, the cache should stop
            }
            println("Stopped updating cache")
        }
        cacheThread.start()

        println("Available commands:")
        println(" - stop")
        println(" - update-cache")
        println(" - print-expiration-times")

        while (isRunning) {
            val command = readLine()
            if (command == "stop") {
                isRunning = false
            } else if (command == "update-cache") {
                cache.update()
            } else if (command == "print-expiration-times") {
                cache.printExpirationTimes()
            } else {
                println("Unknown command")
            }
        }

        println("Stopping server...")
        cacheThread.interrupt()
        httpServer.stop(1)
        threadPool.shutdown()
        println("Stopped server")
    }
}
