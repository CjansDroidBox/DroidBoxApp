package com.example.droidbox

data class Post(
    val title: String,
    val content: String,
    val timestamp: String,
    var likes: Int = 0,
    var comments: Int = 0,
    val imageUrl: String?
)
