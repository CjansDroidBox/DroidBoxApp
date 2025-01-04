package com.example.droidbox.clta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SharedContentAdapter(
    private val contentList: List<HomeContent>,
    private val onLikeClick: (String) -> Unit, // Callback for like action
    private val onCommentClick: (String) -> Unit, // Callback for comment action
    private val onShareClick: (String) -> Unit, // Callback for share action
    private val onDownloadClick: (String) -> Unit // Callback for download action
) : RecyclerView.Adapter<SharedContentAdapter.SharedContentViewHolder>() {

    inner class SharedContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.sharedContentTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.sharedContentDescription)
        val typeTextView: TextView = view.findViewById(R.id.sharedContentType)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val commentButton: ImageView = view.findViewById(R.id.commentButton)
        val shareButton: ImageView = view.findViewById(R.id.shareButton)
        val downloadButton: ImageView = view.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_content, parent, false)
        return SharedContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: SharedContentViewHolder, position: Int) {
        val content = contentList[position]

        holder.titleTextView.text = content.title
        holder.descriptionTextView.text = content.description
        holder.typeTextView.text = content.type

        // Handle button interactions
        holder.likeButton.setOnClickListener {
            onLikeClick(content.sharedContentId)
        }
        holder.commentButton.setOnClickListener {
            onCommentClick(content.sharedContentId)
        }
        holder.shareButton.setOnClickListener {
            onShareClick(content.sharedContentId)
        }
        holder.downloadButton.setOnClickListener {
            onDownloadClick(content.sharedContentId)
        }
    }

    override fun getItemCount(): Int = contentList.size
}
