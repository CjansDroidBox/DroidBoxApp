package com.example.droidbox.clta

import com.google.firebase.Timestamp

data class Notification(
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)
