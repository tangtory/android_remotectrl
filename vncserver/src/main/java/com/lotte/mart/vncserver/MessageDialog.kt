package com.lotte.mart.vncserver

import android.app.Dialog
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.lotte.mart.commonlib.log.Log

//메세지 다이얼로그
class MessageDialog(context : Context) {
    private val dlg = Dialog(context)   //부모 액티비티의 context가 들어감
    private lateinit var lblDesc : TextView //메세지 내용
    private lateinit var btnOK : Button //확인버튼
    private lateinit var listener : MyDialogOKClickedListener
    val TAG = MessageDialog::class.java.simpleName

    fun start(content : String) {
        if(dlg.isShowing){
            lblDesc.text = content
            return
        }

        //다이얼로그 애니메이션 적용
        dlg.window!!.setWindowAnimations(R.style.style_expansion_dialog)
        //타이틀바 제거
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setContentView(R.layout.message_dialog)
        //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
        //dlg.setCancelable(false)

        lblDesc = dlg.findViewById(R.id.content)
        //스크롤 적용
        lblDesc.movementMethod = ScrollingMovementMethod()
        lblDesc.text = content

        btnOK = dlg.findViewById(R.id.ok)
        btnOK.setOnClickListener {
            //확인 시 다이얼로그 종료
            dlg.dismiss()
        }

        dlg.setOnDismissListener {
            //다이얼로그 종료 시 리스너 이벤트 발생
            listener.onOKClicked("OK")
        }
        Log.i(TAG, "Message Dialog show")
        dlg.show()
    }

    //다이얼로그 확인버튼 리스너(호출한 Activity에서 확인할 수 있음)
    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }
}

