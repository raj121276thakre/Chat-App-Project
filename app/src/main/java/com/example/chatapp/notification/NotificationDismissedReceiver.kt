package com.example.chatapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stop the ringtone when the notification is dismissed
        stopRingtone()
    }

    private fun stopRingtone() {
        FirebaseMessaging.ringtone?.let {
            if (it.isPlaying) {
                it.stop()
                FirebaseMessaging.ringtone = null // Clear the reference
            }
        }
    }
}