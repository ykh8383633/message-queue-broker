package com.example.messageQueue.handler

import com.example.messageQueue.server.context.RequestContext
import com.example.messageQueue.server.handler.RequestHandlerBase
import java.nio.charset.Charset

class RequestHandler: RequestHandlerBase() {

    override suspend fun handle(requestContext: RequestContext) {
        println(requestContext.request.buffer.toString(Charsets.UTF_8))
        requestContext.response("response".toByteArray())
    }
}