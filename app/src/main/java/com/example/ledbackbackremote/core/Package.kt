package com.example.ledbackbackremote.core

const val PACKAGE_SIZE = 32
object PackageCode {
    val PING = 1
    val TEXT = 2
    val IMAGE = 3
}

class Package(
    val data: ByteArray
)