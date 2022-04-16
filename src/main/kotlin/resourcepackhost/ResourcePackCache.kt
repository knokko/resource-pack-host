package resourcepackhost

import java.io.File
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

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

    private val expirationTimes = ConcurrentHashMap<String, Long>()

    init {
        /*
         * Since the expiration times are not saved, we don't know when the existing resource packs should expire. I
         * think the best to solve this, is by giving each resource pack the default expiration time (so treating them
         * like they were just requested).
         */
        val existingFiles = folder.listFiles()
        if (existingFiles != null) {
            val currentTime = currentTimeMillis()
            for (file in existingFiles) {
                if (file.name.endsWith(".zip")) {
                    expirationTimes[file.name.substring(0 until file.name.length - 4)] = currentTime + CACHE_TIME
                }
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

    fun printExpirationTimes() {
        val currentTime = currentTimeMillis()
        for ((hexString, expirationTime) in expirationTimes) {
            val timeLeft = expirationTime - currentTime
            if (timeLeft < 0) {
                println("$hexString will be removed during the next cache update")
            } else {
                val secondsLeft = timeLeft / 1000
                println("$hexString has ${secondsLeft / 60} minutes and ${secondsLeft % 60} seconds left")
            }
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
                val fileContent = Files.readAllBytes(file.toPath())
                this.expirationTimes[hexHash] = currentTimeMillis() + CACHE_TIME
                fileContent
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
