package com.lotte.mart.daemonlib.callback

interface RecordServiceListener {
    fun onRecorderStatusChanged(isRunning: Boolean)
}