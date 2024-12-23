package com.example.droidbox

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 7 // Total tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> LeaderboardFragment()
            2 -> QuizFragment()
            3 -> FlashcardsFragment()
            4 -> VideosFragment()
            5 -> DocumentsFragment()
            6 -> ShopFragment()
            else -> HomeFragment()
        }
    }
}
