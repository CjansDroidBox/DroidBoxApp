package com.example.droidbox.clta

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class FlashcardViewer : AppCompatActivity() {

    private lateinit var cardStackView: CardStackView
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var flashcardAdapter: FlashcardAdapter
    private lateinit var flashcardTts: FlashcardTts

    private lateinit var flipButton: Button
    private lateinit var languageButton: Button
    private lateinit var ttsSpeedButton: Button
    private lateinit var cardProgressTextView: TextView

    private var currentCardPosition = 0
    private var totalCards = 0

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userUID: String
    private var selectedLanguage: String = "English"
    private var selectedTtsSpeed: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flashcard_viewer)

        // Initialize Firestore and User ID
        firestore = FirebaseFirestore.getInstance()
        userUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userUID.isEmpty()) {
            Toast.makeText(this, "User not logged in. Preferences cannot be loaded.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize Views
        cardStackView = findViewById(R.id.cardStackView)
        flipButton = findViewById(R.id.flipCardButton)
        languageButton = findViewById(R.id.languageButton)
        ttsSpeedButton = findViewById(R.id.ttsSpeedButton)
        val reshuffleButton: Button = findViewById(R.id.reshuffleButton)
        cardProgressTextView = findViewById(R.id.cardProgress)
        val flashcardTitleTextView = findViewById<TextView>(R.id.flashcardTitle) // Initialize the TextView

// Retrieve Flashcards and Section Name
        val cardsList: ArrayList<Flashcard> =
            intent.getParcelableArrayListExtra("flashcards") ?: arrayListOf()
        val sectionName = intent.getStringExtra("sectionName") ?: "Untitled Section"

// Set the section name in the title TextView
        flashcardTitleTextView.text = sectionName


        if (cardsList.isEmpty()) {
            Toast.makeText(this, "No flashcards to display!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Shuffle the deck
        cardsList.shuffle()
        totalCards = cardsList.size
        cardProgressTextView.text = "1 out of $totalCards"

        // Configure CardStackLayoutManager
        cardStackLayoutManager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardSwiped(direction: Direction?) {
                currentCardPosition++
                if (currentCardPosition < totalCards) {
                    cardProgressTextView.text = "${currentCardPosition + 1} out of $totalCards"
                } else {
                    cardProgressTextView.text = "No cards left"
                }
            }

            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: android.view.View?, position: Int) {}
            override fun onCardDisappeared(view: android.view.View?, position: Int) {}
        })

        cardStackLayoutManager.setVisibleCount(5)
        cardStackLayoutManager.setTranslationInterval(8.0f)
        cardStackLayoutManager.setScaleInterval(0.9f)
        cardStackLayoutManager.setMaxDegree(0f)

        // Fetch preferences, then initialize TTS and the adapter
        fetchPreferencesFromFirestore {
            flashcardTts = FlashcardTts(this, selectedLanguage, selectedTtsSpeed)

            // Initialize the adapter after TTS is initialized
            flashcardAdapter = FlashcardAdapter(cardsList, flashcardTts.getTts())
            cardStackView.layoutManager = cardStackLayoutManager
            cardStackView.adapter = flashcardAdapter
        }

        // Reshuffle button
        reshuffleButton.setOnClickListener {
            cardsList.shuffle()
            currentCardPosition = 0
            cardProgressTextView.text = "1 out of $totalCards"
            flashcardAdapter.notifyDataSetChanged()
        }

        // Flip button
        flipButton.setOnClickListener {
            flashcardAdapter.flipCard(currentCardPosition)
        }

        // Language Selection
        languageButton.setOnClickListener {
            val languages = flashcardTts.supportedLanguages.keys.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages) { _, which ->
                    val newLanguage = languages[which]
                    flashcardTts.changeLanguage(newLanguage)
                    selectedLanguage = newLanguage
                    savePreferencesToFirestore(newLanguage, selectedTtsSpeed)
                }
                .show()
        }

        // TTS Speed Adjustment
        ttsSpeedButton.setOnClickListener {
            val speeds = arrayOf("Slow", "Normal", "Fast")
            val rates = arrayOf(0.5f, 1.0f, 1.5f)
            AlertDialog.Builder(this)
                .setTitle("Select TTS Speed")
                .setItems(speeds) { _, which ->
                    val newSpeed = rates[which]
                    flashcardTts.adjustSpeechRate(newSpeed)
                    selectedTtsSpeed = newSpeed
                    savePreferencesToFirestore(selectedLanguage, newSpeed)
                }
                .show()
        }
    }

    private fun fetchPreferencesFromFirestore(onPreferencesLoaded: () -> Unit) {
        firestore.collection("users").document(userUID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val preferences = document.get("preferences") as? Map<*, *>
                    if (preferences != null) {
                        selectedLanguage = preferences["language"] as? String ?: "English"
                        selectedTtsSpeed = when (val speed = preferences["ttsSpeed"]) {
                            is Double -> speed.toFloat()
                            is String -> speed.toFloatOrNull() ?: 1.0f
                            else -> 1.0f
                        }
                    }
                }
                onPreferencesLoaded()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to load preferences: ${it.message}")
                Toast.makeText(this, "Failed to load preferences.", Toast.LENGTH_SHORT).show()
                onPreferencesLoaded()
            }
    }

    private fun savePreferencesToFirestore(language: String, ttsSpeed: Float) {
        val preferences = mapOf(
            "language" to language,
            "ttsSpeed" to ttsSpeed
        )
        firestore.collection("users").document(userUID)
            .set(mapOf("preferences" to preferences), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Preferences saved: Language=$language, TTS Speed=$ttsSpeed")
                Toast.makeText(this, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to save preferences: ${it.message}")
                Toast.makeText(this, "Failed to save preferences.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        flashcardTts.shutdown()
    }

}
