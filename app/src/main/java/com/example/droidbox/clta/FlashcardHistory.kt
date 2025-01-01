package com.example.droidbox.clta

data class FlashcardHistory(
    val action: String, // e.g., Created, Deleted, Renamed, Shared, Downloaded
    val sectionName: String,
    val previousName: String? = null, // Nullable, only applicable for Renamed action
    val dateTime: String,
    val details: Details = Details() // Details object for shared/downloaded info
)

data class Details(
    val shared: Boolean = false,
    val downloaded: Boolean = false
)