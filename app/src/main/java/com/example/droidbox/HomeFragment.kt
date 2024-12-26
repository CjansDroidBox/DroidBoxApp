package com.example.droidbox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< HEAD
import android.widget.EditText
import android.widget.Toast
=======
>>>>>>> f2c4932 (Remove DroidBoxApp directory from Git tracking)
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.jsoup.Jsoup


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

<<<<<<< HEAD
    private fun showAddPostDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_post, null)
        val postTitleInput = dialogView.findViewById<EditText>(R.id.postTitleInput)
        val postContentInput = dialogView.findViewById<EditText>(R.id.postContentInput)
        val postLinkInput = dialogView.findViewById<EditText>(R.id.postLinkInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val title = postTitleInput.text.toString().trim()
                val content = postContentInput.text.toString().trim()
                val link = postLinkInput.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    if (link.isNotEmpty()) {
                        fetchMetadata(link) { metadata ->
                            val newPost = Post(
                                title = metadata.title ?: title,
                                content = if (metadata.description != null) "${content}\n\n${metadata.description}" else content,
                                timestamp = getCurrentTime(),
                                imageUrl = metadata.imageUrl
                            )
                            addNewPost(newPost)
                        }
                    } else {
                        val newPost = Post(
                            title = title,
                            content = content,
                            timestamp = getCurrentTime(),
                            imageUrl = null
                        )
                        addNewPost(newPost)
                    }
                } else {
                    Toast.makeText(requireContext(), "Title and content cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    class PostDiffCallback(
        private val oldList: List<Post>,
        private val newList: List<Post>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].timestamp == newList[newItemPosition].timestamp
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private fun fetchMetadata(url: String, callback: (Metadata) -> Unit) {
        Thread {
            try {
                val document = Jsoup.connect(url).get()
                val title = document.select("meta[property=og:title]").attr("content")
                val description = document.select("meta[property=og:description]").attr("content")
                val imageUrl = document.select("meta[property=og:image]").attr("content")
                val metadata = Metadata(title, description, imageUrl)

                requireActivity().runOnUiThread {
                    callback(metadata) // Pass the metadata to the callback
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val emptyMetadata = Metadata(null, null, null)

                requireActivity().runOnUiThread {
                    callback(emptyMetadata) // Pass empty metadata on error
                }
            }
        }.start()
=======
    private fun showDeleteConfirmationDialog(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Cancel", null)
            .show()
>>>>>>> f2c4932 (Remove DroidBoxApp directory from Git tracking)
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
