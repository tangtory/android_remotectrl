package com.lotte.mart.vncserver

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lotte.mart.commonlib.log.Log
import kotlinx.android.synthetic.main.activity_message.*

//메시지 처리 및 화면 잠금 처리
class MessageActivity : AppCompatActivity() {
    val TAG = MessageActivity::class.java.simpleName
    companion object{
        var dlg : MessageDialog? = null //메세지 다이얼로그
        var bLock : Boolean? = null  //화면 잠금 여부
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        Log.i(TAG, "onCreate has Type, ${intent.hasExtra("TYPE")}")
        if (!intent.hasExtra("TYPE")) {
            finish()
            return
        }

        overridePendingTransition(0, 0)

        //스크린 잠금 확인
        bLock = intent.getBooleanExtra("LOCK", false)
        Log.i(TAG, "onCreate has MSG, ${intent.hasExtra("MSG")}")
        Log.i(TAG, "onCreate LOCK, $bLock")

        //메세지 처리
        if(intent.hasExtra("MSG")) {
            dlg = MessageDialog(this)
            dlg!!.setOnOKClickedListener {
                //메시지 닫을 때 화면 잠김 해제일 경우 해당 Activity 종료
                if(bLock == false)
                    finish()
            }
            //다이얼로그 시작
            dlg!!.start(intent.getStringExtra("MSG")!!)
        }

        //화면 잠금 처리
        if(bLock == true){
            //화면 잠금일 경우
            textView.visibility = View.VISIBLE
        } else {
            //화면 잠금 해제일 경우
            Log.i(TAG, "onCreate has UNLOCK, ${intent.hasExtra("UNLOCK")}")
            textView.visibility = View.INVISIBLE
            if(intent.hasExtra("UNLOCK")) {
                finish()
            }
        }
    }

    //뒤로가기 키 막기
    override fun onBackPressed () {
        return
    }
}
