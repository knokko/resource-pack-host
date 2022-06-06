package resourcepackhost

import com.sun.net.httpserver.HttpExchange
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.util.*

fun performExchange(exchange: HttpExchange, handler: (HttpExchange) -> Unit) {
    try {
        handler(exchange)
    } catch (ioTrouble: IOException) {
        println("An IOException was thrown: ${ioTrouble.message}")
    } finally {
        exchange.close()
        System.gc()
    }
}

fun discardInput(input: InputStream) {
    val discardBuffer = ByteArray(10_000)
    while (input.read(discardBuffer) != -1) {
        // This loop doesn't need a body
    }
    input.close()
}

fun serveResource(exchange: HttpExchange, path: String, responseCode: Int) {
    val bufferSize = 2_000
    val buffer = ByteArray(bufferSize)

    val inputStream = GetUploadFormHandler::class.java.classLoader.getResourceAsStream(path)

    if (inputStream == null) {
        println("Failed to get resource $path")
        return
    }

    exchange.sendResponseHeaders(responseCode, 0)
    while (true) {
        val numReadBytes = inputStream.read(buffer)
        if (numReadBytes == -1) break
        exchange.responseBody.write(buffer, 0, numReadBytes)
    }

    inputStream.close()
    exchange.responseBody.flush()
}

fun serveResource(exchange: HttpExchange, path: String, responseCode: Int, substitutor: (String) -> String) {
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
    exchange.sendResponseHeaders(responseCode, outputBytes.size.toLong())
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
