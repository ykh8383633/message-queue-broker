package com.example.messageQueue.handler

import com.example.messageQueue.server.context.RequestContext
import com.example.messageQueue.server.handler.RequestHandlerBase

class RequestHandler: RequestHandlerBase() {

    override suspend fun handle(requestContext: RequestContext) {
        requestContext.response("hi".toByteArray())
        disposeContext(requestContext)
    }
}