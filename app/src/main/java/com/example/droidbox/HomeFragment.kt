package com.example.droidbox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
            Post("Sample User 1", "This is a sample post", "10:30 AM", PostType.QUIZ),
            Post("Sample User 2", "Another sample post", "11:15 AM", PostType.VIDEO)
        )
        postsList.addAll(samplePosts)
        postsAdapter.notifyDataSetChanged() // Notify adapter to refresh the RecyclerView
    }

    private fun showAddPostDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_post, null)
        val postTitleInput = dialogView.findViewById<EditText>(R.id.postTitleInput)
        val postContentInput = dialogView.findViewById<EditText>(R.id.postContentInput)
        val postTypeSpinner = dialogView.findViewById<Spinner>(R.id.postTypeSpinner)

        // Create an AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create New Post")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val title = postTitleInput.text.toString()
                val content = postContentInput.text.toString()
                val postType = PostType.valueOf(postTypeSpinner.selectedItem.toString().uppercase()) // Get selected PostType

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    val newPost = Post(title, content, "12:00 PM", postType) // Create a new post
                    addNewPost(newPost) // Add the post to the RecyclerView
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show() // Show the dialog
    }

    private fun addNewPost(post: Post) {
        postsList.add(0, post) // Add new post at the top of the list
        postsAdapter.notifyItemInserted(0) // Notify adapter about the new post
        homeRecyclerView.scrollToPosition(0) // Scroll to the top to display the new post
        Log.d("PostsAdapter", "New Post Added: $post")
    }

    fun filterContent(query: String, postType: PostType? = null) {
        // Filter posts by query and type
        val filteredPosts = postsList.filter {
            (postType == null || it.type == postType) &&
                    (it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true))
        }
        postsAdapter.updateData(filteredPosts) // Update the adapter with filtered posts
    }
}
