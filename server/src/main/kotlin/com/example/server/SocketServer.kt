package com.example.server

import com.example.server.context.ServerSocketChannelContext
import com.example.server.context.context
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
    private lateinit var  _selector: Selector
    private lateinit var _server: ServerSocketChannel
    private val _latch = CountDownLatch(1);
    private var _listener: Thread? = null
    private val _handlers: MutableList<RequestChannelHandler> = mutableListOf()



    fun startup() {
        _selector = Selector.open()
        _server = ServerSocketChannel.open().apply {
            this.bind(InetSocketAddress(_port))
            this.configureBlocking(false)
            this.register(_selector, SelectionKey.OP_ACCEPT)
        }

        // configure server

        listen();
    }

    fun registerHandler(handler: RequestChannelHandler) {
        _handlers.add(handler)
    }

    private fun listen() {
        _listener = thread {
            try{
                println("start listening...")

                while(!Thread.currentThread().isInterrupted){
                    if(_selector.select(500) == 0){
                        continue;
                    }

                    val selectedKeys = _selector.selectedKeys();

                    _selector.selectedKeys().forEach{ key ->
                        selectedKeys.remove(key)
                        val context: ServerSocketChannelContext;

                        if(key.isAcceptable){
                            context = ServerSocketChannelContext()
                                .also { it.startContext(key) }
                        }
                        else if(key.isReadable){
                            context = key.context() ?: throw Exception("context is null")
                            context.read(key)

                            CoroutineScope(Dispatchers.IO).launch {
                                _handlers.forEach{ it.handleRequest(context)}
                            }
                        }
                        else if(key.isWritable){
                            context = key.context() ?: throw Exception("context is null")
                            context.write(key)

                            if(context.isFinished){
                                context.close()
                            }
                        }
                    }
                }
            }
            catch(e: Exception){
                // todo
                throw e
            }
            finally {
                _latch.countDown()
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

        if(_selector.isOpen){
            _selector.wakeup()
            _selector.close()
        }
    }
}

