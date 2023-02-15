package com.lotte.mart.vncserver

import android.app.*

class App : Application() {
    var mApp: App? = null

    companion object {
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this.applicationContext as App?
    }
}
