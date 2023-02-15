package com.lotte.mart.vncserver.service.schema

interface SchemaListener {
    //앱 종료 요청
    fun onAppFinish()
    //앱 업데이트 완료
    fun onAppUpdateFinish(result:Boolean)
    //에러
    fun onError(type:String)
}