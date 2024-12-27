
package com.example.droidbox

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class FlashcardViewer : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var flashcardAdapter: FlashcardAdapter

    // UPDATED LOGIC: Pass context to FlashcardAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flashcard_viewer)

        // Initialize ViewPager2
        viewPager = findViewById(R.id.viewPager)

        // Retrieve flashcards from intent
        val flashcards = intent.getParcelableArrayListExtra<Flashcard>("flashcards") ?: arrayListOf()
        if (flashcards.isEmpty()) {
            Toast.makeText(this, "No flashcards to display!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up the adapter with context
        flashcardAdapter = FlashcardAdapter(flashcards, this) // Pass 'this' as context
        viewPager.adapter = flashcardAdapter
    }
// END OF UPDATED LOGIC

}


