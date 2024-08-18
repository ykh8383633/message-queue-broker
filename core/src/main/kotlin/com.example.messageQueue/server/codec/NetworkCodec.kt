package com.example.messageQueue.server.codec

interface NetworkCodec {
    fun encode(data: String): ByteArray
    fun <T> encode(data: T): ByteArray
    fun decode(data: ByteArray): String
    fun <T> decodeTo(data: ByteArray): T
}