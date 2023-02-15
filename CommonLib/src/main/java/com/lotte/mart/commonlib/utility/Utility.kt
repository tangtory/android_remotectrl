/**
 * 시스템명 : 안드로이드 PDA 도입
 * 프로세스명 : 공통 함수 util
 * Copyright ⓒ 2020 Lotte Department. All rights reserved.
 */
package com.lotte.mart.commonlib.utility

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.TypedValue
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.MainThread
import java.io.File
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executor

object Utility {

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }

    /**
     * dp 를 px로 변환하여 계산한다.
     *
     * @param context
     * @param dp
     * @return int
     */
    fun toPixel(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    /**
     * 상단 상태표시줄의 높이를 계산
     *
     * @param context
     * @return
     */
    fun getStatusBarHeight(context: Context): Int {
        val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        var height = 0
        if (id > 0) {
            height = context.resources.getDimensionPixelSize(id)
        }
        return height
    }

    /**
     * 하드웨어 네트워크 연결유무를 체크한다.
     * @param context
     * @return true : 네트워크 연결됨, falst : 네트워크 연결 안됨
     */
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    fun getCurrentDay():String {
        var formatted: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            formatted =  current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("yyyyMMdd")
            formatted = formatter.format(date)
        }
        return formatted
    }

    fun getCurrentTime():String {
        var formatted: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HHmmss")
            formatted =  current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("HHmmss")
            formatted = formatter.format(date)
        }
        return formatted;
    }

    fun getCurrentDayTime():String {
        var formatted: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            formatted =  current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("yyyyMMddHHmmss")
            formatted = formatter.format(date)
        }
        return formatted;
    }


    /**
     * 데이터가 숫자인지 확인
     */
    fun checkNumber(str: String?): String? {
        return if (null == str) {
            "0"
        } else {
            try {
                //long l = Long.parseLong(str);
                for (i in 0 until str.length) {
                    val c = str[i]
                    if (c >= '0' && c <= '9') {
                    } else {
                        return "0"
                    }
                }
                str
            } catch (e: NumberFormatException) {
                "0"
            }
        }
    }

    /**
     * 날짜 비교
     */
    fun diffOfToday(date: String): Long {
        var diffDays: Long = 0
        try {
            var formatter: SimpleDateFormat? = null
            var receivedDate = ""
            if (date.contains("-") == true) {
                formatter = SimpleDateFormat("yyyy-MM-dd")
                receivedDate = date.substring(0, 10)
            } else {
                formatter = SimpleDateFormat("yyyyMMdd")
                receivedDate = date.substring(0, 8)
            }
            val todate = formatter.format(Date())
            var todate_date: Date? = null
            var received_date: Date? = null
            try {
                todate_date = formatter.parse(todate)
                received_date = formatter.parse(receivedDate)
            } catch (e: Exception) {
            }
            val diff = todate_date!!.time - received_date!!.time
            // 일 차이
            diffDays = diff / (24 * 60 * 60 * 1000)
        } catch (e: Exception) {
        }
        return diffDays
    }

    /*
     * 금일 년월일시분초를 받아온다
     */
    fun getTimeInSecond2(): String? {
        val mSimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA)
        val currentTime = Date()
        return mSimpleDateFormat.format(currentTime)
    }

    fun diffOfCurrTime(startTime : Long, diff: Int): Boolean{
        val curTime = System.currentTimeMillis()
        if(startTime > curTime)
            return true //시작시간이 현재시간 이후면 시간 경과한것으로 표시 //비교값이 음수로 나오기 때문에

        return (curTime - startTime) >  diff
    }

    private var sGlobalExecutor: Executor? = null

    @MainThread
    fun <T> executeTask(task: AsyncTask<T, *, *>?, vararg params: T) {
        task?.executeOnExecutor(getGlobalExecutor(), *params)
    }

    fun getGlobalExecutor(): Executor? {
        if (sGlobalExecutor == null) {
            sGlobalExecutor = AsyncTask.THREAD_POOL_EXECUTOR
        }
        return sGlobalExecutor
    }

    //현재 앱 버전 정보가져오기
    fun getVersionInfo(packageName:String, context: Context) : String {
        try {
            val info: PackageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val version = info.versionName
            return version
        } catch (e : Exception){
            return ""
        }
    }

    fun getInstallPackage(packagename:String, context: Context):Boolean{
        return try {
            val pm = context.packageManager as PackageManager
            val pi = pm.getPackageInfo(packagename.trim(), PackageManager.GET_META_DATA)
            val appInfo = pi.applicationInfo as ApplicationInfo
            appInfo.enabled
        } catch (e:Exception) {
            false
        }
    }

    /**
     * TextView string변경상태 체크 해서 3자리 마다 콤마 추가
     * @param TextView
     */
    fun setTextViewMakeStringComma(view: TextView){
        view.addTextChangedListener(object : TextWatcher {
            var strAmount = ""
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != strAmount) {
                    strAmount = makeStringComma(s.toString().replace(",", ""))
                    view.text = strAmount
                    val e = view.text as Editable
                    Selection.setSelection(e, strAmount.length)
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    /**
     * TextView string변경상태 체크 해서 3자리 마다 콤마 추가
     * @param EditTextView
     */
    fun setEditTextViewMakeStringComma(view: EditText){
        view.addTextChangedListener(object : TextWatcher {
            var strAmount2 = ""
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                if (s.toString() != strAmount2) {
                    strAmount2 = makeStringComma(s.toString().replace(",", ""))
                    view.setText(strAmount2)
                    val e = view.text as Editable
                    Selection.setSelection(e, strAmount2.length)
                }
                }catch (e:java.lang.Exception){

                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }



    fun makeStringComma(str: String): String {
        if (str.length == 0) return ""
        val value = str.toLong()
        val format = DecimalFormat("###,###")
        return format.format(value)
    }

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("SFTP_SERVER", Context.MODE_PRIVATE)
    }

    fun getLocalIPAddress(): String {
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            for (ipAddr in networkInterface.inetAddresses) {
                if (!ipAddr.isLoopbackAddress && !ipAddr.isLinkLocalAddress) {
                    return ipAddr.hostAddress
                }
            }
        }
        return ""
    }

    fun showAlertDialog(context: Context, message: String) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Alert")
            .setMessage(message)
            .setPositiveButton("Ok", null)
            .create()
        dialog.show()
    }

    fun showYesNoDialog(context: Context, title: String, message: String, cb: DialogInterface.OnClickListener) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes", cb)
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    fun getSDCardVolume(context: Context): StorageVolume? {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        for (volume in sm.storageVolumes) {
            if (volume.isRemovable) {
                return volume
            }
        }
        return null
    }

    fun getPathUUID(context: Context, path: String): String {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val volume = sm.getStorageVolume(File(path))
        return if (volume != null && volume.uuid != null) volume.uuid as String else ""
    }

    fun getInternalStoragePath(context: Context): String? {
        val dirs = context.getExternalFilesDirs(null)
        if (dirs.isNotEmpty() && Environment.getExternalStorageState(dirs[0]) == Environment.MEDIA_MOUNTED) {
            val i = dirs[0].absolutePath.indexOf("/Android/data")
            return dirs[0].absolutePath.substring(0, i)
        }
        return null
    }

    fun getSDCardPath(context: Context): String? {
        val dirs = context.getExternalFilesDirs(null)
        if (dirs.size > 1 && Environment.getExternalStorageState(dirs[1]) == Environment.MEDIA_MOUNTED) {
            val i = dirs[1].absolutePath.indexOf("/Android/data")
            return dirs[1].absolutePath.substring(0, i)
        }
        return null
    }
}
