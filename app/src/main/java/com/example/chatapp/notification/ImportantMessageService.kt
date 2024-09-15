package com.example.chatapp.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.chatapp.R
import com.example.chatapp.activities.SplashActivity

class ImportantMessageService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        // Play ringtone
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone) // Ensure this file exists in res/raw
        mediaPlayer?.apply {
            isLooping = true
            start()
        }

        // Start vibration
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationPattern = longArrayOf(0, 1000, 1000)
        vibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))

        // Stop after 30 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 30_000)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        vibrator?.cancel()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, SplashActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "important_message_channel")
            .setContentTitle("Important Message")
            .setContentText("You have an important message!")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Ensure this icon exists in your drawable resources
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pendingIntent, true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
