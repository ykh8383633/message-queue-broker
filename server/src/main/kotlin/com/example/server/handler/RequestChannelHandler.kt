package com.example.server.handler

import com.example.server.context.ServerSocketChannelContext

interface RequestChannelHandler {
    suspend fun handleRequest(context: ServerSocketChannelContext)
}