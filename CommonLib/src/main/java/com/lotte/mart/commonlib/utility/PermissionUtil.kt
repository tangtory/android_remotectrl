package com.lotte.mart.commonlib.utility

import android.app.Activity
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable

/**
 * 권한 처리 유틸
 */
class PermissionUtil {
    interface PermissionsListener {
        fun onGranted()
        fun onDenied()
    }

    companion object {
        val TAG = PermissionUtil::class.java.simpleName

        fun requestPermission(
            activity: Activity,
            listener: PermissionsListener,
            vararg permissions: String
        ) {
            val rxPermissions = RxPermissions(activity!!)
            rxPermissions.request(*permissions)
                .subscribe(object : io.reactivex.Observer<Boolean?> {
                    override fun onSubscribe(d: Disposable) {
                        Log.d("PERMISSION", "onSubscribe")
                    }
                    override fun onNext(aBoolean: Boolean) {
                        if (aBoolean) {
                            listener.onGranted()
                        } else {
                            listener.onDenied()
                        }
                        Log.d("PERMISSION", "onNext")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("PERMISSION", "onError")
                    }
                    override fun onComplete() {
                        Log.d("PERMISSION", "onComplete")
                    }
                })
        }
    }
}