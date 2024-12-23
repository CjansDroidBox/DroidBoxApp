package com.example.droidbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class HomeFragment : Fragment() {

    private lateinit var contentTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        contentTextView = view.findViewById(R.id.contentTextView)
        return view
    }

    fun filterContent(query: String) {
        // Implement your filtering logic here
        contentTextView.text = "Showing results for: $query"
    }
}
