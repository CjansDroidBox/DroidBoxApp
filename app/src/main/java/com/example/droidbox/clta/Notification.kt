package com.example.droidbox.clta



data class Notification(
    val id: String = "",
    val message: String = "",
    val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val isRead: Boolean = false
)
