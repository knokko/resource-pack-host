package resourcepackhost

import java.io.ByteArrayOutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

fun main() {
    val testOutput = ByteArrayOutputStream()
    val digestStream = DigestOutputStream(testOutput, MessageDigest.getInstance("SHA-512"))
    digestStream.on(true)
    digestStream.write(byteArrayOf(
        1, 2, 3, 4
    ))
    digestStream.flush()

    println(digestStream.messageDigest.digest().contentToString())
    println(digestStream.messageDigest.digestLength)
    println(testOutput.toByteArray().contentToString())
}