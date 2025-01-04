package com.example.droidbox.clta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationFragment : Fragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    private var lastBackPressTime: Long = 0

    // Firestore listener
    private var notificationListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        notificationAdapter = NotificationAdapter(notifications)
        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerView.adapter = notificationAdapter

        // Fetch notifications with real-time updates
        listenForNotifications()

        // Handle back press for double-tap to close
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        parentFragmentManager.popBackStack()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(
                            requireContext(),
                            "Press back again to close notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun listenForNotifications() {
        notificationListener = NotificationRepository.listenForNotifications { fetchedNotifications ->
            notifications.clear()
            notifications.addAll(fetchedNotifications)
            notificationAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firestore listener when the fragment is destroyed
        notificationListener?.remove()
    }
}