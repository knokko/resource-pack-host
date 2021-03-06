package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class GetUploadFormHandler: HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            performExchange(exchange) {
                discardInput(exchange.requestBody)
                if (exchange.requestURI.toString() == "/" || exchange.requestURI.toString() == "/index.html") {
                    serveResource(exchange, "index.html", 200)
                } else {
                    exchange.sendResponseHeaders(404, -1)
                }
            }
        }
    }
}
