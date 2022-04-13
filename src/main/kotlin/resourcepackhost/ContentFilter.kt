package resourcepackhost

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

fun extractFileContent(contentPlusHeaders: ByteArray): ByteArray {
    var contentIndex = 0
    var hadLineBreak = false
    while (contentIndex < contentPlusHeaders.size - 1) {
        val currentValue = contentPlusHeaders[contentIndex].toInt().toChar()
        val nextValue = contentPlusHeaders[contentIndex + 1].toInt().toChar()
        if (currentValue == '\r' || currentValue == '\n') {
            contentIndex += if ((currentValue == '\n' && nextValue == '\r') || (currentValue == '\r' && nextValue == '\n')) {
                2
            } else {
                1
            }

            if (hadLineBreak) {
                break
            }

            hadLineBreak = true
        } else {
            hadLineBreak = false
            contentIndex += 1
        }
    }

    return contentPlusHeaders.sliceArray(contentIndex until contentPlusHeaders.size)
}

fun validateResourcePackContent(content: ByteArray): String? {
    val zipInput = ZipInputStream(ByteArrayInputStream(content))
    while (true) {
        try {
            val currentEntry = zipInput.nextEntry ?: break

            println("Current path is ${currentEntry.name}")
        } catch (invalidZip: ZipException) {
            return "This is not a valid ZIP file."
        } catch (invalidZip: IOException) {
            return "This ZIP file ended unexpectedly"
        }
    }

    return null
}
