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
import com.example.chatapp.databinding.ActivityLoginPhoneNumberBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginPhoneNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPhoneNumberBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPhone)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val countryCodePicker = binding.loginCountryCode
        val phoneNumber = binding.loginMobileNumber
        countryCodePicker.registerCarrierNumberEditText(phoneNumber)

        binding.loginProgressbar.visibility = View.GONE

        // Send OTP button click listener
        binding.sendOtpButton.setOnClickListener {

            if (!countryCodePicker.isValidFullNumber) {
                phoneNumber.setError("Phone Number not valid!")
                return@setOnClickListener
            } else {
               val intent = Intent(this@LoginPhoneNumberActivity, LoginOtpActivity::class.java)
                intent.putExtra("phone", countryCodePicker.fullNumberWithPlus)
                startActivity(intent)

            }
        }

    }




}










