package com.example.messageQueue.application

import com.example.server.SocketServer
import com.example.server.context.RequestChannelContext
import com.example.server.handler.RequestChannelHandler

abstract class ServerApplicationBase: ApplicationBase() {
    protected lateinit var server: SocketServer

    override fun run() {
        configure()
        server = SocketServer(8080, RequestHandler())
        server.startup()
        server.waitForShutDown()
    }

    override fun onStop() {
        super.onStop()
        server.close()
    }

    abstract fun onRequest(data: Any)
}

class RequestHandler : RequestChannelHandler {
    override suspend fun handleRequest(context: RequestChannelContext) {
        val data = String(context.requestBuffer.array()).trim()
        println(data)
    }

}