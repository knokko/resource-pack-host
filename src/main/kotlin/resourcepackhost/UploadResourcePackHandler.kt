package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.DataInputStream
import java.io.IOException
import java.lang.Integer.parseInt

// Minecraft clients will simply reject bigger files
const val FILE_SIZE_LIMIT = 100 * 1024 * 1024

class UploadResourcePackHandler(private val cache: ResourcePackCache): HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            performExchange(exchange) {

                var hasValidContentLength = false
                val contentLengths = exchange.requestHeaders["Content-Length"]
                if (contentLengths != null && contentLengths.size == 1) {
                    val rawContentLength = contentLengths[0]
                    try {
                        val contentLength = parseInt(rawContentLength)

                        if (contentLength >= 0) {

                            val (responseCode, responseError) = if (contentLength <= FILE_SIZE_LIMIT) {
                                val content = ByteArray(contentLength)
                                hasValidContentLength = true
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
                                hasValidContentLength = true

                                // Browsers don't like it when I don't read the request body, even if I don't use it
                                discardInput(exchange.requestBody)
                                Pair(400, "This file is too large. It can be at most 100 MB.")
                            }


                            if (responseError != null) {
                                serveResource(exchange, "upload-error.html", responseCode) { originalLine ->
                                    originalLine.replace("%ERROR%", responseError)
                                }
                            }
                        }
                    } catch (badContentLength: NumberFormatException) {
                        // hasValidContentLength will remain false
                    } catch (badContent: IOException) {
                        // In this case, the request body could not be read for some reason.
                        // I don't think much can be done to handle this error
                        println("badContent: " + badContent.message)
                    }
                }

                if (!hasValidContentLength) {
                    discardInput(exchange.requestBody)
                    exchange.sendResponseHeaders(400, -1)
                }
            }
        }
    }
}
