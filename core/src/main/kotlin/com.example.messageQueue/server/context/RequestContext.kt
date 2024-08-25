package com.example.messageQueue.server.context

import com.example.server.context.ServerSocketChannelContext

interface RequestContext {
    val serverContext: ServerSocketChannelContext
    val request: Request

    fun response(buffer: ByteArray)
}