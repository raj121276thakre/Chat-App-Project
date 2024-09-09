package com.example.chatapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.utils.FirebaseUtil.currentUserDetails
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarTitle: TextView
    private lateinit var token: String

    // Declare the launcher at the top of your Activity/Fragment
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications
        } else {
            // Handle the case where the user denies the permission
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        askNotificationPermission()

        setupBottomNavigation()

        getFCMToken()





    }









    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications
                // The permission is already granted, you can proceed with notifications
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }




    fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (task.isSuccessful) {
                 token = task.result.toString()
                Log.i("My Fcm Token : ", token!!)
                currentUserDetails().update("fcmToken", token)
            }
        }
    }


    private fun setupBottomNavigation() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Set up a listener to change the title based on the destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = when (destination.id) {
                R.id.chatFragment -> "Recent Chats"
                R.id.profileFragment -> "Profile"
//                R.id.homeFragment -> "Home"
//                R.id.shoppingFragment -> "Shop"
                else -> getString(R.string.app_name).toString()
            }
            Log.d("MainActivity", "Navigated to ${destination.label}")
        }

        // Handle BottomNavigationView item selection
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_chats -> {
                    navController.navigate(R.id.chatFragment)
                    true
                }

                R.id.nav_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }


                else -> false
            }
        }

        changeToolbarTitle(navController)
    }

    private fun changeToolbarTitle(navController: NavController) {
        // Initialize toolbarTitle
        toolbarTitle = findViewById(R.id.toolbarTitle)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.chatFragment -> toolbarTitle.text = "Recent Chats"
                R.id.profileFragment -> toolbarTitle.text = "Profile"
                // Add more cases as needed

                else -> toolbarTitle.text = getString(R.string.app_name).toString() // Default title
            }
        }

    }










}