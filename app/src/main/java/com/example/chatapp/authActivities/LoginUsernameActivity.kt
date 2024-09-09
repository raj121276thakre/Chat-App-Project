package com.example.chatapp.authActivities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapp.R
import com.example.chatapp.activities.MainActivity
import com.example.chatapp.databinding.ActivityLoginUsernameBinding
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil
import com.google.firebase.Timestamp

class LoginUsernameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginUsernameBinding
   // private lateinit var userName: String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginUserName)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        phoneNumber = intent.getStringExtra("phone").toString()
       // userName = binding.loginUsername.text.toString()

        getUserNameFromFirebase()

        binding.loginLetMeInButton.setOnClickListener {
            val userName = binding.loginUsername.text.toString().trim()
            setUserDataToFirebase(userName,phoneNumber)
        }



    }

    private fun setUserDataToFirebase(userName: String, phoneNumber: String) {
        setInProgress(true)

        if (userName.isEmpty() || userName.length < 3) {
            binding.loginUsername.setError( "Username length should be at least 3 characters!")
            setInProgress(false)
            return
        }


        // Check if user already exists
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // User exists, update the existing user's data
                    val userId = FirebaseUtil.currentUserId()
                    val updatedUser = User(
                        userId = userId!!,
                        username = userName,
                        phone = phoneNumber,
                        createdTimestamp = document.getTimestamp("createdTimestamp"),
                        profilePictureUrl = document.getString("profilePictureUrl") ?: "",
                        about = document.getString("about") ?: "Hey there! I am using ChatApp."
                    )
                    saveUserToFirestore(updatedUser)
                    gotoMainActivity()
                } else {
                    // User doesn't exist, create a new user
                    val userId = FirebaseUtil.currentUserId()
                    val newUser = User(
                        userId = userId!!,
                        username = userName,
                        phone = phoneNumber,
                        createdTimestamp = Timestamp.now()
                    )
                    saveUserToFirestore(newUser)
                    gotoMainActivity()
                }
            } else {
                // Handle the error
                task.exception?.let {
                    // Log or display the error
                }
            }
            setInProgress(false)
        }
    }

    private fun gotoMainActivity() {
        val intent = Intent(this@LoginUsernameActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun saveUserToFirestore(user: User) {
        FirebaseUtil.currentUserDetails()
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getUserNameFromFirebase() {
        setInProgress(true)
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
            setInProgress(false)
            if (task.isSuccessful) {
               val  document = task.result
                if (document != null && document.exists()) {
                    val userData = document.data
                    val user = document.toObject(User::class.java)
                    binding.loginUsername.setText(user!!.username)
                } else {
                    // No such document
                    // Handle the case where the user data is not found
                }
            } else {
                // Handle the error
                task.exception?.let {
                    // Log or display the error
                }
            }
        }

    }

    private fun setInProgress(inProgress: Boolean) {

        if (inProgress) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginLetMeInButton.visibility = View.GONE
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginLetMeInButton.visibility = View.VISIBLE
        }

    }

}