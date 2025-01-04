package com.example.droidbox.clta

data class SharedContent(
    val title: String,
    val description: String,
    val type: String, // e.g., Flashcard, Quiz, etc.
    val sharedContentId: String // Unique ID
)
