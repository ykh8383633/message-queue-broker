package com.example.server.event

import com.example.server.context.ServerSocketChannelContext
import com.example.server.context.context
import java.nio.channels.SelectionKey

@JvmInline
value class NioEvent(
    val key: SelectionKey
) {
    val context: ServerSocketChannelContext?
        get() {
            val att = key.attachment()

            if(att is ServerSocketChannelContext){
                return att
            }

            return null;
        }
}
