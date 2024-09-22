package com.example.messageQueue.server.handler

import com.example.messageQueue.server.context.RequestContext
import com.example.messageQueue.server.context.RequestContextImpl
import com.example.server.context.ServerSocketChannelContext
import com.example.server.handler.RequestChannelHandler
import java.util.concurrent.ConcurrentHashMap

abstract class RequestHandlerBase: RequestChannelHandler {

    override suspend fun handleRequest(context: ServerSocketChannelContext) {
        if(context.attachment == null){
            context.attachment = RequestContextImpl(context)
        }

        val requestContext = context.attachment as RequestContext
        handle(requestContext)
        context.attachment = null
    }

    abstract suspend fun handle(requestContext: RequestContext)
}