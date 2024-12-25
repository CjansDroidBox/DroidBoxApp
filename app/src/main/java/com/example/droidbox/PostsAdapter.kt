package com.example.droidbox


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostsAdapter(
    private var posts: List<Post>
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postTitle: TextView = view.findViewById(R.id.postTitle)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val postTimestamp: TextView = view.findViewById(R.id.postTimestamp)
        val postImageView: ImageView = view.findViewById(R.id.postImageView) // Added the ImageView reference
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

        if (!post.imageUrl.isNullOrEmpty()) {
            holder.postImageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .placeholder(R.drawable.placeholder_image) // Add a placeholder drawable
                .error(R.drawable.error_image) // Add an error drawable
                .into(holder.postImageView)

        } else {
            holder.postImageView.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        val diffCallback = HomeFragment.PostDiffCallback(posts, newPosts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        posts = newPosts
        diffResult.dispatchUpdatesTo(this)
        Log.d("PostsAdapter", "Updating Data: $newPosts")
    }

}
