package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.util.concurrent.ExecutorService

class GetUploadFormHandler(private val threadPoolExecutor: ExecutorService): HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            threadPoolExecutor.execute {
                if (exchange.requestURI.toString() == "/" || exchange.requestURI.toString() == "/index.html") {
                    serveResource(exchange, "index.html", 200)
                } else {
                    exchange.sendResponseHeaders(404, -1)
                }
                exchange.responseBody.close()
            }
        }
    }
}
