package com.example.chatapp.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatapp.R
import com.example.chatapp.activities.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessaging : FirebaseMessagingService() {

    private val channelID = "important_message_channel"
    private val channelName = "Important Message Channel"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.data.isNotEmpty()) {

            val title = message.data["title"] ?: "New Message"
            val body = message.data["body"] ?: "You have received a new message"
           // val isImportant = message.data["isImportant"]?.toBoolean() ?: false
            val isImportant = message.data["isImportant"]?.toString()
            val userId = message.data["userId"]?.toString()

            // Log data for debugging
            Log.d(
                "FirebaseMessaging",
                "Received notification with title: $title, body: $body, isImportant: $isImportant"
            )

            if (title == null || body == null) {
                Log.e("FirebaseMessaging", "Notification title or body is missing.")
                return
            }


                showNotification(title, body, isImportant!!,userId!!)


//            if (isImportant == "false") {
//                Log.d(
//                    "FirebaseMessaging",
//                    "service intent runened ......."
//                )
//                val serviceIntent = Intent(this, ImportantMessageService::class.java)
//                startForegroundService(serviceIntent)
//            }


        } else {
            Log.e("FirebaseMessaging", "Received empty message data.")
        }
    }

    private fun showNotification(title: String, body: String, isImportant: String, userId: String) {
        // Define channel IDs for important and non-important messages
        val importantChannelID = "important_channel"
        val nonImportantChannelID = "non_important_channel"

        // Check the Android version to create notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create the channel for important messages
            val importantChannel = NotificationChannel(
                importantChannelID,
                "Important Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for important chat notifications"
                enableVibration(true)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT) // Play ringtone for important messages
            }

            // Create the channel for non-important messages
            val nonImportantChannel = NotificationChannel(
                nonImportantChannelID,
                "Non-Important Messages",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for non-important chat notifications"
                enableVibration(false) // No vibration
                setSound(null, null) // No sound for non-important messages
            }

            // Register both channels with the system
            notificationManager.createNotificationChannel(importantChannel)
            notificationManager.createNotificationChannel(nonImportantChannel)
        }

        Log.d("FirebaseMessaging", "showNotification .......")

        // Build the notification
        val notificationIntent = Intent(this, SplashActivity::class.java).apply {
            putExtra("userId", userId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use different channels based on the importance of the message
        val channelID = if (isImportant == "true") importantChannelID else nonImportantChannelID

        val builder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_chat_logo) // Ensure this icon exists in your drawable resources
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true) // Cancel the notification when clicked

        // Customize notification for important messages
        if (isImportant == "true") {
            builder.setVibrate(longArrayOf(0, 1000, 500, 1000)) // Vibration pattern for important messages
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_SOUND) // Default lights and sound
        } else {
            builder.setSound(null) // No sound for non-important messages
            builder.setVibrate(null) // No vibration for non-important messages
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS) // Only lights
        }

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(0, builder.build())
        } else {
            Log.e("FirebaseMessaging", "Notification permission not granted.")
        }
    }







    /*


     private fun showNotification(title: String, body: String, isImportant: String) {
            // Create notification channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelID, channelName, importance).apply {
                    description = "Channel for important chat notifications"
                    enableVibration(true)
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                }
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            Log.d(
                "FirebaseMessaging",
                "showNotification ......."
            )

            // Build the notification
            val notificationIntent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_chat_logo) // Ensure this icon exists in your drawable resources
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(pendingIntent, true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000)) // Vibration pattern
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_SOUND) // Default lights and sound

            val notificationManager = NotificationManagerCompat.from(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(0, builder.build())
            } else {
                Log.e("FirebaseMessaging", "Notification permission not granted.")
            }
        }
     */


}
