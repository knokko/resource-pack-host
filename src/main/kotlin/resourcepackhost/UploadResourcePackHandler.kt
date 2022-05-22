package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.DataInputStream
import java.io.IOException
import java.lang.Integer.parseInt
import java.util.concurrent.ExecutorService

// Minecraft clients will simply reject bigger files
const val FILE_SIZE_LIMIT = 100 * 1024 * 1024

class UploadResourcePackHandler(
    private val cache: ResourcePackCache,
    private val threadPool: ExecutorService
): HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            threadPool.execute {

                var success = false
                val contentLengths = exchange.requestHeaders["Content-Length"]
                if (contentLengths != null && contentLengths.size == 1) {
                    val rawContentLength = contentLengths[0]
                    try {
                        val contentLength = parseInt(rawContentLength)

                        if (contentLength >= 0) {

                            success = true
                            val (responseCode, responseError) = if (contentLength <= FILE_SIZE_LIMIT) {
                                val content = ByteArray(contentLength)
                                DataInputStream(exchange.requestBody).readFully(content)

                                val fileContent = extractFileContent(content)
                                val fileError = validateResourcePackContent(fileContent)

                                if (fileError == null) {
                                    try {
                                        val hexHash = this.cache.putInCache(fileContent)
                                        serveResource(exchange, "upload-success.html", 200) { originalLine ->
                                            originalLine.replace("%RESOURCE_PACK_ID%", hexHash)
                                        }

                                        Pair(200, null)
                                    } catch (writeFailed: IOException) {
                                        Pair(500, "An IO error occurred on the server.")
                                    }
                                } else {
                                    Pair(400, fileError)
                                }
                            } else {
                                Pair(400, "This file is too large. It can be at most 100 MB.")
                            }

                            if (responseError != null) {
                                serveResource(exchange, "upload-error.html", responseCode) { originalLine ->
                                    originalLine.replace("%ERROR%", responseError)
                                }
                            }
                            exchange.responseBody.close()
                        }
                    } catch (badContentLength: NumberFormatException) {
                        // success will remain false
                    } catch (badContent: IOException) {
                        // success will remain false
                    }
                }

                if (!success) {
                    exchange.sendResponseHeaders(400, -1)
                    exchange.responseBody.close()
                }
            }
        }
    }
}