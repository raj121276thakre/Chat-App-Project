package com.example.chatapp.authActivities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityLoginOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginOtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginOtpBinding
    private lateinit var verificationId: String
    private lateinit var phoneNumber: String
    private val auth by lazy { Firebase.auth }


    private lateinit var resendingToken: ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginOTPActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        phoneNumber = intent.getStringExtra("phone").toString()
        sendOtp(phoneNumber!!, false)

        binding.resendOtpBtn.setOnClickListener {
            sendOtp(phoneNumber!!, true)
        }

        binding.nextBtn.setOnClickListener {
            val otp = binding.loginOtp.text.toString()

            if (otp.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                signIn(credential)
                setInProgress(true)
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun sendOtp(number: String, isResend: Boolean) {
        Toast.makeText(this, "Sending OTP to $number", Toast.LENGTH_SHORT).show()

        setInProgress(true)
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks

        if (isResend && ::resendingToken.isInitialized) {
            // Use the resending token to resend the OTP
            optionsBuilder.setForceResendingToken(resendingToken)
        }

        val options = optionsBuilder.build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun startResendTimer() {
        val resendOtp = binding.resendOtpBtn
        resendOtp.isEnabled = false

        // Set the timer for 60 seconds (60,000 milliseconds)
        val resendTimeInMillis: Long = 60000
        val countdownInterval: Long = 1000 // 1 second intervals

        val countDownTimer = object : CountDownTimer(resendTimeInMillis, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the button text with the remaining time
                resendOtp.text = "Resend OTP in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                // Re-enable the resend button and reset the text
                resendOtp.isEnabled = true
                resendOtp.text = "Resend OTP"
            }
        }

        countDownTimer.start()
    }


    private fun setInProgress(inProgress: Boolean) {

        if (inProgress) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.nextBtn.visibility = View.GONE
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.nextBtn.visibility = View.VISIBLE
        }

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            auth.signInWithCredential(credential)
                .addOnCompleteListener(this@LoginOtpActivity) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@LoginOtpActivity,
                            "Login successful",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        signIn(credential)
                        setInProgress(false)

                    } else {
                        Toast.makeText(
                            this@LoginOtpActivity,
                            "Verification failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        override fun onVerificationFailed(e: FirebaseException) {

            Toast.makeText(
                this@LoginOtpActivity,
                "Verification failed: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()

            setInProgress(false)
        }

        override fun onCodeSent(
            id: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {

            super.onCodeSent(id, token)
            verificationId = id
            resendingToken = token
            Toast.makeText(this@LoginOtpActivity, "OTP sent successfully", Toast.LENGTH_SHORT)
                .show()
            setInProgress(false)
            startResendTimer()
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        setInProgress(true)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "OTP verified successful", Toast.LENGTH_SHORT).show()

                   val intent = Intent(this@LoginOtpActivity, LoginUsernameActivity::class.java)
                    intent.putExtra("phone", phoneNumber)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this,
                        "OTP Verification failed !",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }


}