package com.lotte.mart.agent.data

data class Header(
    val domain : String = "RcvAmsLog",
    val func : String = "PosAmsLog",
    val saleDt : String,
    val strCd : String,
    val posNo : String,
    val tranNo : String,
    val seq : String,
    val respCd : String
)