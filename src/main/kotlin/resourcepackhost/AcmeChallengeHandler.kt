package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.File
import java.nio.file.Files

const val ACME_CHALLENGE_PREFIX = "/.well-known/acme-challenge/"
private val challengeDirectory = File("./acme-challenge$ACME_CHALLENGE_PREFIX")

class AcmeChallengeHandler : HttpHandler {

	override fun handle(exchange: HttpExchange) {
		performExchange(exchange) {
			discardInput(exchange.requestBody)

			val challenge = exchange.requestURI.path.substring(ACME_CHALLENGE_PREFIX.length)
			if (challenge.toCharArray().all { it.isLetterOrDigit() || it == '_' || it == '-' }) {
				val content = Files.readAllBytes(File("$challengeDirectory/$challenge").toPath())
				exchange.sendResponseHeaders(200, content.size.toLong())
				exchange.responseBody.write(content)
				exchange.responseBody.flush()
			} else {
				exchange.sendResponseHeaders(403, -1)
			}
		}
	}
}
