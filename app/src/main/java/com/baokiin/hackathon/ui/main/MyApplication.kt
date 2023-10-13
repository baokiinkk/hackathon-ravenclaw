package com.baokiin.hackathon.ui.main

import android.app.Application

class MyApplication : Application() {
    init {
        myApplication = this
    }
    companion object {
        private var myApplication: MyApplication? = null
        fun getApplication(): MyApplication {
            return myApplication as MyApplication
        }
    }
}