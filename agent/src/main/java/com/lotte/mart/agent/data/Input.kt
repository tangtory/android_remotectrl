package com.lotte.mart.agent.data

import com.google.gson.annotations.SerializedName

data class Input (
    @SerializedName("BusiType")
    var busiType : String? = null,
    @SerializedName("SysId")
    var sysId : String? = null,
    @SerializedName("ProcCd")
    var procCd : String? = null,
    @SerializedName("EvtCd")
    var evtCd : String? = null,
    @SerializedName("CurLoc")
    var curLoc : String? = null,
    @SerializedName("EvtGbn")
    var evtGbn : String? = null,
    @SerializedName("EvtStatGbn")
    var evtStatGbn : String? = null,

    @SerializedName("ErrLvl")
    var errLvl : String? = null,
    @SerializedName("AppErrCmt")
    var appErrCmt : String? = null,
    @SerializedName("DetCmt")
    var detCmt : String? = null,
    @SerializedName("CurDt")
    var curDt : String? = null,
    @SerializedName("CurTm")
    var curTm : String? = null

)