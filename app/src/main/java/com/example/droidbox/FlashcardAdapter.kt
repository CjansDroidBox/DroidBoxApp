package com.example.droidbox

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlashcardAdapter(private val flashcards: List<Flashcard>, private val tts: TextToSpeech) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flashcard_item, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcards[position]
        holder.frontTextView.text = flashcard.title
        holder.backTextView.text = flashcard.description

        holder.flipButton.setOnClickListener {
            if (holder.frontSide.visibility == View.VISIBLE) {
                holder.frontSide.visibility = View.GONE
                holder.backSide.visibility = View.VISIBLE
            } else {
                holder.frontSide.visibility = View.VISIBLE
                holder.backSide.visibility = View.GONE
            }
        }

        holder.ttsTitleIcon.setOnClickListener {
            tts.speak(flashcard.title, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        holder.ttsDescriptionIcon.setOnClickListener {
            tts.speak(flashcard.description, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun getItemCount(): Int {
        return flashcards.size
    }

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frontTextView: TextView = itemView.findViewById(R.id.frontDeckTitle)
        val backTextView: TextView = itemView.findViewById(R.id.backDeckDescription)
        val flipButton: Button = itemView.findViewById(R.id.flipButton)
        val ttsTitleIcon: ImageView = itemView.findViewById(R.id.ttsTitleIcon)
        val ttsDescriptionIcon: ImageView = itemView.findViewById(R.id.ttsDescriptionIcon)
        val frontSide: View = itemView.findViewById(R.id.frontSide)
        val backSide: View = itemView.findViewById(R.id.backSide)
    }
}