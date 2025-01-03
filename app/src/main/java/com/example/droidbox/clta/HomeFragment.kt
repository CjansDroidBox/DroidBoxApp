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
    private lateinit var adapter: HomeContentAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val contentList = mutableListOf<HomeContent>() // List for content
    private val filteredList = mutableListOf<HomeContent>() // Filtered content

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HomeContentAdapter(filteredList)
        recyclerView.adapter = adapter

        loadContentFromFirestore()

        swipeRefreshLayout.setOnRefreshListener { loadContentFromFirestore() }

        return view
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
                    val description = document.getString("description") ?: return@mapNotNull null
                    HomeContent(title, description)
                }

                contentList.clear()
                contentList.addAll(fetchedContent)
                filteredList.clear()
                filteredList.addAll(contentList)
                adapter.notifyDataSetChanged()
                showEmptyState(filteredList.isEmpty())
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
}
