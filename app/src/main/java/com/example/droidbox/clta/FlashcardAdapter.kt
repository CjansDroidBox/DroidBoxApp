package com.example.droidbox.clta

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlashcardAdapter(
    private val flashcards: List<Flashcard>,
    private val tts: TextToSpeech
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    private val flippedStates = MutableList(flashcards.size) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.flashcard_item, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcards[position]
        val isFlipped = flippedStates[position]

        // Bind data to views
        if (isFlipped) {
            holder.frontSide.visibility = View.GONE
            holder.backSide.visibility = View.VISIBLE
            holder.backDescription.text = flashcard.description
        } else {
            holder.frontSide.visibility = View.VISIBLE
            holder.backSide.visibility = View.GONE
            holder.frontTitle.text = flashcard.title
        }

        // Correctly map positions for stacking
        val stackPosition = itemCount - position - 0 // Reverse the stacking order

        // Apply stacking effect
        if (stackPosition > 1) {
            // Cards below the topmost card
            holder.itemView.translationY = stackPosition * 10f // Offset for stacking
            holder.itemView.elevation = stackPosition.toFloat() * 5 // Elevation for shadow
        } else {
            // Topmost card
            holder.itemView.translationY = 0f
            holder.itemView.elevation = itemCount.toFloat() * 0
        }

        // Handle TTS for front and back
        holder.ttsFrontIcon.setOnClickListener {
            tts.speak(flashcard.title, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        holder.ttsBackIcon.setOnClickListener {
            tts.speak(flashcard.description, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun getItemCount(): Int = flashcards.size

    fun flipCard(position: Int) {
        flippedStates[position] = !flippedStates[position]
        notifyItemChanged(position)
    }

    class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frontSide: View = itemView.findViewById(R.id.frontSide)
        val backSide: View = itemView.findViewById(R.id.backSide)
        val frontTitle: TextView = itemView.findViewById(R.id.frontDeckTitle)
        val backDescription: TextView = itemView.findViewById(R.id.backDeckDescription)
        val ttsFrontIcon: ImageView = itemView.findViewById(R.id.ttsFrontIcon)
        val ttsBackIcon: ImageView = itemView.findViewById(R.id.ttsBackIcon)
    }
}
