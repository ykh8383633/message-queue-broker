package com.example.messageQueue.application

import com.example.messageQueue.application.config.ServerConfigurer
import com.example.server.SocketServer
import com.example.server.context.ServerSocketChannelContext
import com.example.server.handler.RequestChannelHandler
import java.nio.ByteBuffer

abstract class ServerApplicationBase: ApplicationBase() {
    protected lateinit var server: SocketServer
    private val configurer: ServerConfigurer = ServerConfigurer()

    override fun run() {
        configure()
        configureServer(configurer)

        if(configurer.requestHandler == null){
            throw Exception("request handler is not registered")
        }

        server = SocketServer(8080, configurer.requestHandler!!)
        server.startup()
        server.waitForShutDown()
    }

    override fun onStop() {
        super.onStop()
        server.close()
    }

    abstract fun configureServer(configurer: ServerConfigurer)
}

class RequestHandler : RequestChannelHandler {
    override suspend fun handleRequest(context: ServerSocketChannelContext) {
        val data = context.getStringData()
        println(data)
        val buffer = "this is response".toByteArray()
        context.doWrite(buffer)
        context.doClose()
    }

}