package com.lotte.mart.daemonlib.module.imagerecorder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.*
import android.util.Log
import android.view.WindowManager
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.daemonlib.callback.RecordServiceListener
import io.reactivex.functions.Function
import java.io.ByteArrayOutputStream
import java.lang.Thread.sleep
import java.nio.ByteBuffer


internal open class RecorderModule(context: Context){
    var tag : String = RecorderModule::class.java.simpleName

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var running = false
    private var width = 0
    private var height = 0
    private var dpi = 0

    private var screenHandler: ScreenHandler? = null
//    private var handlerThread: HandlerThread? = null
//    private var threadCount = 0
//    private var executorService: ExecutorService? = null
//    private var scheduler: Scheduler? = null
    private var qrate :Int = 80
    private var imgFlag: Long = 0
    private var postedImgFlag: Long = 0
    private var startTime = 0L;
    private var recordServiceListener: RecordServiceListener? = null
    var recordImage : RecorderImage = RecorderImage(null, 0)

    fun setListener(listener: RecordServiceListener?) = ExceptionHandler.tryOrDefault() {
        recordServiceListener = listener
    }

    fun removeListener() {
        recordServiceListener = null
    }

    private class ScreenHandler(looper: Looper?) : Handler(looper!!){
        override fun handleMessage(msg: Message) = ExceptionHandler.tryOrDefault() {
            super.handleMessage(msg)
        }
    }

    companion object {
    }

    init {
        running = false
        val serviceThread =
            HandlerThread("service_thread", Process.THREAD_PRIORITY_FOREGROUND)
        serviceThread.start()

//        threadCount = Runtime.getRuntime().availableProcessors()
//        Log.d(tag, "onCreate: threadCount" + threadCount)
//        executorService = Executors.newFixedThreadPool(threadCount)
//
//        val handlerThread = HandlerThread("Screen Record")
//        handlerThread.start()
//        screenHandler = ScreenHandler(handlerThread.looper)
//
//        setImageRecorder(context)
    }

    @SuppressLint("WrongConstant")
    fun setImageRecorder(context: Context) = ExceptionHandler.tryOrDefault() {
        //get the size of the window
        val mWindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        width = mWindowManager.getDefaultDisplay().getWidth() + 40;
        width = mWindowManager.defaultDisplay.width
        height = mWindowManager.defaultDisplay.height
        //height = 2300;
//        Log.i(TAG, "onCreate: w is " + width + " h is " + height);
//        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

//        scheduler = Schedulers.from(executorService!!)
    }

    fun setMediaProject(project: MediaProjection) = ExceptionHandler.tryOrDefault() {
        mediaProjection = project
        mediaProjection!!.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.d("mediaProjection", "onStop()")
                super.onStop()
                sleep(100) //setOnImageAvailableListener 종료 대기

                if (virtualDisplay != null) {
                    virtualDisplay!!.release()
                    virtualDisplay = null
                }

                if (imageReader != null) {
                    imageReader!!.surface.release()
                    imageReader!!.close()
                    imageReader = null
                }

                mediaProjection!!.unregisterCallback(this)
                mediaProjection = null
            }

        },null)
    }

    fun isRunning(): Boolean {
        return running
    }

    fun setConfig(width: Int, height: Int, dpi: Int) {
        this.width = width
        this.height = height
        this.dpi = dpi
    }

    fun setQrate(q:Int){
        qrate = q
    }

    @SuppressLint("WrongConstant")
    fun startRecord(context: Context): Boolean = ExceptionHandler.tryOrDefault(false){
        if (mediaProjection == null || running) {
            false
        }

        setImageRecorder(context)

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        createVirtualDisplayForImageReader()
        running = true
        true
    }

    fun stopRecord() = ExceptionHandler.tryOrDefault(){
        if (!running) {
            false
        }

        running = false
        if(mediaProjection == null)
            return@tryOrDefault

        synchronized(mediaProjection!!) {
            if (mediaProjection != null) {
                mediaProjection!!.stop()
            }
        }
    }

    private fun createVirtualDisplayForImageReader() = ExceptionHandler.tryOrDefault() {
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "MainScreen", width, height, dpi
            , DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, imageReader!!.surface
            , null, null
        )

        imageReader!!.setOnImageAvailableListener({ imageReader ->
            try {
                synchronized(mediaProjection!!) {
                    if(imageReader != null) {
                        if (isRunning()) {
                            val img = imageReader.acquireLatestImage()
                            if (img != null) {
                                val width = img.width
                                val height = img.height
                                val planes = img.planes
                                val buffer = planes[0].buffer
                                val pixelStride = planes[0].pixelStride
                                val rowStride = planes[0].rowStride
                                val rowPadding = rowStride - pixelStride * width
                                val imageInfo =
                                    ImageInfo(width, height, buffer, pixelStride, rowPadding)
                                val bitmap = Bitmap.createBitmap(
                                    imageInfo.width + imageInfo.rowPadding / imageInfo.pixelStride,
                                    imageInfo.height,
                                    Bitmap.Config.ARGB_8888
                                )
                                bitmap.copyPixelsFromBuffer(imageInfo.byteBuffer)

                                synchronized(recordImage) {
                                    recordImage.bitmap =
                                        compressBitmapByteArray(
                                            Bitmap.createBitmap(
                                                bitmap,
                                                0,
                                                0,
                                                width,
                                                height
                                            )
                                        )!!.clone()
                                    recordImage.index++
                                }
                                img.close()
                                buffer.clear()
                                bitmap.recycle()
                                imageInfo.byteBuffer.clear()
                            }
//                            imageReader.discardFreeBuffers()
                        } else {
                            imageReader.discardFreeBuffers()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, null)
    }

    private fun getBitmapFunction(): Function<FlagImageInfo, FlagBitmap?> {
        return Function<FlagImageInfo, FlagBitmap?> { flagImageInfo ->
            val imageInfo = flagImageInfo.imageInfo
            var bitmap = Bitmap.createBitmap(
                imageInfo.width + imageInfo.rowPadding / imageInfo.pixelStride,
                imageInfo.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(imageInfo.byteBuffer)
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            FlagBitmap(bitmap, flagImageInfo.flag)
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, qrate, stream)
        val byteArray: ByteArray = stream.toByteArray()
        stream.close()
        bitmap.recycle()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun compressBitmapByteArray(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, qrate, stream)
        val byteArray: ByteArray = stream.toByteArray()
        stream.close()
        bitmap.recycle()
        return byteArray
    }

    private class ImageInfo(
        val width: Int,
        val height: Int,
        val byteBuffer: ByteBuffer,
        val pixelStride: Int,
        val rowPadding: Int
    )

    private class FlagImageInfo(val imageInfo: ImageInfo, val flag: Long)

    class FlagBitmap(val bitmap: Bitmap, val flag: Long)
}



