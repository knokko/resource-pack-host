package resourcepackhost

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

fun extractFileContent(contentPlusHeaders: ByteArray): ByteArray {
    var contentStartIndex = 0
    var hadLineBreak = false
    while (contentStartIndex < contentPlusHeaders.size - 1) {
        val currentValue = contentPlusHeaders[contentStartIndex].toInt().toChar()
        val nextValue = contentPlusHeaders[contentStartIndex + 1].toInt().toChar()
        if (currentValue == '\r' && nextValue == '\n') {
            contentStartIndex += 2

            if (hadLineBreak) {
                break
            }

            hadLineBreak = true
        } else {
            hadLineBreak = false
            contentStartIndex += 1
        }
    }

    var contentBoundIndex = contentPlusHeaders.size - 4
    while (contentBoundIndex >= contentStartIndex) {
        val currentValue = contentPlusHeaders[contentBoundIndex].toInt().toChar()
        val nextValue = contentPlusHeaders[contentBoundIndex + 1].toInt().toChar()
        if (currentValue == '\r' && nextValue == '\n') {
            break
        } else {
            contentBoundIndex -= 1
        }
    }

    return contentPlusHeaders.sliceArray(contentStartIndex until contentBoundIndex)
}

fun validateResourcePackContent(content: ByteArray): String? {
    val zipInput = ZipInputStream(ByteArrayInputStream(content))
    var hasPackMcmeta = false
    var isEmpty = true
    while (true) {
        try {
            val currentEntry = zipInput.nextEntry ?: break

            if (currentEntry.name.isNotBlank()) isEmpty = false
            if (currentEntry.name == "pack.mcmeta") hasPackMcmeta = true
        } catch (invalidZip: ZipException) {
            return "This is not a valid ZIP file."
        } catch (invalidZip: IOException) {
            return "This ZIP file ended unexpectedly"
        }
    }

    // All resource-packs must have a pack.mcmeta in their root directory, but 'encrypted' resource packs will
    // appear to be empty
    return if (hasPackMcmeta || isEmpty) null
    else "This resource pack doesn't have a pack.mcmeta file in the root directory"
}
