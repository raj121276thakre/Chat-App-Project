package com.example.chatapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class ChatMessageModel(
    val message: String = "",
    val timestamp: Timestamp? = null,
    val senderId: String="",
    var isImportant: Boolean = false ,// Ensure this field is present
    var isScheduled: Boolean = false // Add this property to manage scheduled status
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeParcelable(timestamp, flags)
        parcel.writeString(senderId)
        parcel.writeByte(if (isImportant) 1 else 0)
        parcel.writeByte(if (isScheduled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatMessageModel> {
        override fun createFromParcel(parcel: Parcel): ChatMessageModel {
            return ChatMessageModel(parcel)
        }

        override fun newArray(size: Int): Array<ChatMessageModel?> {
            return arrayOfNulls(size)
        }
    }
}