package com.example.server

import com.example.server.context.ServerSocketChannelContext
import com.example.server.context.context
import com.example.server.event.EventLoop
import com.example.server.event.EventType
import com.example.server.event.NioEvent
import com.example.server.event.NioEventLoop
import com.example.server.handler.RequestChannelHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class SocketServer(
    private val _port: Int,
) {
    private lateinit var _server: ServerSocketChannel
    private val _latch = CountDownLatch(1);
    private var _listener: Thread? = null
    private val _handlers: MutableList<RequestChannelHandler> = mutableListOf()
    private lateinit var _acceptors: EventLoop
    private lateinit var _loop: EventLoop

    fun startup() {
        _acceptors = NioEventLoop();
        _loop = NioEventLoop()
        _server = ServerSocketChannel.open().apply {
            this.bind(InetSocketAddress(_port))
            this.configureBlocking(false)
        }

        _acceptors.subscribe(_server, mutableSetOf(EventType.ACCECT))
        _acceptors.registerHandler(EventType.ACCECT) { e ->
            val context = ServerSocketChannelContext(_loop)
            context.startContext(e)
        }
        _loop.registerHandler(EventType.READ) { e ->
            val context = e.context ?: throw Exception("context is null")
            context.read(e)

            CoroutineScope(Dispatchers.IO).launch {
                _handlers.forEach{ it.handleRequest(context)}
            }
        }
        _loop.registerHandler(EventType.WRITE) { e ->
            val context = e.context ?: throw Exception("context is null")
            context.write(e)

            if(context.isFinished){
                context.close()
            }
        }

        listen();
    }

    fun registerHandler(handler: RequestChannelHandler) {
        _handlers.add(handler)
    }

    private fun listen() {
        _listener = thread {
            try{
                println("start listening...")

//                while(!Thread.currentThread().isInterrupted){
//                    if(_selector.select(500) == 0){
//                        continue;
//                    }
//
//                    val selectedKeys = _selector.selectedKeys();
//
//                    _selector.selectedKeys().forEach{ key ->
//                        selectedKeys.remove(key)
//                        val context: ServerSocketChannelContext;
//
//                        if(key.isAcceptable){
//                            context = ServerSocketChannelContext()
//                            context.startContext(key)
//                        }
//                        else if(key.isReadable){
//                            context = key.context() ?: throw Exception("context is null")
//                            context.read(key)
//
//                            CoroutineScope(Dispatchers.IO).launch {
//                                _handlers.forEach{ it.handleRequest(context)}
//                            }
//                        }
//                        else if(key.isWritable){
//                            context = key.context() ?: throw Exception("context is null")
//                            context.write(key)
//
//                            if(context.isFinished){
//                                context.close()
//                            }
//                        }
//                    }
//                }
            }
            catch(e: Exception){
                // todo
                throw e
            }
            finally {
                terminate();
            }
        }
    }

    fun waitForShutDown() {
        _latch.await()
    }

    fun close() {
        println("shut down server...")
        if(_listener?.isAlive == true){
            _listener?.interrupt()
            _listener?.join()
        }

        if(_server.isOpen){
            _server.close()
        }

//        if(_selector.isOpen){
//            _selector.wakeup()
//            _selector.close()
//        }
    }

    private fun terminate() {
        _latch.countDown();
    }
}

