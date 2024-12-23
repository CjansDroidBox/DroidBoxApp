package com.example.droidbox

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 8 // Total tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> GroupChatFragment()
            2 -> LeaderboardFragment()
            3 -> QuizFragment()
            4 -> FlashcardsFragment()
            5 -> VideosFragment()
            6 -> DocumentsFragment()
            7 -> ShopFragment()
            else -> HomeFragment()
        }
    }
}
