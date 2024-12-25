package com.example.droidbox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class HomeFragment : Fragment() {

    private lateinit var homeRecyclerView: RecyclerView
    private lateinit var newPostButton: FloatingActionButton
    private val postsList: MutableList<Post> = mutableListOf() // Holds the list of posts
    private lateinit var postsAdapter: PostsAdapter // Adapter for RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeRecyclerView = view.findViewById(R.id.homeRecyclerView)
        newPostButton = view.findViewById(R.id.newPostButton)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Initialize RecyclerView and Adapter
        postsAdapter = PostsAdapter(postsList)
        homeRecyclerView.adapter = postsAdapter
        homeRecyclerView.layoutManager = LinearLayoutManager(context)

        // Handle New Post Button Click
        newPostButton.setOnClickListener {
            showAddPostDialog() // Show dialog to create a new post
        }

        swipeRefreshLayout.setOnRefreshListener {
            refreshPosts() // Refresh the RecyclerView
        }

        return view
    }

    private fun refreshPosts() {
        // Reset the adapter's data to the full posts list
        postsAdapter.updateData(postsList)
        swipeRefreshLayout.isRefreshing = false // Stop the refreshing animation
        Log.d("HomeFragment", "Posts refreshed: $postsList")
    }

    private fun showAddPostDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_post, null)
        val postTitleInput = dialogView.findViewById<EditText>(R.id.postTitleInput)
        val postContentInput = dialogView.findViewById<EditText>(R.id.postContentInput)
        val postLinkInput = dialogView.findViewById<EditText>(R.id.postLinkInput)
        val submitPostButton = dialogView.findViewById<Button>(R.id.submitPostButton)

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Handle Submit Button Click
        submitPostButton.setOnClickListener {
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
                dialog.dismiss() // Dismiss the dialog after submitting
            } else {
                Toast.makeText(requireContext(), "Title and content cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        dialog.show()
    }

    private fun addNewPost(post: Post) {
        postsList.add(0, post) // Add the new post to the top of the list
        postsAdapter.notifyItemInserted(0) // Notify adapter about the new post
        homeRecyclerView.scrollToPosition(0) // Scroll to the top to display the new post
        Log.d("HomeFragment", "New Post Added to List: $post")
    }

    private fun getCurrentTime(): String {
        // Placeholder for getting the current time
        return "12:00 PM"
    }

    fun filterContent(query: String) {
        // Filter posts by query
        val filteredPosts = postsList.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
        postsAdapter.updateData(filteredPosts) // Update the adapter with filtered posts
    }
}
