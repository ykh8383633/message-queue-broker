package com.example.server.event

import com.example.server.context.ServerSocketChannelContext
import com.example.server.context.context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.channels.Channel
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.thread

class NioEventLoop(): EventLoop {
    private val _selector: Selector = Selector.open()
    private val _handlers = EventHandlerCollection()

    init {
        thread {
            try{
                while(!Thread.currentThread().isInterrupted){
                    if(_selector.select(500) == 0){
                        continue;
                    }

                    val selectedKeys = _selector.selectedKeys();

                    _selector.selectedKeys().forEach{ key ->
                        selectedKeys.remove(key)

                        val handlers = if(key.isAcceptable){
                            _handlers.getHandlers(EventType.ACCECT)
                        } else if(key.isReadable){
                            _handlers.getHandlers(EventType.READ)
                        } else if(key.isWritable){
                            _handlers.getHandlers(EventType.WRITE)
                        } else {
                            mutableListOf()
                        }

                        val e = NioEvent(key)
                        CoroutineScope(Dispatchers.IO).launch {
                            handlers.forEach{ it(e) }
                        }
                    }
                }
            }
            catch(e: Exception){
                // todo
                throw e
            }
            finally {
            }
        }
    }

    override fun subscribe(channel: SelectableChannel, eventTypes: MutableSet<EventType>) {
        this.subscribe(channel, eventTypes, null)
    }

    override fun subscribe(channel: SelectableChannel, eventTypes: MutableSet<EventType>, attachment: Any?) {
        var ops = 0;

        eventTypes.forEach{
            ops = ops or nioOpsOf(it)
        }

        channel.register(this._selector, ops, attachment)
    }

    override fun subscribe(key: SelectionKey, eventTypes: MutableSet<EventType>) {
        var ops = 0;

        eventTypes.forEach{
            ops = ops or nioOpsOf(it)
        }
        key.interestOps(ops);
    }

    override fun unsubscribe(key: SelectionKey, eventTypes: MutableSet<EventType>) {
        var ops = key.interestOps();

        eventTypes.forEach{
            ops = ops and nioOpsOf(it).inv();
        }
        key.interestOps(ops);
    }

    override fun registerHandler(eventType: EventType, handler: (e: NioEvent) -> Unit) {
        _handlers.register(eventType, handler)
    }

    private fun nioOpsOf(event: EventType): Int = when(event) {
        EventType.ACCECT -> SelectionKey.OP_ACCEPT
        EventType.READ -> SelectionKey.OP_READ
        EventType.WRITE -> SelectionKey.OP_WRITE
    }


    inner class EventHandlerCollection {
        private val _lock = ReentrantReadWriteLock();
        private val _handlerMap = mutableMapOf<EventType, MutableList<(e: NioEvent) -> Unit>>()

        fun register(event: EventType, handler: (e: NioEvent) -> Unit) {
            val wLock = _lock.writeLock()

            wLock.lock()
            val handlers = this._handlerMap.getOrPut(event) { mutableListOf() }
            handlers.add(handler);
            wLock.unlock();
        }

        fun getHandlers(event: EventType): MutableList<(e: NioEvent) -> Unit> {
            val rLock = _lock.readLock();

            rLock.lock()
            val result = _handlerMap.getOrElse(event) { mutableListOf() }
            rLock.unlock()

            return result
        }
    }

}