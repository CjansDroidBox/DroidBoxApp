package com.example.droidbox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeRecyclerView: RecyclerView
    private lateinit var newPostButton: FloatingActionButton
    private val postsList: MutableList<Post> = mutableListOf() // Holds the list of posts
    private lateinit var postsAdapter: PostsAdapter // Adapter for RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeRecyclerView = view.findViewById(R.id.homeRecyclerView)
        newPostButton = view.findViewById(R.id.newPostButton)

        // Initialize RecyclerView
        postsAdapter = PostsAdapter(postsList)
        homeRecyclerView.adapter = postsAdapter

        // Load initial sample data
        loadSampleData()

        // Handle New Post Button Click
        newPostButton.setOnClickListener {
            showAddPostDialog() // Show dialog to create a new post
        }

        return view
    }

    private fun loadSampleData() {
        // Add sample posts (can be replaced with backend/database logic)
        val samplePosts = listOf(
            Post("Sample Title 1", "This is a sample post content", "10:30 AM"),
            Post("Sample Title 2", "Another sample post content", "11:15 AM")
        )
        postsList.addAll(samplePosts)
        postsAdapter.notifyDataSetChanged() // Notify adapter to refresh the RecyclerView
    }

    private fun showAddPostDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_post, null)
        val postTitleInput = dialogView.findViewById<EditText>(R.id.postTitleInput)
        val postContentInput = dialogView.findViewById<EditText>(R.id.postContentInput)
        val postLinkInput = dialogView.findViewById<EditText>(R.id.postLinkInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create New Post")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val title = postTitleInput.text.toString().trim()
                val content = postContentInput.text.toString().trim()
                val link = postLinkInput.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    val newPost = Post(
                        title = title,
                        content = if (link.isNotEmpty()) "$content\n\nLink: $link" else content,
                        timestamp = getCurrentTime()
                    )
                    addNewPost(newPost)
                } else {
                    Toast.makeText(requireContext(), "Title and content cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun getCurrentTime(): String {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return currentTime.format(Date())
    }

    private fun addNewPost(post: Post) {
        postsList.add(0, post)
        postsAdapter.notifyItemInserted(0)
        homeRecyclerView.scrollToPosition(0)
        Log.d("PostsAdapter", "New Post Added: $post")
    }

    fun filterContent(query: String) {
        // Filter posts by query
        val filteredPosts = postsList.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
        postsAdapter.updateData(filteredPosts) // Update the adapter with filtered posts
    }
}
