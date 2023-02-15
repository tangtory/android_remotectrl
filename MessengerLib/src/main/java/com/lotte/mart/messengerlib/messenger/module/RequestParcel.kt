package com.lotte.mart.messengerlib.messenger.module

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class RequestParcel(val callback : Serializable, val value : String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readSerializable()!!,
        parcel.readString()!!
    )

    companion object : Parceler<RequestParcel> {

        override fun RequestParcel.write(parcel: Parcel, flags: Int) {
            parcel.writeSerializable(callback)
            parcel.writeString(value)
        }

        override fun create(parcel: Parcel): RequestParcel {
            return RequestParcel(parcel)
        }
    }
}