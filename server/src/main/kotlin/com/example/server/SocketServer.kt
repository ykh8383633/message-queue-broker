package com.example.server

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class SocketServer(
    private val _port: Int
) {
    private lateinit var  _selector: Selector
    private lateinit var _server: ServerSocketChannel
    private val _latch = CountDownLatch(1);

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

    fun registerHandler() {

    }

    private fun listen() = thread {
        try{
            println("start listening...")

            while(true){
                _selector.select()

                val selectedKeys = _selector.selectedKeys()
                val iterator = selectedKeys.iterator()

                while(iterator.hasNext()){
                    val key = iterator.next()

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
                        val data = String(buffer.array()).trim();
                        println(data)
                    }
                    else if(key.isWritable){}
                }
            }
        }
        catch(e: Exception){
            println(e.message)
        }
        finally {
            _latch.countDown()
        }
    }

    fun waitForShutDown() {
        _latch.await()
    }

    fun close() {
        println("shut down server...")

        if(_server.isOpen){
            _server.close()
        }

        if(_selector.isOpen){
            _selector.close()
        }
    }
}