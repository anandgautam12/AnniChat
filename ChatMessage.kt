package com.anand.annichat.models

data class ChatMessage(
    val id: Int,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val imageUrl: String?,
    val timestamp: String
)