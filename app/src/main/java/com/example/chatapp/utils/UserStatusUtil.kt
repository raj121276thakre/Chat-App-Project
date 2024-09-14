package com.example.chatapp.utils

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

object UserStatusUtil {

    fun updateUserStatus(userId: String, status: String) {
        val userRef = FirebaseUtil.allUserCollectionReference().document(userId)
        userRef.update("status", status)
            .addOnSuccessListener {
                Log.d("FirebaseUtil", "Status updated successfully: $status")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtil", "Error updating status", e)
            }
    }


    fun listenToUserStatus(userId: String, onStatusChanged: (String?) -> Unit) {
        val userRef = FirebaseUtil.allUserCollectionReference().document(userId)
        userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseUtil", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                onStatusChanged(status)
            } else {
                Log.d("FirebaseUtil", "Current data: null")
            }
        }
    }


    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

}
