package com.example.messageQueue.common

import java.nio.ByteBuffer

class RequestParser {
    fun parse(request: ByteArray) {
        var offset = 0;
        while (true) {
            val start = offset
            offset += 4;

            val bytes = request.slice(start..<offset).toByteArray()
            val size = ByteBuffer.wrap(bytes).getInt()

            val dataBytes = request.slice(offset..<offset + size).toByteArray()
            val data = dataBytes.toString(Charsets.UTF_8)
        }
    }
}
