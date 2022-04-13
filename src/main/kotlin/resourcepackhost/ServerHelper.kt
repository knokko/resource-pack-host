package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

fun serveResource(exchange: HttpExchange, path: String) {
    val bufferSize = 2_000
    val buffer = ByteArray(bufferSize)

    val inputStream = GetUploadFormHandler::class.java.classLoader.getResourceAsStream(path)

    if (inputStream == null) {
        println("Failed to get resource $path")
        return
    }

    exchange.sendResponseHeaders(200, 0)
    while (true) {
        val numReadBytes = inputStream.read(buffer)
        if (numReadBytes == -1) break
        exchange.responseBody.write(buffer, 0, numReadBytes)
    }

    inputStream.close()
    exchange.responseBody.flush()
}

fun serveResource(exchange: HttpExchange, path: String, substitutor: (String) -> String) {
    val rawInput = GetUploadFormHandler::class.java.classLoader.getResourceAsStream(path)

    if (rawInput == null) {
        println("Failed to get resource $path")
        return
    }

    val outputBuffer = ByteArrayOutputStream()

    val inputScanner = Scanner(rawInput)
    val outputWriter = PrintStream(outputBuffer)

    while (inputScanner.hasNextLine()) {
        outputWriter.println(substitutor(inputScanner.nextLine()))
    }

    inputScanner.close()
    outputWriter.flush()

    val outputBytes = outputBuffer.toByteArray()
    exchange.sendResponseHeaders(200, outputBytes.size.toLong())
    exchange.responseBody.write(outputBytes)
    exchange.responseBody.flush()
}

fun binaryToHexadecimal(binary: ByteArray): String {
    val hexChars = CharArray(binary.size * 2)
    for ((index, byteValue) in binary.withIndex()) {

        fun intToHexChar(value: Int): Char {
            if (value < 0) throw IllegalArgumentException("value ($value) must be non-negative")
            if (value < 10) return value.digitToChar()
            if (value < 16) return 'A' + (value - 10)
            throw IllegalArgumentException("Value ($value) must be smaller than 16")
        }

        val intValue = byteValue.toUByte().toInt()
        val leftValue = intValue / 16
        val rightValue = intValue % 16
        hexChars[2 * index] = intToHexChar(leftValue)
        hexChars[2 * index + 1] = intToHexChar(rightValue)
    }

    return String(hexChars)
}
