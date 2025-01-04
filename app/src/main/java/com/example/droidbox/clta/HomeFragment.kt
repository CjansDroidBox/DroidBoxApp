package com.example.droidbox.clta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SharedContentAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val filteredList = mutableListOf<HomeContent>()


    private val contentList = mutableListOf<HomeContent>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SharedContentAdapter(
            contentList,
            onLikeClick = { sharedContentId -> handleLikeClick(sharedContentId) },
            onCommentClick = { sharedContentId -> handleCommentClick(sharedContentId) },
            onShareClick = { sharedContentId -> handleShareClick(sharedContentId) },
            onDownloadClick = { sharedContentId -> handleDownloadClick(sharedContentId) }
        )
        recyclerView.adapter = adapter

        loadContentFromFirestore()

        swipeRefreshLayout.setOnRefreshListener { loadContentFromFirestore() }

        return view
    }

    fun filterContent(query: String) {
        val lowercaseQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(
            contentList.filter {
                it.title.lowercase().contains(lowercaseQuery) ||
                        it.description.lowercase().contains(lowercaseQuery)
            }
        )
        adapter.notifyDataSetChanged()
        showEmptyState(filteredList.isEmpty())
    }


    private fun loadContentFromFirestore() {
        FirebaseFirestore.getInstance().collection("shared_content")
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                swipeRefreshLayout.isRefreshing = false
                if (snapshot.isEmpty) {
                    showEmptyState(true)
                    return@addOnSuccessListener
                }

                val fetchedContent = snapshot.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: return@mapNotNull null
                    val description = document.getString("description") ?: ""
                    val type = document.getString("type") ?: "Unknown"
                    val sharedContentId = document.id

                    HomeContent(title, description, type, sharedContentId)
                }

                contentList.clear()
                contentList.addAll(fetchedContent)
                adapter.notifyDataSetChanged()
                showEmptyState(contentList.isEmpty())
            }
            .addOnFailureListener { e ->
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Failed to load shared content.", Toast.LENGTH_SHORT).show()
                showEmptyState(true)
            }
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateTextView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun handleLikeClick(sharedContentId: String) {
        Toast.makeText(requireContext(), "Liked content ID: $sharedContentId", Toast.LENGTH_SHORT).show()
    }

    private fun handleCommentClick(sharedContentId: String) {
        Toast.makeText(requireContext(), "Commented on content ID: $sharedContentId", Toast.LENGTH_SHORT).show()
    }

    private fun handleShareClick(sharedContentId: String) {
        Toast.makeText(requireContext(), "Shared content ID: $sharedContentId", Toast.LENGTH_SHORT).show()
    }

    private fun handleDownloadClick(sharedContentId: String) {
        Toast.makeText(requireContext(), "Downloaded content ID: $sharedContentId", Toast.LENGTH_SHORT).show()
    }
}
