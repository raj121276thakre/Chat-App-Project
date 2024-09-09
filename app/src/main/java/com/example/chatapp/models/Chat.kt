package com.example.chatapp.models

data class Chat(
    val username: String,
    val lastMessage: String,
    val timestamp: String,
    val profileImage: Int,  // Assuming images are in drawable
    val isRead: Boolean,
    val isTyping: Boolean
)
