package com.anand.annichat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anand.annichat.R
import com.anand.annichat.models.ChatMessage
import com.bumptech.glide.Glide

class ChatAdapter(
    private val chatList: List<ChatMessage>,
    private val senderId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMessage: TextView = view.findViewById(R.id.textMessage)
        val imageMessage: ImageView = view.findViewById(R.id.imageMessage)
        val bubble: View = view.findViewById(R.id.bubbleLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatList[position]

        if (message.imageUrl.isNullOrEmpty()) {
            holder.textMessage.visibility = View.VISIBLE
            holder.imageMessage.visibility = View.GONE
            holder.textMessage.text = message.message
        } else {
            holder.textMessage.visibility = View.GONE
            holder.imageMessage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(message.imageUrl).into(holder.imageMessage)
        }

        // Align messages
        val params = holder.bubble.layoutParams as ViewGroup.MarginLayoutParams
        if (message.senderId == senderId) {
            params.marginStart = 100
            params.marginEnd = 10
            holder.bubble.setBackgroundResource(R.drawable.sender_bubble)
        } else {
            params.marginStart = 10
            params.marginEnd = 100
            holder.bubble.setBackgroundResource(R.drawable.receiver_bubble)
        }
        holder.bubble.layoutParams = params
    }

    override fun getItemCount(): Int = chatList.size
}