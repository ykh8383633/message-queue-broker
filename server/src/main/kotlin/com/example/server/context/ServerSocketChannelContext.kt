package com.example.server.context

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class ServerSocketChannelContext {
    var readBuffer: ByteArray? = null
        private set
    private var _writeBuffer: ByteArray? = null

    private var _acceptableKey: SelectionKey? = null
    private var _readableKey: SelectionKey? = null
    private var _writableKey: SelectionKey? = null

    private fun handleRead(channel: SocketChannel) {
        val byteBuffer = ByteBuffer.allocate(256);
        var buffers: ByteArray = byteArrayOf()

        while(true){
            byteBuffer.clear()
            val readBytes = channel.read(byteBuffer)
            byteBuffer.flip()

            if(readBytes > 0){
                buffers += byteBuffer.array().slice(0..<byteBuffer.limit())
            }
            else if(readBytes == 0){
                readBuffer = buffers
                break;
            }
            else {
                close()
            }
        }
    }

    private fun handleWrite(channel: SocketChannel){
        if(_writeBuffer == null) {
            _writeBuffer = byteArrayOf()
        }

        val byteBuffer = ByteBuffer.wrap(_writeBuffer)
        byteBuffer?.flip()
        channel.write(byteBuffer)
    }

    internal fun startContext(key: SelectionKey) {
        if(!key.isAcceptable){
            throw Exception("invalid key type")
        }
        _acceptableKey = key

        val server = key.channel() as ServerSocketChannel
        val channel = server.accept()
        channel.configureBlocking(false)

        channel.register(key.selector(), SelectionKey.OP_READ, this)
    }

    internal fun read(key: SelectionKey) {
        println("read channel")
        if(!key.isReadable){
            throw Exception("invalid key type")
        }
        _readableKey = key

        (key.channel() as SocketChannel)
            .apply { this.configureBlocking(false) }
            .run {handleRead(this)}
    }

    internal fun write(key: SelectionKey){
        if(!key.isWritable) {
            throw Exception("invalid key type")
        }
        _writableKey = key;

        (key.channel() as SocketChannel)
            .apply { this.configureBlocking(false) }
            .run { handleWrite(this) }
    }

    internal fun close() {
        if (_readableKey?.channel()?.isOpen == true) _readableKey?.channel()?.close()
        if(_readableKey?.channel()?.isOpen == true) _writableKey?.channel()?.close()
    }

    fun doWrite(buffer: ByteArray) {
        val key = (_readableKey ?: throw Exception("readableKey is null"))
        val channel = key.channel() as SocketChannel
        _writeBuffer = buffer
        channel.register(key.selector(), SelectionKey.OP_WRITE, this)
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