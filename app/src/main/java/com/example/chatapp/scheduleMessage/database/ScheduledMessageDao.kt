package com.example.chatapp.scheduleMessage.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ScheduledMessageDao {
    @Insert
    suspend fun insert(message: ScheduledMessage)
    
    @Query("SELECT * FROM scheduled_messages WHERE timestamp <= :currentTime")
    suspend fun getMessagesToSend(currentTime: Long): List<ScheduledMessage>
    
    @Delete
    suspend fun delete(message: ScheduledMessage)

    @Query("SELECT * FROM scheduled_messages")
    suspend fun getAllScheduledMessages(): List<ScheduledMessage>

}