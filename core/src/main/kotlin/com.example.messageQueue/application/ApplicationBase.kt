package com.example.messageQueue.application

import application.Application
import java.util.concurrent.CountDownLatch

abstract class ApplicationBase: Application {

    override fun start() {
        onStart()
        Runtime.getRuntime().addShutdownHook(Thread {
            stop();
        })
        run()
    }

    override fun stop() {
        onStop();
    }

    protected open fun onStart(){
        println("start")
    }

    protected open fun onStop(){
        println("stop")
    }

    protected abstract fun run()
    protected abstract fun configure()
}