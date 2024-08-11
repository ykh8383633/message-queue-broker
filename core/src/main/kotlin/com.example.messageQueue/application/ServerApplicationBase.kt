package com.example.messageQueue.application

import com.example.server.SocketServer

abstract class ServerApplicationBase: ApplicationBase() {
    protected lateinit var server: SocketServer

    override fun run() {
        configure()
        server = SocketServer(8080)
        server.startup()
        server.waitForShutDown()
    }

    override fun onStop() {
        super.onStop()
        server.close()
    }

    abstract fun onRequest(data: Any)
}