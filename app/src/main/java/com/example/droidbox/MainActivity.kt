package com.example.droidbox

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.mainToolbar)
        val searchBar: EditText = findViewById(R.id.searchBar)
        val accountButton: ImageView = findViewById(R.id.accountButton)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val iconColor = ContextCompat.getColor(this, R.color.toolbarIconColor)
        accountButton.setColorFilter(iconColor)
        // Setup ViewPager Adapter
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Attach TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_home)
                    tab.contentDescription = "Home Tab"
                }
                1 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_leaderboard)
                    tab.contentDescription = "Leaderboard Tab"
                }
                2 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_quiz)
                    tab.contentDescription = "Quiz Tab"
                }
                3 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_flashcards)
                    tab.contentDescription = "Flashcards Tab"
                }
                4 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_videos)
                    tab.contentDescription = "Videos Tab"
                }
                5 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_documents)
                    tab.contentDescription = "Documents Tab"
                }
                6 -> {
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_shop)
                    tab.contentDescription = "Shop Tab"
                }
            }
        }.attach()

        // Handle Account Button Click
        accountButton.setOnClickListener {
            Toast.makeText(this, "Open Account Settings", Toast.LENGTH_SHORT).show()
            // Start the account settings activity
        }

        // Handle Search Bar Text Changes (Optional)
        searchBar.setOnEditorActionListener { textView, actionId, keyEvent ->
            val query = textView.text.toString()
            Toast.makeText(this, "Search: $query", Toast.LENGTH_SHORT).show()
            true
        }
    }
}
