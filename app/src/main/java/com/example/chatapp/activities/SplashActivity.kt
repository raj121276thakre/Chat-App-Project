package com.example.chatapp.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapp.R
import com.example.chatapp.authActivities.LoginPhoneNumberActivity
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil.allUserCollectionReference
import com.example.chatapp.utils.Utils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot


class SplashActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check and request to disable battery optimization for the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }


        if (intent.extras != null) {
            //from notification
            val userId = intent.extras!!.getString("userId")
            allUserCollectionReference().document(userId!!).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        val model: User? = task.result.toObject(User::class.java)

                        val mainIntent = Intent(
                            this,
                            MainActivity::class.java
                        )
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(mainIntent)

                        val intent = Intent(
                            this,
                            ChattingActivity::class.java
                        )
                        Utils.passUserModelAsIntent(intent, model!!)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
        } else {


            // Check login status and navigate accordingly
            Handler().postDelayed({
                if (auth.currentUser != null) {
                    // User is logged in, navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // User is not logged in, navigate to LoginActivity
                    startActivity(Intent(this, LoginPhoneNumberActivity::class.java))
                }
                finish()

            }, 2000) // Splash screen delay

        }
    }


}