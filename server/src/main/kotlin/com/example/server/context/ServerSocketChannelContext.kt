package com.example.server.context

import com.example.server.event.Event
import com.example.server.event.EventLoop
import com.example.server.event.EventType
import com.example.server.event.NioEvent
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class ServerSocketChannelContext (
    val loop: EventLoop
) {
    var attachment: Any? = null
    var isFinished = false
    var readBuffer: ByteArray? = null
        private set
    private var _writeBuffer: ByteArray? = null

    private var _acceptableKey: SelectionKey? = null
    private var _readableKey: SelectionKey? = null
    private var _writableKey: SelectionKey? = null



    private fun handleRead(channel: SocketChannel) {
        val byteBuffer = ByteBuffer.allocate(256);
        var buffer: ByteArray = byteArrayOf()

        while(true){
            byteBuffer.clear()
            val readBytes = channel.read(byteBuffer)
            byteBuffer.flip()

            if(readBytes > 0){
                var bf = byteBuffer.array()

                if(byteBuffer.hasRemaining()){
                    bf = bf.slice(0..<byteBuffer.limit()).toByteArray()
                }

                buffer += bf
            } else if(readBytes == 0){
                readBuffer = buffer
                break;
            } else {
                close()
            }
        }
    }

    private fun handleWrite(channel: SocketChannel){
        if(_writeBuffer == null) {
            _writeBuffer = byteArrayOf()
        }

        val cap = 256
        val byteBuffer = ByteBuffer.allocate(cap)

        var page = 0
        while(true){
            byteBuffer.clear()

            val offset = page * cap
            var limit = (page + 1) * cap

            if(limit > (_writeBuffer?.size ?: 0)){
                limit = _writeBuffer?.size ?: 0
            }

            byteBuffer.put(_writeBuffer?.slice(offset..<limit)?.toByteArray())
            byteBuffer?.flip()
            val writeBytes = channel.write(byteBuffer)

            if(writeBytes == 0){
                _writeBuffer = null
                break
            }

            page++
        }

    }

    internal fun startContext(e: NioEvent) {
        val key = e.key
        if(!key.isAcceptable){
            throw Exception("invalid key type")
        }
        _acceptableKey = key

        val server = key.channel() as ServerSocketChannel
        val ch = server.accept().apply { this.configureBlocking(false) }
        loop.subscribe(ch, mutableSetOf(EventType.READ), this)
    }

    internal fun read(e: NioEvent) {
        val key = e.key
        if(!key.isReadable){
            throw Exception("invalid key type")
        }
        _readableKey = key

        val socket = (key.channel() as SocketChannel).apply { this.configureBlocking(false) }
        handleRead(socket)
    }

    internal fun write(e: NioEvent){
        val key = e.key
        if(!key.isWritable) {
            throw Exception("invalid key type")
        }
        _writableKey = key;

        val ch = (key.channel() as SocketChannel).apply { this.configureBlocking(false) }
        handleWrite(ch)

        // key.interestOpsAnd(SelectionKey.OP_WRITE.inv())
        loop.unsubscribe(key, mutableSetOf(EventType.WRITE));
        loop.subscribe(key, mutableSetOf(EventType.READ))
    }

    fun close() {
        if (_readableKey?.channel()?.isOpen == true) _readableKey?.channel()?.close()
        if(_readableKey?.channel()?.isOpen == true) _writableKey?.channel()?.close()
    }

    fun doWrite(buffer: ByteArray) {
        val key = (_readableKey ?: throw Exception("readableKey is null"))
        val ch = key.channel() as SocketChannel
        _writeBuffer = buffer
        loop.subscribe(key, mutableSetOf(EventType.WRITE))
    }

    fun doClose() {
        isFinished = true
    }

    fun getStringData(): String {
        if(this.readBuffer != null){
            return String(readBuffer!!).trim()
        }
        return ""
    }
}

fun SelectionKey.context(): ServerSocketChannelContext?{
    val att = this.attachment()

    if(att is ServerSocketChannelContext){
        return att
    }

    return null;
}