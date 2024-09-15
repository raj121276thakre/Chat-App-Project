package com.example.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.activities.SearchUserActivity
import com.example.chatapp.adapter.RecentChatAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.models.ChatRoomModel
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var adapter: RecentChatAdapter? = null
    private lateinit var chatQuery: Query


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString().trim())  // Call the search function
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(requireContext(), SearchUserActivity::class.java))
        }
    }

    private fun performSearch(searchText: String) {
        if (searchText.isEmpty()) {
            // Reset to the original query if search text is empty
            chatQuery = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId()!!)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

            // Reset the adapter with the original query
            val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                .setQuery(chatQuery, ChatRoomModel::class.java)
                .build()
            adapter?.updateOptions(options)

        } else {
            // Query usernames that match the search text (case-insensitive)
            FirebaseUtil.allUserCollectionReference()
                .orderBy("username")
                .startAt(searchText.capitalize())
                .endAt("${searchText.capitalize()}\uf8ff")
                .get().addOnSuccessListener { querySnapshot ->
                    val matchingUserIds = querySnapshot.documents.map { it.id }

                    if (matchingUserIds.isNotEmpty()) {
                        // Apply query only if there are matching users
                        chatQuery = FirebaseUtil.allChatroomCollectionReference()
                            .whereArrayContainsAny("userIds", matchingUserIds)
                            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

                        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                            .setQuery(chatQuery, ChatRoomModel::class.java)
                            .build()

                        adapter?.updateOptions(options)
                    } else {
                        // No matching users, set empty results
                        val emptyOptions = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                            .setQuery(FirebaseUtil.allChatroomCollectionReference()
                                .whereEqualTo("userIds", "null"), ChatRoomModel::class.java) // Dummy query that returns no results
                            .build()

                        adapter?.updateOptions(emptyOptions)
                    }
                }
        }
    }



    private fun setUpRecyclerView() {
        // Set up the initial query
        chatQuery = FirebaseUtil.allChatroomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtil.currentUserId()!!)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<ChatRoomModel> = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(chatQuery, ChatRoomModel::class.java)
            .build()

        adapter = RecentChatAdapter(options, requireContext())

        binding.recentChatsRV.layoutManager = LinearLayoutManager(requireContext())
        binding.recentChatsRV.adapter = adapter
    }



  /*  private fun setUpRecyclerView() {
        val query: Query = FirebaseUtil.allChatroomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtil.currentUserId()!!)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<ChatRoomModel> =
            FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                .setQuery(query, ChatRoomModel::class.java)
                .build()

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        adapter = RecentChatAdapter(options, requireContext())

//        // Set up RecyclerView
//        val manager : LinearLayoutManager = LinearLayoutManager(this)
//        manager.reverseLayout = true

        binding.recentChatsRV.layoutManager = LinearLayoutManager(requireContext())
        binding.recentChatsRV.adapter = adapter

        // Register the AdapterDataObserver
        //  adapter?.registerAdapterDataObserver(binding.recentChatsRV)

        // Start listening with the new query
        adapter?.startListening()

    }

   */

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening() // Stop listening when not visible
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged() // Restart listening when resumed
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}