package com.example.messageQueue.application

import com.example.messageQueue.application.config.ServerConfigurer
import com.example.server.SocketServer
import com.example.server.context.ServerSocketChannelContext
import com.example.server.handler.RequestChannelHandler
import java.nio.ByteBuffer

abstract class ServerApplicationBase: ApplicationBase() {
    protected lateinit var server: SocketServer
    private val serverConfigurer: ServerConfigurer = ServerConfigurer()

    override fun run() {
        configure()
        configureServer(serverConfigurer)

        SocketServer(8080).apply {
            serverConfigurer.requestHandlers.forEach{ server.registerPipeline(it) }
            server.startup()
            server.waitForShutDown()
        }
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