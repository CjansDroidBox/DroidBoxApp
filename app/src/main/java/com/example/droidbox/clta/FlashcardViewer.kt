package com.example.droidbox.clta

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flashcard_viewer)

        // Initialize Views
        cardStackView = findViewById(R.id.cardStackView)
        flipButton = findViewById(R.id.flipCardButton)
        languageButton = findViewById(R.id.languageButton)
        ttsSpeedButton = findViewById(R.id.ttsSpeedButton)
        val reshuffleButton: Button = findViewById(R.id.reshuffleButton)
        cardProgressTextView = findViewById(R.id.cardProgress)

        // Initialize TTS
        flashcardTts = FlashcardTts(this)

        // Retrieve Flashcards
        val cardsList: ArrayList<Flashcard> =
            intent.getParcelableArrayListExtra("flashcards") ?: arrayListOf()

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

        // Adjust stacking behavior
        cardStackLayoutManager.setVisibleCount(5) // Show 5 cards in the stack
        cardStackLayoutManager.setTranslationInterval(8.0f) // Adjust vertical spacing for layers
        cardStackLayoutManager.setScaleInterval(0.9f) // Scale cards for a deck-like effect
        cardStackLayoutManager.setMaxDegree(0f) // Prevent rotation for a clean stack

        flashcardAdapter = FlashcardAdapter(cardsList, flashcardTts.getTts())
        cardStackView.layoutManager = cardStackLayoutManager
        cardStackView.adapter = flashcardAdapter

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
                    flashcardTts.changeLanguage(languages[which])
                    Toast.makeText(this, "Language set to ${languages[which]}", Toast.LENGTH_SHORT)
                        .show()
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
                    flashcardTts.adjustSpeechRate(rates[which])
                    Toast.makeText(this, "TTS speed set to ${speeds[which]}", Toast.LENGTH_SHORT)
                        .show()
                }
                .show()
        }
    }

    override fun onDestroy() {
        flashcardTts.shutdown()
        super.onDestroy()
    }
}
