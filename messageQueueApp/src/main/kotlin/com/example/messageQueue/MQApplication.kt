package com.example.messageQueue

import application.Application
import com.example.messageQueue.application.ApplicationBase
import com.example.messageQueue.application.ServerApplicationBase

class MQApplication: ServerApplicationBase()
{
    override fun configure() { }

    override fun onRequest(data: Any) {

    }
}