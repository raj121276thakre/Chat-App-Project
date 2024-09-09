package com.example.chatapp.activities

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.graphics.Rect
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapter.ChatMessageAdapter
import com.example.chatapp.databinding.ActivityChattingBinding
import com.example.chatapp.models.ChatMessageModel
import com.example.chatapp.models.ChatRoomModel
import com.example.chatapp.models.User
import com.example.chatapp.notification.AccessToken
import com.example.chatapp.utils.FirebaseUtil
import com.example.chatapp.utils.FirebaseUtil.currentUserDetails
import com.example.chatapp.utils.Utils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Arrays
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.work.WorkRequest
import com.example.chatapp.adapter.ScheduledMessageAdapter
import com.example.chatapp.scheduleMessage.SendMessageWorker
import com.example.chatapp.scheduleMessage.database.AppDatabase
import com.example.chatapp.scheduleMessage.database.ScheduledMessage
import com.example.chatapp.utils.UserStatusUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChattingActivity : AppCompatActivity() {

    /////////////////////////  Imp Message ring only working when App is opened /////////////////////////////

    private lateinit var binding: ActivityChattingBinding

    private lateinit var otherUser: User
    private lateinit var chatroomId: String
    private lateinit var chatRoomModel: ChatRoomModel
    private lateinit var adapter: ChatMessageAdapter

    private lateinit var importantMessageCB: CheckBox
    private var isImportant: Boolean = false

    private var scheduledTimeInMillis: Long? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chattingActivityLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Retrieve the User object from the Intent
        otherUser = Utils.getUserModelFromIntent(intent) ?: User()
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId()!!, otherUser.userId)

        setupChat()

        listenToOtherUserStatus()

        // Call the function to adjust layout for keyboard
        adjustLayoutForKeyboard()

        binding.goBackBtn.setOnClickListener {
            onBackPressed()
        }

        getOrCreateChatroomModel()
        setUpChatRecyclerview()

        // added for getting accessToken
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        FirebaseMessaging.getInstance().subscribeToTopic("test")



        importantMessageCB = findViewById(R.id.importantMessageCB)
        isImportant = importantMessageCB.isChecked


//        binding.sendMessageBtn.setOnClickListener {
//            val message = binding.messageInputET.text.toString().trim()
//            if (message.isEmpty()) {
//                return@setOnClickListener
//            }
//
//            sendMessageToUser(message)
//
//        }


        //............
        // Set up click listeners
        binding.scheduleMsgTimeBtn.setOnClickListener {
            showTimePickerDialog()
        }

        binding.sendMessageBtn.setOnClickListener {
            val message = binding.messageInputET.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (scheduledTimeInMillis != null) {
                val scheduledTime = scheduledTimeInMillis!!
                val currentTime = System.currentTimeMillis()
                if (scheduledTime > currentTime) {
                    scheduleMessage(message)
                    // Clear the message from the EditText only after scheduling
                    binding.messageInputET.text.clear()
                    // Uncheck the isImportant checkbox
                    binding.importantMessageCB.isChecked = false
                    // Clear the scheduled time
                    scheduledTimeInMillis = null
                } else {
                    Toast.makeText(this, "Scheduled time must be in the future", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                sendMessageToUser(message)

            }
        }


        //............//

        binding.ivMenu.setOnClickListener {
            showScheduledMessagesDialog()
        }


    }


    private fun showScheduledMessagesDialog() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()

        lifecycleScope.launch {
            try {
                // Retrieve messages from the database
                val messages = db.scheduledMessageDao().getAllScheduledMessages()

                // Inflate the custom layout
                val dialogView = LayoutInflater.from(this@ChattingActivity)
                    .inflate(R.layout.dialog_scheduled_messages, null)

                // Set up the RecyclerView
                val recyclerView: RecyclerView = dialogView.findViewById(R.id.recycler_view_scheduled_messages)
                recyclerView.layoutManager = LinearLayoutManager(this@ChattingActivity)
                recyclerView.adapter = ScheduledMessageAdapter(messages)

                // Create and show the AlertDialog
                AlertDialog.Builder(this@ChattingActivity)
                    .setTitle("Scheduled Messages")
                    .setView(dialogView)
                    .setPositiveButton("OK", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@ChattingActivity, "Error loading messages: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


//......

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                scheduledTimeInMillis = calendar.timeInMillis
                Toast.makeText(this, "Message scheduled for $hourOfDay:$minute", Toast.LENGTH_SHORT)
                    .show()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun scheduleMessage(message: String) {
        scheduledTimeInMillis?.let { scheduledTime ->
            val delay = scheduledTime - System.currentTimeMillis()
            if (delay > 0) {
                // Save message locally
                val scheduledMessage = ScheduledMessage(
                    message = message,
                    timestamp = scheduledTime,
                    isImportant = binding.importantMessageCB.isChecked,
                    senderId = FirebaseUtil.currentUserId()!!,
                    chatroomId = chatroomId
                )

                // Use a background thread or coroutine to insert the message
                CoroutineScope(Dispatchers.IO).launch {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    ).build()
                    db.scheduledMessageDao().insert(scheduledMessage)
                }

                // Prepare data for WorkManager
                val workData = workDataOf(
                    "CHATROOM_ID" to chatroomId,
                    "RECIPIENT_USER_ID" to otherUser.userId,
                    "RECIPIENT_TOKEN" to otherUser.fcmToken // Assuming this is the FCM token of the recipient
                )

                val workRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workData)
                    .build()

                WorkManager.getInstance(this).enqueue(workRequest)

                Toast.makeText(this, "Message scheduled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Scheduled time must be in the future", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


//......


    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun adjustLayoutForKeyboard() {
        val rootView = binding.chattingActivityLayout
        val layoutMessageInput = binding.layoutMessageInput

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val layoutParams = layoutMessageInput.layoutParams as RelativeLayout.LayoutParams
            val marginInDp = if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is visible, adjust bottom margin in dp
                // dpToPx(keypadHeight  / resources.displayMetrics.density.toInt())
                dpToPx((keypadHeight - 300) / resources.displayMetrics.density.toInt())

            } else {
                // Keyboard is hidden, reset bottom margin in dp
                dpToPx(14)
            }

            layoutParams.bottomMargin = marginInDp
            layoutMessageInput.layoutParams = layoutParams


        }
    }


    private fun setUpChatRecyclerview() {
        val query: Query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<ChatMessageModel> =
            FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel::class.java)
                .build()

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        adapter = ChatMessageAdapter(options, currentUserId)

        // Set up RecyclerView
        val manager: LinearLayoutManager = LinearLayoutManager(this)
        manager.reverseLayout = true

        binding.rvChatMessages.layoutManager = manager
        binding.rvChatMessages.adapter = adapter

        // Register the AdapterDataObserver
        adapter?.registerAdapterDataObserver(binding.rvChatMessages)

        // Start listening with the new query
        adapter.startListening()


    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


    override fun onResume() {
        super.onResume()
        adapter.startListening()
        UserStatusUtil.updateUserStatus(FirebaseAuth.getInstance().currentUser?.uid!!, "Online")
    }

    override fun onPause() {
        super.onPause()
        UserStatusUtil.updateUserStatus(
            FirebaseAuth.getInstance().currentUser?.uid!!,
            "Last seen at ${UserStatusUtil.getCurrentTime()}"
        )
    }

    private fun listenToOtherUserStatus() {
        UserStatusUtil.listenToUserStatus(otherUser.userId) { status ->
            binding.tvStatus.text = status ?: "Offline"
        }
    }


    private fun getOrCreateChatroomModel() {
        val chatRoomReference = FirebaseUtil.getChatroomReference(chatroomId)

        chatRoomReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Convert DocumentSnapshot to com.example.chatapp.models.ChatRoomModel
                    chatRoomModel = documentSnapshot.toObject(ChatRoomModel::class.java)!!

                    // Handle the fetched com.example.chatapp.models.ChatRoomModel
                    chatRoomModel?.let {
                        println("com.example.chatapp.models.ChatRoomModel: $it")
                    }
                } else {
                    // Create a new com.example.chatapp.models.ChatRoomModel
                    val newChatRoomModel = ChatRoomModel(
                        chatRoomId = chatroomId,
                        userIds = Arrays.asList(FirebaseUtil.currentUserId(), otherUser.userId),
                        lastMessageTimestamp = Timestamp.now(),
                        lastMessageSenderId = ""
                    )

                    // Add the new chatroom to Firestore
                    chatRoomReference.set(newChatRoomModel)
                        .addOnSuccessListener {
                            println("ChatRoom created successfully!")
                            // Update chatRoomModel with the newly created model
                            chatRoomModel = newChatRoomModel
                        }
                        .addOnFailureListener { exception ->
                            println("Error creating chatroom: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the fetch
                println("Error fetching chatroom: ${exception.message}")
            }
    }

    private fun setupChat() {
        // Use user details, e.g., set the username in a TextView
        binding.tvUsername.text = otherUser.username

        val profileImage = binding.profilePicLayout.profilePicImageView

        // Load the profile picture using Glide
        Glide.with(profileImage.context)
            .load(otherUser?.profilePictureUrl)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(profileImage)


    }


    private fun sendMessageToUser(message: String) {
        chatRoomModel.lastMessageTimestamp = Timestamp.now()
        chatRoomModel.lastMessageSenderId = FirebaseUtil.currentUserId()!!
        chatRoomModel.lastMessage = message
        FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel)

        val chatMessageModel =
            ChatMessageModel(message, Timestamp.now(), FirebaseUtil.currentUserId()!!)

        chatMessageModel.isImportant = isImportant

        // Add the new message to the chatroom's messages collection in Firestore
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Clear the message from the EditText if the message was sent successfully
                    binding.messageInputET
                        .text.clear()
                    // Uncheck the isImportant checkbox
                    binding.importantMessageCB.isChecked = false

                    if (isImportant) {
                        sendImportantMessageNotification(message, otherUser.fcmToken)
                    } else {
                        sendNotification(message, otherUser.fcmToken)
                    }

                    //  sendNotification(message, otherUser.fcmToken)

                    println("Message sent successfully!")
                } else {
                    // Handle any errors that occurred while sending the message
                    println("Error sending message: ${task.exception?.message}")
                }
            }


    }

/*
    //..............    // Function to send Important notification using Firebase Cloud Messaging API (HTTP v1)
    private fun sendImportantMessageNotification(message: String, recipientToken: String) {
        currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)
                try {
                    val client = OkHttpClient()

                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
                                .put(
                                    "notification", JSONObject()
                                        .put("title", currentUser!!.username)
                                        .put("body", message)
                                        .put(
                                            "sound",
                                            "default"
                                        ) // Play the default sound, often the ringtone
                                )
                                .put(
                                    "data", JSONObject() // Include the data payload
                                        .put(
                                            "isImportant",
                                            true.toString()
                                        ) // Pass isImportant as true
                                )
                                .put(
                                    "android", JSONObject()
                                        .put("priority", "high")
                                        .put(
                                            "notification", JSONObject()
                                                .put(
                                                    "sound",
                                                    "default"
                                                ) // Ensure this matches the default sound setup
                                        )
                                )
                        )

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chat-app-15577/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                                println("Response body: ${response.body?.string()}")
                            } else {
                                println("Notification sent successfully")
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }



 */


    private fun sendImportantMessageNotification(message: String, recipientToken: String) {
        currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)
                try {
                    val client = OkHttpClient()

                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
                                .put(
                                    "data", JSONObject() // Custom data payload
                                        .put("title", currentUser!!.username) // Sender's name
                                        .put("body", message) // Message content
                                        .put("isImportant", true.toString()) // Mark message as important
                                )
                                .put(
                                    "android", JSONObject()
                                        .put("priority", "high") // High priority for urgent delivery
                                        .put(
                                            "notification", JSONObject()
                                                .put("vibrate", true) // Ensure vibration is enabled
                                                .put("sound", "default") // Default sound (ringtone)
                                        )
                                )
                        )


//                    val jsonPayload = JSONObject()
//                        .put(
//                            "message", JSONObject()
//                                .put("token", recipientToken)
//                                .put(
//                                    "notification", JSONObject() // Add notification settings
//                                        .put("title", currentUser!!.username)  // Sender's name
//                                        .put("body", message)  // Message content
//                                        .put("sound", "default")  // Default sound (ringtone)
//                                )
//                                .put(
//                                    "data", JSONObject() // Include custom data payload
//                                        .put("title", currentUser!!.username)  // Sender's name
//                                        .put("body", message)  // Message content
//                                        .put("isImportant", true.toString())  // Mark message as important
//                                )
//                                .put(
//                                    "android", JSONObject()
//                                        .put("priority", "high")  // High priority for urgent delivery
//                                        .put(
//                                            "notification", JSONObject()
//                                                .put("vibrate", true)  // Ensure vibration is enabled
//                                                .put("sound", "default")  // Default sound (ringtone)
//                                        )
//                                )
//                        )


                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/your-project-id/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                            } else {
                                println("Notification sent successfully")
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }




    // Function to send notification using Firebase Cloud Messaging API (HTTP v1)


    private fun sendNotification(message: String, recipientToken: String) {

        currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val currentUser: User? = task.result.toObject(User::class.java)
                try {

                    val client = OkHttpClient()

                    // Construct the JSON payload
                    val jsonPayload = JSONObject()
                        .put(
                            "message", JSONObject()
                                .put("token", recipientToken)
                                .put(
                                    "notification", JSONObject()
                                        .put("title", currentUser!!.username)
                                        .put("body", message)
                                )
                                .put(
                                    "android", JSONObject()
                                        .put("priority", "high")
                                )
                                .put(
                                    "apns", JSONObject()
                                        .put(
                                            "headers", JSONObject()
                                                .put("apns-priority", "10")
                                        )
                                )
                                .put(
                                    "data",
                                    JSONObject()                       // Custom data payload
                                        .put("userId", currentUser.userId)
                                )
                        )


                    val mediaType = "application/json; charset=utf-8".toMediaType()

                    //val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonPayload.toString())

                    val requestBody = RequestBody.create(mediaType, jsonPayload.toString())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chat-app-15577/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer ${AccessToken.getAccessToken()}")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                println("Failed to send notification: ${response.code}")
                                println("Response body: ${response.body?.string()}")
                                Log.i("Response body", "${response.body?.string()}")
                            } else {
                                println("Notification sent successfully")
                            }
                        }
                    })


                } catch (e: Exception) {

                }
            }
        }


    }


}