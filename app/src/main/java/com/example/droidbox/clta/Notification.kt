package com.example.droidbox.clta

import com.google.firebase.Timestamp

data class Notification(
    val id: String, // Document ID
    val title: String,
    val description: String,
    val ownerName: String,
    val sharedContentId: String,
    val timestamp: Timestamp,
    val isRead: Boolean,
    val type: String // e.g., Flashcard, Quiz
)
