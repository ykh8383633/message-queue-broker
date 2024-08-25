package com.example.messageQueue.server.context

import com.example.server.context.ServerSocketChannelContext

class RequestContextImpl(
    override val serverContext: ServerSocketChannelContext
): RequestContext {
    override val request: Request = Request(serverContext.readBuffer!!)

    override fun response(buffer: ByteArray) {
        serverContext.doWrite(buffer)
    }
}