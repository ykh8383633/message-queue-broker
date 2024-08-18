package com.example.messageQueue

import application.Application
import com.example.messageQueue.application.ApplicationBase
import com.example.messageQueue.application.ServerApplicationBase
import com.example.messageQueue.application.config.ServerConfigurer
import com.example.messageQueue.handler.RequestHandler

class MQApplication: ServerApplicationBase()
{
    override fun configure() { }
    override fun configureServer(configurer: ServerConfigurer) {
        configurer.registerRequestHandler(RequestHandler())
    }

}