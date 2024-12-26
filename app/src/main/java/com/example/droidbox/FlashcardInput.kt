package com.example.droidbox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FlashcardInput : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flashcard_input)

        val inputField = findViewById<EditText>(R.id.flashcardInputField)
        val saveButton = findViewById<Button>(R.id.flashcardInputSaveButton)

        // Retrieve the selected section name from the intent
        val selectedSectionName = intent.getStringExtra("selectedSectionName")

        saveButton.setOnClickListener {
            val inputData = inputField.text.toString().trim()
            if (inputData.isNotEmpty()) {
                val flashcards = inputData.lines().mapNotNull { line ->
                    val parts = line.split(":")
                    if (parts.size == 2) Flashcard(parts[0].trim(), parts[1].trim()) else null
                }

                // Pass the flashcards back to FlashcardsFragment
                val resultIntent = Intent().apply {
                    putParcelableArrayListExtra("flashcards", ArrayList(flashcards))
                    putExtra("selectedSectionName", selectedSectionName)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Input field cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
