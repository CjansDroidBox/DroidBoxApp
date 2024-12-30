package com.example.droidbox.clta

data class FlashcardHistory(
    val action: String, // e.g., Created, Deleted, Renamed, Shared, Downloaded
    val sectionName: String,
    val dateTime: String
)


