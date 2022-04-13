package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class GetUploadFormHandler: HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            if (exchange.requestURI.toString() == "/" || exchange.requestURI.toString() == "/index.html") {
                exchange.responseHeaders["Content-Type"] = "text/html"
                serveResource(exchange, "index.html")
            } else if (exchange.requestURI.toString() == "/index.js") {
                exchange.responseHeaders["Content-Type"] = "application/javascript"
                serveResource(exchange, "index.js")
            } else {
                println("Request URI is ${exchange.requestURI}")
                println("And content length is ${exchange.requestHeaders["Content-Length"]}")
                println(exchange.requestBody.read())
                exchange.sendResponseHeaders(404, -1)
            }
            exchange.responseBody.close()
        }
    }
}
