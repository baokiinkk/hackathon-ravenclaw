package com.baokiin.hackathon.ui.main

import android.app.Application
import com.baokiin.hackathon.data.sql.BitmapDbHelper

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

    override fun onTerminate() {
        super.onTerminate()
        BitmapDbHelper(this).deleteTable()
    }
}