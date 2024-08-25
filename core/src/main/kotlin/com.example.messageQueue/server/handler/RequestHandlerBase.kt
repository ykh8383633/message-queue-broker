package com.example.messageQueue.server.handler

import com.example.messageQueue.server.context.RequestContext
import com.example.messageQueue.server.context.RequestContextImpl
import com.example.server.context.ServerSocketChannelContext
import com.example.server.handler.RequestChannelHandler
import java.util.concurrent.ConcurrentHashMap

abstract class RequestHandlerBase: RequestChannelHandler {
    protected val requestContextMap: ConcurrentHashMap<Int, RequestContext> = ConcurrentHashMap()

    override suspend fun handleRequest(context: ServerSocketChannelContext) {
        val requestContext = createRequestContext(context)
        handle(requestContext)
    }

    private fun createRequestContext(context: ServerSocketChannelContext): RequestContext {
        return requestContextMap.getOrPut(context.hashCode()){ RequestContextImpl(context) }
    }

    protected fun disposeContext(requestContext: RequestContext) {
        requestContextMap.remove(requestContext.serverContext.hashCode())
    }

    abstract suspend fun handle(requestContext: RequestContext)
}