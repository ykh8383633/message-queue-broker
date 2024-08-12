package com.example.server

import com.example.server.context.RequestChannelContext
import com.example.server.handler.RequestChannelHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Channel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class SocketServer(
    private val _port: Int,
    private var handler: RequestChannelHandler
) {
    private lateinit var  _selector: Selector
    private lateinit var _server: ServerSocketChannel
    private val _latch = CountDownLatch(1);
    private var _serverThread: Thread? = null



    fun startup() {
        _selector = Selector.open()
        _server = ServerSocketChannel.open().let { socket ->
            socket.bind(InetSocketAddress(_port))
            socket.configureBlocking(false)
            socket.register(_selector, SelectionKey.OP_ACCEPT)
            return@let socket
        }

        // configure server

        listen();
    }

    private fun listen() {
        _serverThread = thread {
            try{
                println("start listening...")

                while(!Thread.currentThread().isInterrupted){
                    if(_selector.select(500) == 0){
                        continue;
                    }

                    _selector.selectedKeys().apply {
                        forEach{ key ->
                            this.remove(key)

                            if(key.isAcceptable){
                                val server = key.channel() as ServerSocketChannel
                                val client = server.accept()
                                client.configureBlocking(false)
                                client.register(_selector, SelectionKey.OP_READ)
                            }
                            else if(key.isReadable){
                                val channel = key.channel() as SocketChannel
                                val buffer = ByteBuffer.allocate(256)

                                channel.read(buffer)
                                val context = RequestChannelContext(buffer)

                                CoroutineScope(Dispatchers.Default).launch {
                                    handler.handleRequest(context)
                                    if(!context.doWrite){
                                        channel.close(buffer)
                                        return@launch
                                    }
                                    channel.register(_selector, SelectionKey.OP_WRITE, context.responseBuffer)
                                }
                            }
                            else if(key.isWritable){
                                val channel = key.channel() as SocketChannel
                                val buffer = key.attachment() as ByteBuffer

                                channel.write(buffer)
                                channel.close(buffer)
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
        if(_serverThread?.isAlive == true){
            _serverThread?.interrupt()
            _serverThread?.join()
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

fun Channel.close(byteBuffer: ByteBuffer?){
    if(byteBuffer?.hasRemaining() == false){
        byteBuffer.clear()
    }
    this.close()
}