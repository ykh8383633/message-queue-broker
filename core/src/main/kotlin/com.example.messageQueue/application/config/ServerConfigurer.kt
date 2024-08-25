package com.example.messageQueue.application.config

import com.example.messageQueue.server.handler.RequestHandlerBase
import com.example.server.handler.RequestChannelHandler

class ServerConfigurer {
    var requestHandlers: MutableList<RequestHandlerBase> = mutableListOf()
    var port: Int = 8080

    fun registerRequestHandler(handler: RequestHandlerBase){
        this.requestHandlers.add(handler)
    }

}