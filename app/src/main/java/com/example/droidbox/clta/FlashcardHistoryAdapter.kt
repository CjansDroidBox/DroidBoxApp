// Updated FlashcardHistoryAdapter.kt
package com.example.droidbox.clta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class FlashcardHistoryAdapter(
    private val historyList: List<FlashcardHistory>
) : RecyclerView.Adapter<FlashcardHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val actionTextView: TextView = view.findViewById(R.id.actionTextView)
        val sectionNameTextView: TextView = view.findViewById(R.id.sectionNameTextView)
        val previousNameTextView: TextView = view.findViewById(R.id.previousNameTextView)
        val dateTimeTextView: TextView = view.findViewById(R.id.dateTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.flashcard_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]

        holder.actionTextView.text = historyItem.action
        holder.sectionNameTextView.text = "Section: ${historyItem.sectionName}"
        holder.previousNameTextView.text =
            historyItem.previousName?.let { "Previous: $it" } ?: ""

        // Format the dateTime field
        val formattedDate = historyItem.dateTime.let { timestamp ->
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(timestamp.toDate())
        }

        holder.dateTimeTextView.text = formattedDate
    }

    override fun getItemCount(): Int = historyList.size
}
