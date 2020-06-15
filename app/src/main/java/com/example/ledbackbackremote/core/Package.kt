package com.example.ledbackbackremote.core

const val PACKAGE_SIZE = 32

// 1 byte - up to 255 codes
object PackageCode {
    const val PING: Byte = 1
    const val REMOTE_COMMAND: Byte = 2
    const val PLAIN_TEXT: Byte = 3
    const val IMAGE: Byte = 4
}

// 1 byte - up to 255 commands
object PackageRemoteCommand {
    const val brightnessDown: Byte = 1
    const val brightnessUp: Byte = 2
    const val prevSprite: Byte = 3
    const val nextSprite: Byte = 4
    const val displayText: Byte = 5
    const val displaySprite: Byte = 6
}

data class Package(
    val data: ByteArray
)