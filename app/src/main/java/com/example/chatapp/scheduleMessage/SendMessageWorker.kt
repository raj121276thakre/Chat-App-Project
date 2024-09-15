package com.example.chatapp.scheduleMessage

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chatapp.models.ChatRoomModel
import com.example.chatapp.models.User
import com.example.chatapp.notification.AccessToken
import com.example.chatapp.scheduleMessage.database.AppDatabase
import com.example.chatapp.utils.FirebaseUtil
import com.example.chatapp.utils.FirebaseUtil.currentUserDetails
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Arrays

class SendMessageWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val db: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
    }


    override suspend fun doWork(): Result {
        val chatroomId = inputData.getString("CHATROOM_ID") ?: return Result.failure()
        val otherUserId = inputData.getString("RECIPIENT_USER_ID") ?: return Result.failure()
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
                chatroomReference.add(chatMessageModel)
                    .await() // Use await() for coroutine compatibility


                // Check if the message is important
                if (message.isImportant) {
                    sendImportantMessageNotification(message.message, recipientToken)
                } else {
                    sendNotification(message.message, recipientToken)
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





    // Sends a regular notification using Firebase Cloud Messaging
    private fun sendNotification(message: String, recipientToken: String) {
        Log.d(
            "Not ..Important function",
            "$message"
        )
        currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)
                try {

                    val client = OkHttpClient()

                    // Construct the JSON payload
                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
//                                .put(
//                                    "notification", JSONObject()
//                                        .put("title", currentUser!!.username)
//                                        .put("body", message)
//                                )
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
                                    JSONObject().apply {
                                        put(
                                            "title",
                                            currentUser?.username ?: "New Message"
                                        ) // Sender's name
                                        put("body", message) // Message content
                                        put(
                                            "isImportant",
                                            "false"
                                        ) // Mark message as important (boolean value)
                                        put("userId", currentUser!!.userId)

                                    }
//                                    JSONObject()                       // Custom data payload
//                                        .put("userId", currentUser.userId)
                                )
                        )


                    val mediaType = "application/json; charset=utf-8".toMediaType()

                    //val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonPayload.toString())

                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chat-app-56add/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                                println("Response body: ${response.body?.string()}")
                                Log.i("Response body", "${response.body?.string()}")
                            } else {
                                println("Normal Notification sent successfully")
                            }
                        }
                    })


                } catch (e: Exception) {

                }
            }
        }


    }



    private fun sendImportantMessageNotification(message: String, recipientToken: String) {
        Log.d(
            "Important function",
            "$message"
        )
        currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)
                try {

                    val client = OkHttpClient()

                    // Construct the JSON payload
                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
//                                .put(
//                                    "notification", JSONObject()
//                                        .put("title", currentUser!!.username)
//                                        .put("body", message)
//                                )
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
                                    JSONObject().apply {
                                        put(
                                            "title",
                                            currentUser?.username ?: "New Message"
                                        ) // Sender's name
                                        put("body", message) // Message content
                                        put(
                                            "isImportant",
                                            "true"
                                        ) // Mark message as important (boolean value)
                                        put("userId", currentUser!!.userId)

                                    }
//                                    JSONObject()                       // Custom data payload
//                                        .put("userId", currentUser.userId)
                                )
                        )


                    val mediaType = "application/json; charset=utf-8".toMediaType()

                    //val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonPayload.toString())

                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chat-app-56add/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                                println("Response body: ${response.body?.string()}")
                                Log.i("Response body", "${response.body?.string()}")
                            } else {
                                println("Normal Notification sent successfully")
                            }
                        }
                    })


                } catch (e: Exception) {

                }
            }
        }
    }





    /*
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

        private fun sendImportantMessageNotification(message: String, recipientToken: String) {
            currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val currentUser: User? = task.result.toObject(User::class.java)
                    try {
                        val client = OkHttpClient()

                        val jsonPayload = JSONObject()
                            .put(
                                "message", JSONObject()
                                    .put("token", recipientToken)
                                    .put(
                                        "notification", JSONObject() // Add notification settings
                                            .put("title", currentUser!!.username)  // Sender's name
                                            .put("body", message)  // Message content
                                            .put("sound", "default")  // Default sound (ringtone)
                                    )
                                    .put(
                                        "data", JSONObject() // Include custom data payload
                                            .put("title", currentUser!!.username)  // Sender's name
                                            .put("body", message)  // Message content
                                            .put(
                                                "isImportant",
                                                true.toString()
                                            )  // Mark message as important
                                    )
                                    .put(
                                        "android", JSONObject()
                                            .put(
                                                "priority",
                                                "high"
                                            )  // High priority for urgent delivery
                                            .put(
                                                "notification", JSONObject()
                                                    .put(
                                                        "vibrate",
                                                        true
                                                    )  // Ensure vibration is enabled
                                                    .put(
                                                        "sound",
                                                        "default"
                                                    )  // Default sound (ringtone)
                                            )
                                    )
                            )


                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                        val request = Request.Builder()
                            .url("https://fcm.googleapis.com/v1/projects/your-project-id/messages:send")
                            .post(requestBody)
                            .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                            .addHeader("Content-Type", "application/json")
                            .build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (!response.isSuccessful) {
                                    println("Failed to send notification: ${response.code}")
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

     */

}