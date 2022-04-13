package resourcepackhost

import java.io.File
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.nio.file.Files
import java.security.MessageDigest

/*
 * To prevent old resource packs from consuming my disk space, I
 * delete every resource pack that has not been refreshed for at
 * least an hour.
 *
 * The plug-in is programmed to send periodic refresh messages
 * while the server is running. This should ensure that no
 * resource pack will be deleted while the corresponding server
 * is still running.
 *
 * Also, the resource pack plug-in will download a copy of the
 * resource pack and store it in the server file system. This
 * ensures that the plug-in can always upload the resource pack
 * again.
 */
private const val CACHE_TIME = 1000L * 60L * 60L

class ResourcePackCache(private val folder: File) {

    private val expirationTimes = mutableMapOf<String, Long>()

    init {
        /*
         * All previous resource packs are assumed to be expired
         * when this server starts. This is convenient because I
         * don't know when they should expire. If they are still
         * used, the resource pack plug-ins will ensure that they
         * are uploaded again.
         */
        val existingFiles = folder.listFiles()
        if (existingFiles != null) {
            for (file in existingFiles) {
                file.delete()
            }
        }
    }

    private fun getFile(hexHash: String) = File(folder.path + "/" + hexHash + ".zip")

    fun update() {
        val currentTime = currentTimeMillis()
        val deletedFiles = mutableListOf<String>()
        for ((hexString, expirationTime) in expirationTimes) {
            if (currentTime > expirationTime) {
                getFile(hexString).delete()
                deletedFiles.add(hexString)
            }
        }

        for (deletedFile in deletedFiles) {
            expirationTimes.remove(deletedFile)
        }
    }

    fun putInCache(fileContent: ByteArray): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        sha256.update(fileContent)

        val rawHash = sha256.digest()
        val hexHash = binaryToHexadecimal(rawHash)

        val file = File(folder.path + "/" + hexHash + ".zip")
        if (!file.exists()) {
            Files.write(file.toPath(), fileContent)
        }
        this.expirationTimes[hexHash] = currentTimeMillis() + CACHE_TIME

        return hexHash
    }

    fun getFromCache(hexHash: String): ByteArray? {
        return if (this.expirationTimes.containsKey(hexHash)) {
            val file = File(folder.path + "/" + hexHash + ".zip")
            try {
                Files.readAllBytes(file.toPath())
            } catch (ioTrouble: IOException) {
                this.expirationTimes.remove(hexHash)
                file.delete()
                null
            }
        } else {
            null
        }
    }
}
