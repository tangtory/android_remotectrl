package com.lotte.mart.messengerlib.messenger.module.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Messenger

/*
* When a service is created, define Messenger class
* onBind() is Return the Binder of Messenger
* */
internal class MessengerService : Service() {
    lateinit var mMessenger: Messenger;

    override fun onCreate() {
        super.onCreate()
        mMessenger = Messenger(MessengerHandler())
    }

    override fun onBind(intent: Intent): IBinder? {
        return mMessenger!!.binder
    }
}