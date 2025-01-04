package com.example.droidbox.clta

data class HomeContent(
    val title: String,
    val description: String,
    val type: String, // e.g., Flashcard, Quiz
    val sharedContentId: String // Unique ID for navigation
)
