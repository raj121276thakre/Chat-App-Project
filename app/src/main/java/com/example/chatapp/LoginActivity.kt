package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapp.activities.MainActivity
import com.example.chatapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
   private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog  // ProgressDialog instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing in...")
        progressDialog.setCancelable(false)
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }


    }

    private fun signInWithGoogle() {
        progressDialog.show() // Show loading before starting sign-in
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, 1001)

        // ✅ Sign out from GoogleSignInClient to force account selection
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                progressDialog.dismiss() // Dismiss if sign-in fails
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    // ✅ Get Profile Picture URL from Google Account
                    val profilePictureUrl = account?.photoUrl?.toString()
                    checkUserExists(firebaseUser?.uid, firebaseUser?.displayName, firebaseUser?.email, profilePictureUrl)
                } else {
                    progressDialog.dismiss() // Dismiss if authentication fails
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserExists(
        userId: String?,
        username: String?,
        email: String?,
        profilePictureUrl: String?
    ) {
        if (userId == null) return

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // User exists, navigate to MainActivity
                    gotoMainActivity()
                } else {
                    // New user, save to Firestore
                    val newUser = User(
                        userId = userId,
                        username = username ?: "User",
                        phone = email ?: "",
                        profilePictureUrl = profilePictureUrl, // ✅ Store Profile Picture
                        createdTimestamp = Timestamp.now()
                    )
                    saveUserToFirestore(newUser)
                    gotoMainActivity()
                }
            }
            progressDialog.dismiss() // Dismiss after data retrieval
        }
    }

    private fun saveUserToFirestore(user: User) {
        FirebaseUtil.currentUserDetails()
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss() // Dismiss in case of failure
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}