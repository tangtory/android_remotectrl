package com.lotte.mart.commander.schema

interface SchemaListener {
    fun onCmd(cmd:String, host:String)
    fun onSetTime(time:String, host:String)
    fun onError(type:String)
}