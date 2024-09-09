package com.example.chatapp.scheduleMessage

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chatapp.scheduleMessage.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SendMessageWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val db: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
    }

    override suspend fun doWork(): Result {
        val chatroomId = inputData.getString("CHATROOM_ID") ?: return Result.failure()

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
}
