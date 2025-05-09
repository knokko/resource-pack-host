package resourcepackhost

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.util.concurrent.Executors

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
        httpServer.executor = this.threadPool

        httpServer.createContext("/", GetUploadFormHandler())
        httpServer.createContext(GET_RESOURCE_PACK_PREFIX, GetResourcePackHandler(this.cache))
        httpServer.createContext("/upload-resource-pack/", UploadResourcePackHandler(this.cache))

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
        println(" - memory")

        while (isRunning) {
            val command = readLine()
            if (command == "stop") {
                isRunning = false
            } else if (command == "update-cache") {
                cache.update()
            } else if (command == "print-expiration-times") {
                cache.printExpirationTimes()
            } else if (command == "memory") {
                fun formatMemory(value: Long) = String.format("%.3f MB", value.toDouble() / (1024.0 * 1024.0))

                val freeMemory = Runtime.getRuntime().freeMemory()
                val totalMemory = Runtime.getRuntime().totalMemory()
                val maxMemory = Runtime.getRuntime().maxMemory()

                println("Claimed memory: ${formatMemory(totalMemory)} / ${formatMemory(maxMemory)}")
                println("Used memory: ${formatMemory(totalMemory - freeMemory)}")
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
