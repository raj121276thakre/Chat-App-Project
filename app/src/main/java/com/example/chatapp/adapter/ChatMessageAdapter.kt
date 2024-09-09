package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemChatIncomingBinding
import com.example.chatapp.databinding.ItemChatOutgoingBinding
import com.example.chatapp.models.ChatMessageModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.text.SimpleDateFormat
import java.util.Locale


class ChatMessageAdapter(
    options: FirestoreRecyclerOptions<ChatMessageModel>,
    private val currentUserId: String
) : FirestoreRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>(options) {

    companion object {
        private const val VIEW_TYPE_INCOMING = 1
        private const val VIEW_TYPE_OUTGOING = 2
    }

    inner class IncomingMessageViewHolder(val binding: ItemChatIncomingBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class OutgoingMessageViewHolder(val binding: ItemChatOutgoingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INCOMING) {
            IncomingMessageViewHolder(
                ItemChatIncomingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            OutgoingMessageViewHolder(
                ItemChatOutgoingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        model: ChatMessageModel
    ) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        if (holder is IncomingMessageViewHolder) {
            holder.binding.apply {
                tvMessage.text = model.message
                tvTimestamp.text = model.timestamp?.toDate()?.let { timeFormat.format(it) } ?: ""
            }
        } else if (holder is OutgoingMessageViewHolder) {
            holder.binding.apply {
                tvMessage.text = model.message
                tvTimestamp.text = model.timestamp?.toDate()?.let { timeFormat.format(it) } ?: ""
                //progressbar

                // Display or hide the progress bar based on whether the message is scheduled
                progressBar.visibility = if (model.isScheduled) View.VISIBLE else View.GONE

            }
        }
    }



    override fun getItemViewType(position: Int): Int {
        val chatMessageModel = getItem(position)
        return if (chatMessageModel.senderId == currentUserId) {
            VIEW_TYPE_OUTGOING
        } else {
            VIEW_TYPE_INCOMING
        }
    }


    // Register AdapterDataObserver
    fun registerAdapterDataObserver(recyclerView: RecyclerView) {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val messageCount = itemCount
                val lastVisiblePosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()

                // Automatically scroll to the bottom if the user is at or near the bottom
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (messageCount - 1) && lastVisiblePosition == (positionStart - 1))
                ) {
                    recyclerView.smoothScrollToPosition(positionStart)
                }
            }
        })
    }

}


