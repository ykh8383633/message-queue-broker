package com.example.server.event

interface Event {
    val type: EventType
}

interface  NioSocketEvent: Event {
    val operation: Int
}