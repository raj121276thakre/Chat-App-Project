package com.example.chatapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.models.User

object Utils {

    fun passUserModelAsIntent(intent: Intent, user: User) {
        intent.putExtra("USER", user)
    }


    fun getUserModelFromIntent(intent: Intent): User? {
        return intent.getParcelableExtra("USER")
    }


    fun setProfilePic(context: Context, imageUri: Uri, imageView: ImageView) {
        Glide.with(context)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }

}