package com.example.server.context

import java.nio.ByteBuffer
import java.nio.channels.Channel
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class RequestChannelContext(
    val requestBuffer: ByteBuffer,
) {
    var doWrite: Boolean = false
        private set
    var responseBuffer: ByteBuffer? = null
        private set

    fun write(buffer: ByteBuffer){
        doWrite = true
        responseBuffer = buffer
    }
}