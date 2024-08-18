package com.example.messageQueue.server.context

import com.example.server.context.ServerSocketChannelContext

class RequestContextImpl(
    private val _serverContext: ServerSocketChannelContext
): RequestContext {
    override val request: Request = Request(_serverContext.readBuffer!!)

    override fun response(buffer: ByteArray) {
        _serverContext.doWrite(buffer)
    }
}