package com.example.chatapp.scheduleMessage

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chatapp.models.User
import com.example.chatapp.notification.AccessToken
import com.example.chatapp.scheduleMessage.database.AppDatabase
import com.example.chatapp.utils.FirebaseUtil.currentUserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class SendMessageWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val db: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
    }

    override suspend fun doWork(): Result {
        val chatroomId = inputData.getString("CHATROOM_ID") ?: return Result.failure()
        val recipientUserId = inputData.getString("RECIPIENT_USER_ID") ?: return Result.failure()
        val recipientToken = inputData.getString("RECIPIENT_TOKEN") ?: return Result.failure()


        // Fetch the messages to send at this time
        val messagesToSend = withContext(Dispatchers.IO) {
            db.scheduledMessageDao().getMessagesToSend(System.currentTimeMillis())
        }

        // Process each message
        for (message in messagesToSend) {
            val chatMessageModel = mapOf(
                "message" to message.message,
                "timestamp" to Timestamp.now(),
                "senderId" to message.senderId,
                "isImportant" to message.isImportant,
                "isScheduled" to false
            )

            val chatroomReference = FirebaseFirestore.getInstance()
                .collection("chatrooms")
                .document(chatroomId)
                .collection("chats")

            try {
                chatroomReference.add(chatMessageModel).await() // Use await() for coroutine compatibility


                // Check if the message is important
                if (message.isImportant) {
                    sendImportantMessageNotification(message.message, recipientToken)
                } else {
                    sendNotificationAfterSaving(message.message, recipientToken)
                }

                // Delete the message from the local database after successful upload
                withContext(Dispatchers.IO) {
                    db.scheduledMessageDao().delete(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.failure()
            }
        }

        return Result.success()
    }




    private fun sendNotificationAfterSaving(message: String, recipientToken: String) {
        currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)


                try {
                    val client = OkHttpClient()
                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
                                .put(
                                    "notification", JSONObject()
                                        .put("title", currentUser!!.username)
                                        .put("body", message)
                                )
                                .put(
                                    "android", JSONObject()
                                        .put("priority", "high")
                                )
                                .put(
                                    "apns", JSONObject()
                                        .put(
                                            "headers", JSONObject()
                                                .put("apns-priority", "10")
                                        )
                                )
                                .put(
                                    "data",
                                    JSONObject()
                                        .put("userId", currentUser.userId)
                                )
                        )

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chat-app-15577/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                                println("Response body: ${response.body?.string()}")
                            } else {
                                println("Notification sent successfully")
                            }
                        }
                    })

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


//    Imp message notification not working
    private fun sendImportantMessageNotification(message: String, recipientToken: String) {
        currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)
                try {
                    val client = OkHttpClient()

                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
                                .put(
                                    "notification", JSONObject()
                                        .put("title", currentUser!!.username)
                                        .put("body", message)
                                        .put("sound", "default") // Play the default sound
                                )
                                .put(
                                    "data", JSONObject() // Include the data payload
                                        .put("isImportant", "true") // Pass isImportant as true
                                )
                                .put(
                                    "android", JSONObject()
                                        .put("priority", "high")
                                        .put(
                                            "notification", JSONObject()
                                                .put("sound", "default") // Ensure this matches the default sound setup
                                        )
                                )
                        )

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chat-app-15577/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                                println("Response body: ${response.body?.string()}")
                            } else {
                                println("Important notification sent successfully")
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }



}
