package com.example.messageQueue.handler

import com.example.messageQueue.server.context.RequestContext
import com.example.messageQueue.server.handler.RequestHandlerBase

class RequestParseHandler: RequestHandlerBase() {

    override suspend fun handle(context: RequestContext) {
        val req = context.request
        val reqBuff = req.buffer

    }

}