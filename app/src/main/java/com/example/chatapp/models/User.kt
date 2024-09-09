package com.example.chatapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp


data class User(
    val userId: String = "",
    val username: String = "",
    val phone: String = "",
    val createdTimestamp: Timestamp? = null,
    val profilePictureUrl: String = "",
    val about: String = "Hey there! I am using ChatApp.",
    val fcmToken: String = ""

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(username)
        parcel.writeString(phone)
        parcel.writeParcelable(createdTimestamp, flags)
        parcel.writeString(profilePictureUrl)
        parcel.writeString(about)
        parcel.writeString(fcmToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
