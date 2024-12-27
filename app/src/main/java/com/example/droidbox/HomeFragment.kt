package com.example.droidbox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class HomeFragment : Fragment() {

    private lateinit var homeRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val postsList: MutableList<Post> = mutableListOf()
    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeRecyclerView = view.findViewById(R.id.homeRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Initialize RecyclerView
        postsAdapter = PostsAdapter(postsList) { post -> showDeleteConfirmationDialog(post) }
        homeRecyclerView.adapter = postsAdapter
        homeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Handle swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener {
            refreshPosts()
        }

        return view
    }

    private fun refreshPosts() {
        // Logic to fetch updated posts from the source
        postsAdapter.updateData(postsList)
        swipeRefreshLayout.isRefreshing = false
        Log.d("HomeFragment", "Posts refreshed")
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(post: Post) {
        postsList.remove(post)
        postsAdapter.updateData(postsList)
        Log.d("HomeFragment", "Post deleted: $post")
    }

    fun filterContent(query: String) {
        val filteredPosts = postsList.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
        postsAdapter.updateData(filteredPosts)
    }

    fun addPostFromOtherTabs(post: Post) {
        postsList.add(0, post)
        postsAdapter.updateData(postsList)
    }
}
