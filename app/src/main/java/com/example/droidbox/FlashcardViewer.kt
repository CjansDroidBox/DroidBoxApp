package com.example.droidbox

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import java.util.Locale

class FlashcardViewer : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var cardStackView: CardStackView
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var flashcardAdapter: FlashcardAdapter
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flashcard_viewer)

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        // Initialize CardStackView and LayoutManager
        cardStackView = findViewById(R.id.cardStackView)
        cardStackLayoutManager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardSwiped(direction: Direction?) {
                Toast.makeText(this@FlashcardViewer, "Card swiped $direction", Toast.LENGTH_SHORT).show()
            }

            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: android.view.View?, position: Int) {}
            override fun onCardDisappeared(view: android.view.View?, position: Int) {}
        })

        // Retrieve flashcards from intent
        val cardsList = intent.getParcelableArrayListExtra<Flashcard>("flashcards") ?: arrayListOf()

        if (cardsList.isEmpty()) {
            Toast.makeText(this, "No flashcards to display!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up the adapter and layout manager
        flashcardAdapter = FlashcardAdapter(cardsList, tts)
        cardStackView.layoutManager = cardStackLayoutManager
        cardStackView.adapter = flashcardAdapter
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        } else {
            Toast.makeText(this, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}