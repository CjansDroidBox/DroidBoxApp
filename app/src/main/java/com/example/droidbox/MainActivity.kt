package com.example.droidbox

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.mainToolbar)
        val searchButton: ImageView = findViewById(R.id.searchButton)
        val accountButton: ImageView = findViewById(R.id.accountButton)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        searchView = findViewById(R.id.searchView)

        val iconColorDefault = ContextCompat.getColor(this, R.color.toolbarIconColor) // Default color
        val iconColorSelected = ContextCompat.getColor(this, R.color.selectedTabIconColor) // Selected tab color

        // Setup ViewPager Adapter
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Attach TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_home)
                1 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_group_chat)
                2->  tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_leaderboard)
                3 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_quiz)
                4 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_flashcards)
                5 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_videos)
                6 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_documents)
                7 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_shop)
            }
        }.attach()

        // Handle Tab Icon Tint for Both Selection and Scroll
        updateTabIcons(tabLayout, 0, iconColorSelected, iconColorDefault) // Initialize first tab
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabIcons(tabLayout, position, iconColorSelected, iconColorDefault)
            }
        })

        // Handle Account Button Click
        accountButton.setOnClickListener {
            Toast.makeText(this, "Open Account Settings", Toast.LENGTH_SHORT).show()
        }

        // Handle Search Icon Click
        searchButton.setOnClickListener {
            toggleSearchView()
        }

        // Handle Search Query Submission
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    sendSearchQueryToHomeFragment(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun toggleSearchView() {
        if (searchView.visibility == View.GONE) {
            searchView.visibility = View.VISIBLE
        } else {
            searchView.visibility = View.GONE
        }
    }

    private fun sendSearchQueryToHomeFragment(query: String) {
        val currentFragment =
            supportFragmentManager.findFragmentByTag("f" + findViewById<ViewPager2>(R.id.viewPager).currentItem)
        if (currentFragment is HomeFragment) {
            currentFragment.filterContent(query)
        }
    }

    private fun updateTabIcons(
        tabLayout: TabLayout,
        selectedPosition: Int,
        iconColorSelected: Int,
        iconColorDefault: Int
    ) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (i == selectedPosition) {
                tab?.icon?.setTint(iconColorSelected) // Selected Tab
            } else {
                tab?.icon?.setTint(iconColorDefault) // Unselected Tabs
            }
        }
    }
}
