package com.example.ledbackbackremote.core

import android.util.Log

class CommandCommunicator(
    val device: DeviceCommunicator
) {
    fun brightnessDown() {
        send(byteArrayOf(
            PackageCode.REMOTE_COMMAND, PackageRemoteCommand.brightnessDown
        ))
    }

    fun brightnessUp() {
        send(byteArrayOf(
            PackageCode.REMOTE_COMMAND, PackageRemoteCommand.brightnessUp
        ))
    }

    fun prevSprite() {
        send(byteArrayOf(
            PackageCode.REMOTE_COMMAND, PackageRemoteCommand.prevSprite
        ))
    }

    fun nextSprite() {
        send(byteArrayOf(
            PackageCode.REMOTE_COMMAND, PackageRemoteCommand.nextSprite
        ))
    }

    fun displayText() {
        send(byteArrayOf(
            PackageCode.REMOTE_COMMAND, PackageRemoteCommand.displayText
        ))
    }

    fun displaySprite() {
        send(byteArrayOf(
            PackageCode.REMOTE_COMMAND, PackageRemoteCommand.displaySprite
        ))
    }

    fun customText(text: String): Boolean {
        return sendMassiveData(PackageCode.PLAIN_TEXT, 0, text.toByteArray(Charsets.US_ASCII))
    }

    private fun send(data: ByteArray): Boolean {
        return device.send(Package(data))
    }

    private fun sendMassiveData(code: Byte, subCode: Byte, data: ByteArray): Boolean {
        Log.i(LOG_TAG, "Sending massive data: $code, $subCode, ${data.contentToString()}")

        val chunkSize = PACKAGE_SIZE - 4 // code + subcode + total_chunks + i_chunk
        val maxChunksCount = 256 / chunkSize
        val maxDataSize = maxChunksCount * chunkSize

        if (data.size > maxDataSize) {
            Log.e(LOG_TAG, "Failed to send data $data as it's exceed maximum size of "
                    + "$maxDataSize bytes on ${data.size - maxDataSize} bytes")
            return false
        }

        val chunks = data.asIterable().chunked(chunkSize)
        chunks.forEachIndexed { index, chunk ->
            val data = byteArrayOf(code, subCode, chunks.count().toByte(), index.toByte()) + chunk
            if (!send(data)) return false
        }

        return true
    }
}