
package com.example.droidbox

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class FlashcardAdapter(
    private val flashcards: List<Flashcard>,
    context: Context
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this) // Use context as a property

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

        // Text-to-Speech logic
        holder.ttsTitleIcon.setOnClickListener {
            speakText(holder.frontDeckTitle.text.toString())
        }
        holder.ttsDescriptionIcon.setOnClickListener {
            speakText(holder.backDeckDescription.text.toString())
        }
    }

    override fun getItemCount(): Int = flashcards.size

    private fun speakText(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        tts.shutdown()
    }
}
// END OF UPDATED LOGIC
