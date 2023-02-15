package com.lotte.mart.agent.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("Header")
    @Expose
    var header: Header? = null
){
    companion object {

    }
}