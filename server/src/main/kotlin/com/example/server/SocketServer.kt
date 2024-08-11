package com.example.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    fun registerHandler() {

    }

    private fun listen() {
        _serverThread = thread {
            try{
                println("start listening...")

                while(!_serverThread!!.isInterrupted){
                    if(_selector.select(500) == 0){
                        continue;
                    }

                    val selectedKeys = _selector.selectedKeys()
                    selectedKeys.forEach { key ->
                        selectedKeys.remove(key)

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

                            CoroutineScope(Dispatchers.Default).launch {

                            }
                        }
                        else if(key.isWritable){}
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
            println("selector close")
            _selector.wakeup()
            _selector.close()
        }
    }
}