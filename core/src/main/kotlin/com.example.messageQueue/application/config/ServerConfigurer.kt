package com.example.messageQueue.application.config

import com.example.messageQueue.server.handler.RequestHandlerBase
import com.example.server.handler.RequestChannelHandler

class ServerConfigurer {
    var requestHandler: RequestHandlerBase? = null
    var port: Int = 8080

    fun registerRequestHandler(handler: RequestHandlerBase){
        this.requestHandler = handler
    }

}