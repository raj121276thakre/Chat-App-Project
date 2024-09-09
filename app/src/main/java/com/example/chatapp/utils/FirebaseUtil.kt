package com.example.chatapp.utils

import android.util.Log
import com.example.chatapp.models.ChatRoomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseUtil {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().uid
    }

    fun currentUserDetails(): DocumentReference {
        val userId = currentUserId()
        return FirebaseFirestore.getInstance().collection("users").document(userId ?: "")
    }


    fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference? {
        val currentUserId = currentUserId()

        Log.d("FirebaseUtil", "Current User ID: $currentUserId, User IDs: $userIds")

        if (currentUserId == null || userIds.size != 2) {
            Log.e("FirebaseUtil", "Invalid user IDs or current user ID is null.")
            return null
        }

        val otherUserId = if (userIds[0] == currentUserId) userIds[1] else userIds[0]

        if (otherUserId.isNullOrEmpty()) {
            Log.e("FirebaseUtil", "Invalid other user ID.")
            return null
        }

        // Return the DocumentReference for the other user
        return allUserCollectionReference().document(otherUserId)
    }

    fun allUserCollectionReference(): CollectionReference {

        return FirebaseFirestore.getInstance().collection("users")
    }

    fun allChatroomCollectionReference(): CollectionReference {

        return FirebaseFirestore.getInstance().collection("chatrooms")
    }




    fun getChatroomReference(chatroomId: String): DocumentReference {

        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
    }

    fun getChatroomId(userId1: String, userId2: String): String {
        if (userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2
        }else{
            return userId2+"_"+userId1
        }
    }

    fun getChatroomMessageReference(chatroomId: String): CollectionReference {

        return getChatroomReference(chatroomId).collection("chats")
    }





    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentProfilePicStorageRef(): StorageReference {
        return FirebaseStorage.getInstance().reference
            .child("profile_pic")
            .child(currentUserId()!!)
    }

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
        return FirebaseStorage.getInstance().reference
            .child("profile_pic")
            .child(otherUserId)
    }





    fun updateMessageStatus(chatroomId: String, messageId: String, isScheduled: Boolean) {
        val chatroomReference = getChatroomMessageReference(chatroomId)
            .document(messageId)

        chatroomReference.update("isScheduled", isScheduled)
            .addOnSuccessListener {
                // Successfully updated
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }






}
