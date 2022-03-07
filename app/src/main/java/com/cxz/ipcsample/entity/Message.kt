package com.cxz.ipcsample.entity

import android.os.Parcel
import android.os.Parcelable

data class Message(

    var content: String? = null,

    var isSendSuccess: Boolean = false

) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    fun readFromParcel(parcel: Parcel) {
        this.content = parcel.readString()
        this.isSendSuccess = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(content)
        parcel.writeByte(if (isSendSuccess) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}
