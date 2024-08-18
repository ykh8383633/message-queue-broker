package com.example.messageQueue.server.context

interface RequestContext {
    val request: Request

    fun response(buffer: ByteArray)
}