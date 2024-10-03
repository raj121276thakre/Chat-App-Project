package com.example.chatapp.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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

    companion object {
        var ringtone: Ringtone? = null // Static Ringtone reference
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.data.isNotEmpty()) {

            val title = message.data["title"] ?: "New Message"
            val body = message.data["body"] ?: "You have received a new message"
            // val isImportant = message.data["isImportant"]?.toBoolean() ?: false
            val isImportant = message.data["isImportant"]?.toString()
            val userId = message.data["userId"]?.toString()



            if (title == null || body == null) {
                Log.e("FirebaseMessaging", "Notification title or body is missing.")
                return
            }


            showNotification(title, body, isImportant!!, userId!!)


        } else {
            Log.e("FirebaseMessaging", "Received empty message data.")
        }


    }




    private fun overrideSilentModeAndDND() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val previousRingerMode = audioManager.ringerMode
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Request Do Not Disturb (DND) permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        // Switch to the main thread to modify UI-related components like audio settings
        Handler(Looper.getMainLooper()).post {
            // Override silent mode by setting ringer mode to normal
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

            // Restore the previous ringer mode after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                audioManager.ringerMode = previousRingerMode // Restore previous mode
            }, 10000) // 30 seconds delay
        }
    }


    private fun showNotification(title: String, body: String, isImportant: String, userId: String) {
        // Define channel IDs for important and non-important messages
        val importantChannelID = "important_channel"
        val nonImportantChannelID = "non_important_channel"

        // Check if the message is important and override silent/DND modes
         if (isImportant == "true") {
            overrideSilentModeAndDND() // Ensure this runs before playing the notification
        }


        // Check the Android version to create notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create the channel for important messages
            val importantChannel = NotificationChannel(
                importantChannelID,
                "Important Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for important chat notifications"
                enableVibration(true)
               // val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                setSound(getCustomRingtoneUri(), null)

//                setSound(
//                    soundUri,
//                    Notification.AUDIO_ATTRIBUTES_DEFAULT
//                ) // Play ringtone for important messages
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
            .setSmallIcon(R.mipmap.ic_launcher_round) // Ensure this icon exists in your drawable resources
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true) // Cancel the notification when clicked
            .setDeleteIntent(getDeleteIntent()) // Set the delete intent to stop ringtone

        // Customize notification for important messages
        if (isImportant == "true") {
            builder.setVibrate(
                longArrayOf(
                    0,
                    1000,
                    500,
                    1000
                )
            ) // Vibration pattern for important messages
            builder.setSound(getCustomRingtoneUri()) // Use custom ringtone
           // builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_SOUND) // Default lights and sound
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

            if (isImportant == "true") {
                playCustomRingtone() // Play ringtone immediately for important messages

                // Optional: Stop the ringtone after a set duration (e.g., 30 seconds)
                Handler(Looper.getMainLooper()).postDelayed({
                    stopRingtone()
                }, 10000)
            }



        } else {
            Log.e("FirebaseMessaging", "Notification permission not granted.")
        }
    }


    private fun getDeleteIntent(): PendingIntent {
        val intent = Intent(this, NotificationDismissedReceiver::class.java)
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getCustomRingtoneUri(): Uri {
        return Uri.parse("android.resource://${packageName}/raw/ringtone") // Ensure the file is in res/raw
    }

    private fun playCustomRingtone() {
        val ringtoneUri = getCustomRingtoneUri()
        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone?.play() // Play the ringtone immediately
    }

    fun stopRingtone() {
        ringtone?.stop() // Stop the ringtone if it's playing
        ringtone = null // Clear the ringtone reference
    }




}