package com.lotte.mart.agent
import android.app.*

class App : Application() {
    val TAG :String = "App"
    var mApp: App? = null

    companion object {
        private val TAG = App::class.java.simpleName
        var mApp: App? = null
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this.applicationContext as App?
    }
}
