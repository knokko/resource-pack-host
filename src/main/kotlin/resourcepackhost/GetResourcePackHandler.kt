package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

const val GET_RESOURCE_PACK_PREFIX = "/get-resource-pack/"

class GetResourcePackHandler(
    private val cache: ResourcePackCache
): HttpHandler {

    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {

            val hexHash = exchange.requestURI.path.substring(GET_RESOURCE_PACK_PREFIX.length)

            println("Request URI is: ${exchange.requestURI} so the hash is $hexHash")

            val content = this.cache.getFromCache(hexHash)

            if (content != null) {
                exchange.sendResponseHeaders(200, content.size.toLong())
                exchange.responseBody.write(content)
                exchange.responseBody.flush()
                println("Sent resource pack")
            } else {
                exchange.sendResponseHeaders(404, -1)
                println("Can't find resource pack")
            }

            exchange.responseBody.close()
        } else {
            println("The exchange is null?")
        }
    }
}
