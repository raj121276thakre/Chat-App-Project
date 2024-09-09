package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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