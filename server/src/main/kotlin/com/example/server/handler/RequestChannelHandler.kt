package com.example.server.handler

import com.example.server.context.RequestChannelContext

interface RequestChannelHandler {
    suspend fun handleRequest(context: RequestChannelContext)
}