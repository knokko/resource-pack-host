package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.lang.Integer.parseInt
import java.nio.charset.StandardCharsets
import java.nio.file.Files

// Minecraft clients will simply reject bigger files
const val FILE_SIZE_LIMIT = 100_000_000

class UploadResourcePackHandler(private val cache: ResourcePackCache): HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            println("Request URI is ${exchange.requestURI}")
            var success = false
            val contentLengths = exchange.requestHeaders["Content-Length"]
            if (contentLengths != null && contentLengths.size == 1) {
                val rawContentLength = contentLengths[0]
                try {
                    val contentLength = parseInt(rawContentLength)

                    if (contentLength >= 0) {
                        if (contentLength <= FILE_SIZE_LIMIT) {
                            success = true
                            val content = ByteArray(contentLength)
                            DataInputStream(exchange.requestBody).readFully(content)

                            val fileContent = extractFileContent(content)
                            // TODO Validate the contents
                            val hexHash = this.cache.putInCache(fileContent)

                            serveResource(exchange, "upload-success.html") { originalLine ->
                                originalLine.replace("%RESOURCE_PACK_ID%", hexHash)
                            }
                        } else {
                            println("The file is too large...")
                            // TODO Handle this gracefully
                        }
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