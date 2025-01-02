package com.example.droidbox.clta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationFragment : Fragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        // Initialize RecyclerView
        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        notificationAdapter = NotificationAdapter(notifications)
        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerView.adapter = notificationAdapter

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Fetch notifications
        fetchNotifications()

        return view
    }

    private fun fetchNotifications() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("NotificationFragment", "User is not authenticated")
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetchedNotifications = snapshot.documents.mapNotNull { document ->
                    val message = document.getString("message") ?: return@mapNotNull null
                    val timestamp = document.getTimestamp("timestamp") ?: return@mapNotNull null
                    val isRead = document.getBoolean("isRead") ?: false

                    Notification(message, timestamp, isRead)
                }
                notifications.clear()
                notifications.addAll(fetchedNotifications)
                notificationAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("NotificationFragment", "Failed to fetch notifications: ${e.message}")
            }
    }
}
