package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemSearchUserBinding
import com.example.chatapp.models.User
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import de.hdodenhof.circleimageview.CircleImageView

class SearchUserAdapter(
    options: FirestoreRecyclerOptions<User>,
    private val onUserClick: (User) -> Unit
) : FirestoreRecyclerAdapter<User, SearchUserAdapter.UserViewHolder>(options) {

    private var currentUserId: String? = null
    inner class UserViewHolder(private val binding: ItemSearchUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            binding.userNameText.text = if (user.userId == currentUserId) {
                "${user.username} (me)"
            } else {
                user.username
            }

           // binding.userNameText.text = user.username
            binding.phoneText.text = user.phone

            val profileImage = itemView.findViewById<CircleImageView>(R.id.profile_pic_image_view)

            // Load the profile picture using Glide or any other image loading library
            Glide.with(profileImage.context)
                .load(user.profilePictureUrl)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profileImage)


            // Handle click events
            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemSearchUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
        holder.bind(model)
    }

    fun getUserAtPosition(position: Int): DocumentSnapshot {
        return snapshots.getSnapshot(position)
    }
}
