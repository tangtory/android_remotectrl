package com.lotte.mart.agent.service.schema

interface SchemaListener {
    fun onCommanderUpdateFinish(result: String)
    fun onVncUpdateFinish(result: String)
    fun onAgentUpdateFinish(result: String)
    fun onAppUpdate(path:String)
    fun onAppUpdateFinish(result: String)
    fun onSetTime(time:String)
    fun onSetTimeFinish(result:String)
    fun onReboot(reboot:Boolean)
    fun onError(type:String)
}