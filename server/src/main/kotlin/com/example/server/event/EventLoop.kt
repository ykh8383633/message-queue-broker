package com.example.server.event

import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey

interface EventLoop {
    fun subscribe(channel: SelectableChannel, eventTypes: MutableSet<EventType>)
    fun subscribe(channel: SelectableChannel, eventTypes: MutableSet<EventType>, attachment: Any?)
    fun subscribe(key: SelectionKey, eventTypes: MutableSet<EventType>)
    fun unsubscribe(key: SelectionKey, eventTypes: MutableSet<EventType>)
    fun registerHandler(eventType: EventType, handler: (e: NioEvent) -> Unit)
}