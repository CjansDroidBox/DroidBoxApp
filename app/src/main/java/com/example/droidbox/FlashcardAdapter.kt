package com.example.droidbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlashcardAdapter(private val flashcards: List<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frontSide: View = itemView.findViewById(R.id.frontSide)
        val backSide: View = itemView.findViewById(R.id.backSide)
        val frontDeckTitle: TextView = itemView.findViewById(R.id.frontDeckTitle)
        val ttsTitleIcon: ImageView = itemView.findViewById(R.id.ttsTitleIcon)
        val backDeckDescription: TextView = itemView.findViewById(R.id.backDeckDescription)
        val ttsDescriptionIcon: ImageView = itemView.findViewById(R.id.ttsDescriptionIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flashcard_item, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcards[position]
        holder.frontDeckTitle.text = flashcard.title
        holder.backDeckDescription.text = flashcard.description

        // Flip logic
        holder.frontSide.setOnClickListener {
            holder.frontSide.visibility = View.GONE
            holder.backSide.visibility = View.VISIBLE
        }
        holder.backSide.setOnClickListener {
            holder.backSide.visibility = View.GONE
            holder.frontSide.visibility = View.VISIBLE
        }

        // Text-to-Speech (if implemented)
        holder.ttsTitleIcon.setOnClickListener {
            // Implement Text-to-Speech logic for the front title
        }
        holder.ttsDescriptionIcon.setOnClickListener {
            // Implement Text-to-Speech logic for the back description
        }
    }

    override fun getItemCount(): Int = flashcards.size
}
