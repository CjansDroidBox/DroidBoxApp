package com.example.droidbox

data class Post(
    val title: String,
    val content: String,
    val timestamp: String,
    val type: PostType // This refers to the enum
)

enum class PostType {
    FLASHCARD,
    QUIZ,
    VIDEO,
    DOCUMENT
}
