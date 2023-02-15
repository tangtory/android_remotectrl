package com.lotte.mart.daemonlib.service

import android.content.Context
import android.media.projection.MediaProjection
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.daemonlib.callback.RecordServiceListener
import com.lotte.mart.daemonlib.module.imagerecorder.RecorderImage
import com.lotte.mart.daemonlib.module.imagerecorder.RecorderModule

/**
 * 화면녹화(캡쳐링) 서비스
 */
class RecordService(context: Context) {
    var tag : String = RecorderModule::class.java.simpleName
    private var recordModule: RecorderModule? = null

    init {
        recordModule = RecorderModule(context)
    }

    /**
     * 화면 녹화 서비스 가동 여부
     */
    fun isRunning(): Boolean = ExceptionHandler.tryOrDefault(false){
        recordModule!!.isRunning()
    }

    /**
     * 이미지 설정
     */
    fun setConfig(width: Int, height: Int, dpi: Int) = ExceptionHandler.tryOrDefault(){
        recordModule!!.setConfig(width, height, dpi)
    }

    /**
     * 화면 녹화 서비스 중지
     */
    fun stop() = ExceptionHandler.tryOrDefault(){
        recordModule!!.stopRecord()
    }

    /**
     * 화면 녹화 서비스 가동
     */
    fun start(context: Context): Boolean = ExceptionHandler.tryOrDefault(false){
        recordModule!!.startRecord(context)
    }

    fun setListener(listener: RecordServiceListener) = ExceptionHandler.tryOrDefault(){
        recordModule!!.setListener(listener)
    }

    /**
     * 화면 녹화 API MediaProjection 설정
     */
    fun setMediaProject(project: MediaProjection) = ExceptionHandler.tryOrDefault(){
        recordModule!!.setMediaProject(project)
    }

    /**
     * 이미지 취득
     */
    fun getRecordImage() : RecorderImage? = ExceptionHandler.tryOrDefault(null){
        recordModule!!.recordImage
    }

    /**
     * 이미지 압축률 설정(화질)
     */
    fun setQrate(q:Int) = ExceptionHandler.tryOrDefault() {
        recordModule!!.setQrate(q)
    }
}