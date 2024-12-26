package com.example.droidbox

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Flashcard(
    val title: String,
    val description: String
) : Parcelable
