package com.example.droidbox.clta

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


object NotificationRepository {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun fetchUnreadNotificationsCount(userId: String, callback: (Int) -> Unit) {
        firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.size())
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepository", "Failed to fetch unread count: ${e.message}")
                callback(0)
            }
    }

    fun fetchNotifications(userId: String, callback: (List<Notification>) -> Unit) {
        firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifications = snapshot.documents.mapNotNull { document ->
                    try {
                        val id = document.id
                        val message = document.getString("message") ?: return@mapNotNull null
                        val timestamp = document.getTimestamp("timestamp") ?: return@mapNotNull null
                        val isRead = document.getBoolean("isRead") ?: false
                        Notification(id, message, timestamp, isRead)
                    } catch (e: Exception) {
                        Log.e("NotificationRepository", "Error parsing notification: ${e.message}")
                        null
                    }
                }
                callback(notifications)
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepository", "Failed to fetch notifications: ${e.message}")
                callback(emptyList())
            }
    }


    fun markNotificationAsRead(notificationId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("Notification", "Notification marked as read")
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "Failed to mark as read: ${e.message}")
            }
    }


}
