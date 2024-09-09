package com.example.chatapp.scheduleMessage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val timestamp: Long,
    val isImportant: Boolean,
    val senderId: String,
    val chatroomId: String
)