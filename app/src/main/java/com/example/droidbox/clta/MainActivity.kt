package com.example.droidbox.clta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var notificationButton: ImageView
    private lateinit var notificationBadge: TextView
    private var unreadNotificationsCount = 0

    private var homeFragment: HomeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.mainToolbar)
        val searchButton: ImageView = findViewById(R.id.searchButton)
        val accountButton: ImageView = findViewById(R.id.accountButton)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        searchView = findViewById(R.id.searchView)

        val iconColorDefault = ContextCompat.getColor(this, R.color.toolbarIconColor)
        val iconColorSelected = ContextCompat.getColor(this, R.color.selectedTabIconColor)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        initializeHomeFragment()

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_home)
                1 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_group_chat)
                2 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_leaderboard)
                3 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_quiz)
                4 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_flashcards)
                5 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_videos)
                6 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_documents)
                7 -> tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_shop)
            }
        }.attach()

        updateTabIcons(tabLayout, 0, iconColorSelected, iconColorDefault)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabIcons(tabLayout, position, iconColorSelected, iconColorDefault)
            }
        })

        accountButton.setOnClickListener {
            val intent = Intent(this, ProfileSettings::class.java)
            startActivity(intent)
        }

        searchButton.setOnClickListener {
            toggleSearchView()
        }

        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { sendSearchQueryToHomeFragment(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        Glide.with(this).load(R.drawable.ic_account).into(accountButton)
        Glide.with(this).load(R.drawable.ic_search).into(searchButton)

        notificationButton = findViewById(R.id.notificationButton)
        notificationBadge = findViewById(R.id.notificationBadge)

        notificationButton.setOnClickListener {
            openNotificationFragment()
        }

        fetchNotifications()
        fetchUnreadNotificationsCount()

    }

    private fun initializeHomeFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        homeFragment = HomeFragment()
        fragmentTransaction.add(R.id.fragment_container, homeFragment!!, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun toggleSearchView() {
        if (searchView.visibility == View.VISIBLE) {
            searchView.visibility = View.GONE
            searchView.clearFocus()
        } else {
            searchView.visibility = View.VISIBLE
            searchView.requestFocus()
        }
    }

    private fun sendSearchQueryToHomeFragment(query: String) {
        homeFragment?.filterContent(query) ?: Log.e("MainActivity", "HomeFragment is not initialized!")
    }

    private fun fetchNotifications() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e("MainActivity", "User is not authenticated")
            return
        }

        NotificationRepository.fetchUnreadNotificationsCount(currentUser.uid) { count ->
            unreadNotificationsCount = count
            updateNotificationBadge(count)
        }
    }

    private fun fetchUnreadNotificationsCount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return

        NotificationRepository.fetchUnreadNotificationsCount(currentUser.uid) { count ->
            runOnUiThread {
                updateNotificationBadge(count)
            }
        }
    }


    private fun updateNotificationBadge(count: Int) {
        if (count > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = count.toString()
        } else {
            notificationBadge.visibility = View.GONE
        }
    }

    private fun openNotificationFragment() {
        findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, NotificationFragment())
        transaction.addToBackStack(null)
        transaction.commit()
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
                tab?.icon?.setTint(iconColorSelected)
            } else {
                tab?.icon?.setTint(iconColorDefault)
            }
        }
    }
}
