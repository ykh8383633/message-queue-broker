package com.example.messageQueue.server.handler

import com.example.messageQueue.server.context.RequestContext
import com.example.messageQueue.server.context.RequestContextImpl
import com.example.server.context.ServerSocketChannelContext
import com.example.server.handler.RequestChannelHandler

abstract class RequestHandlerBase: RequestChannelHandler {

    override suspend fun handleRequest(context: ServerSocketChannelContext) {
        val requestContext = createRequestContext(context)
        handle(requestContext)
    }

    private fun createRequestContext(context: ServerSocketChannelContext): RequestContext {
        return RequestContextImpl(context)
    }

    abstract suspend fun handle(requestContext: RequestContext)
}