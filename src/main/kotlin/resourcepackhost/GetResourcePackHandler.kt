package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.util.concurrent.ExecutorService

const val GET_RESOURCE_PACK_PREFIX = "/get-resource-pack/"

class GetResourcePackHandler(
    private val cache: ResourcePackCache,
    private val threadPool: ExecutorService
): HttpHandler {

    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            threadPool.execute {
                performExchange(exchange) {
                    discardInput(exchange.requestBody)

                    val hexHash = exchange.requestURI.path.substring(GET_RESOURCE_PACK_PREFIX.length)
                    // TODO Performance: Don't read the entire resource pack for HEAD requests
                    val content = this.cache.getFromCache(hexHash)

                    if (content != null) {
                        if (exchange.requestMethod.lowercase() == "head") {
                            exchange.sendResponseHeaders(200, -1)
                        } else {
                            exchange.sendResponseHeaders(200, content.size.toLong())
                            exchange.responseBody.write(content)
                            exchange.responseBody.flush()
                        }
                    } else {
                        exchange.sendResponseHeaders(404, -1)
                    }
                }
            }
        }
    }
}
