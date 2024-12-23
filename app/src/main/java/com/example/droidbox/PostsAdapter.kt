package com.example.droidbox

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostsAdapter(
    private var posts: List<Post>
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postTitle: TextView = view.findViewById(R.id.postTitle)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val postTimestamp: TextView = view.findViewById(R.id.postTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        holder.postContent.text = post.content
        holder.postTimestamp.text = post.timestamp
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
        Log.d("PostsAdapter", "Updating Data: $newPosts")
    }
}
