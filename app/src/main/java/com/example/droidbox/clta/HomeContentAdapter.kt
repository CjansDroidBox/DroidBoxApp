package com.example.droidbox.clta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeContentAdapter(private val contentList: MutableList<HomeContent>) :
    RecyclerView.Adapter<HomeContentAdapter.ContentViewHolder>() {

    inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.contentTitle)
        val description: TextView = view.findViewById(R.id.contentDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_content, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val content = contentList[position]
        holder.title.text = content.title
        holder.description.text = content.description
    }

    override fun getItemCount(): Int = contentList.size

    // Update the content list dynamically
    fun updateContent(newContent: List<HomeContent>) {
        contentList.clear()
        contentList.addAll(newContent)
        notifyDataSetChanged()
    }
}
