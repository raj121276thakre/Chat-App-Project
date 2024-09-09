package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.adapter.SearchUserAdapter
import com.example.chatapp.databinding.ActivitySearchUserBinding
import com.example.chatapp.models.User
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.passUserModelAsIntent
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private var adapter: SearchUserAdapter? = null
    private lateinit var db: FirebaseFirestore
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and get the current user ID
        db = FirebaseFirestore.getInstance()
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Initial query to load all users excluding the current user
        setUpRecyclerView("")

        // Add TextWatcher to the search bar to listen for text changes
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the query based on the search input
                setUpRecyclerView(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.goBackBtn.setOnClickListener {
            onBackPressed()
        }

    }


    private fun setUpRecyclerView(searchText: String) {
        // Log the search text
        Log.d("SearchUserActivity", "Searching for: $searchText")

        // Get reference to the users collection
        val usersCollection = db.collection("users")

        // Create a query to exclude the current user and filter based on search text
        var query: Query = usersCollection
            //  .whereNotEqualTo("userId", currentUserId)
            .orderBy("username")

        // Apply search filter if searchText is provided
        if (searchText.isNotEmpty()) {
            query = query
                .whereGreaterThanOrEqualTo("username", searchText)
                .whereLessThanOrEqualTo("username", "$searchText\uf8ff")
        }

        // Configure FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = SearchUserAdapter(options) { user ->
            // Handle user click
            // For example, navigate to the chat screen or show user details
            navigateToChatScreen(user)
        }

        // Set up RecyclerView
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatsRecyclerView.adapter = adapter

        // Start listening with the new query
        adapter?.startListening()
    }


    private fun navigateToChatScreen(user: User) {
        val intent = Intent(this, ChattingActivity::class.java)
        Utils.passUserModelAsIntent(intent, user)
        startActivity(intent)
    }



    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.startListening()
    }

    override fun onResume() {
        super.onResume()
        adapter?.startListening()
    }


}