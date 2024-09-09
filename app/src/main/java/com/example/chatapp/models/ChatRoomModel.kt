package com.example.chatapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class ChatRoomModel(
    val chatRoomId: String = "",
    val userIds: List<String> = listOf(),
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String = "",
    var lastMessage: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        chatRoomId = parcel.readString() ?: "",
        userIds = parcel.createStringArrayList() ?: listOf(),
        lastMessageTimestamp = parcel.readParcelable(Timestamp::class.java.classLoader),
        lastMessageSenderId = parcel.readString() ?: "",
        lastMessage = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(chatRoomId)
        parcel.writeStringList(userIds)
        parcel.writeParcelable(lastMessageTimestamp, flags)
        parcel.writeString(lastMessageSenderId)
        parcel.writeString(lastMessage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatRoomModel> {
        override fun createFromParcel(parcel: Parcel): ChatRoomModel {
            return ChatRoomModel(parcel)
        }

        override fun newArray(size: Int): Array<ChatRoomModel?> {
            return arrayOfNulls(size)
        }
    }
}
