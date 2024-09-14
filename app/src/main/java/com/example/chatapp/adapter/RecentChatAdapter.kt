package com.example.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.activities.ChattingActivity
import com.example.chatapp.databinding.ItemChatBinding
import com.example.chatapp.models.ChatRoomModel
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil
import com.example.chatapp.utils.Utils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

class RecentChatAdapter(
    options: FirestoreRecyclerOptions<ChatRoomModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<ChatRoomModel, RecentChatAdapter.ChatViewHolder>(options) {

    inner class ChatViewHolder(val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatRoomModel) {

            val otherUserRef = FirebaseUtil.getOtherUserFromChatroom(chat.userIds)
            if (otherUserRef != null) {
                // Proceed with the valid DocumentReference

                otherUserRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                        val lastMessageSentByMe: Boolean =
                            chat.lastMessageSenderId.equals(FirebaseUtil.currentUserId())

                        val otherUserModel: User? = task.getResult().toObject(User::class.java)
                        binding.tvUsername.text = otherUserModel!!.username

                        if (lastMessageSentByMe)
                            binding.tvLastMessage.text = "You: ${chat.lastMessage}"
                        else
                            binding.tvLastMessage.text = chat.lastMessage
                        binding.lastMsgTimestamp.text =
                            chat.lastMessageTimestamp?.toDate()?.let { timeFormat.format(it) } ?: ""

                        // Access the CircleImageView using ViewBinding
                        val profileImage = binding.ivProfileImageLL.profilePicImageView

                        // Load the profile picture using Glide
                        Glide.with(profileImage.context)
                            .load(otherUserModel?.profilePictureUrl)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(profileImage)


                        binding.root.setOnClickListener {
                            val intent = Intent(context, ChattingActivity::class.java)
                            Utils.passUserModelAsIntent(intent, otherUserModel)
                            context.startActivity(intent)
                        }

                    }

                }

            } else {
                // Handle the error case (e.g., log, skip, show error message)
                Log.e(
                    "RecentChatAdapter",
                    "Failed to get a valid document reference for the other user."
                )
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatRoomModel) {
        holder.bind(model)
    }

    fun getChatAtPosition(position: Int): DocumentSnapshot {
        return snapshots.getSnapshot(position)
    }
}
