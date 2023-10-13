package com.baokiin.hackathon.data.network

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(
    private val diskIO: Executor,
    private val networkIO: Executor,
    private val mainThread: Executor
) {

    companion object {

        private lateinit var instance: AppExecutors

        fun getInstance(): AppExecutors {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = AppExecutors()
                }
                return instance
            }
        }
    }

    constructor() : this(
        diskIO = Executors.newSingleThreadExecutor(),
        networkIO = Executors.newFixedThreadPool(100),
       mainThread = MainThreadExecutor()
    )


    fun diskIO(): Executor {
        return diskIO
    }

    fun networkIO(): Executor {
        return networkIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
