package com.example.droidbox.clta

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

object NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun fetchNotifications(callback: (List<Notification>) -> Unit) {
        firestore.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifications = snapshot.documents.mapNotNull { parseNotification(it) }
                callback(notifications)
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepository", "Failed to fetch notifications: ${e.message}")
                callback(emptyList())
            }
    }

    fun listenForNotifications(callback: (List<Notification>) -> Unit): ListenerRegistration {
        return firestore.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "Listen failed: ${error.message}")
                    callback(emptyList())
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { parseNotification(it) } ?: emptyList()
                callback(notifications)
            }
    }

    private fun parseNotification(document: DocumentSnapshot): Notification? {
        return try {
            val id = document.id
            val title = document.getString("title") ?: ""
            val description = document.getString("description") ?: ""
            val ownerName = document.getString("ownerName") ?: "Unknown"
            val sharedContentId = document.getString("sharedContentId") ?: ""
            val timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
            val isRead = document.getBoolean("isRead") ?: false
            val type = document.getString("type") ?: "Unknown"

            Notification(
                id = id,
                title = title,
                description = description,
                ownerName = ownerName,
                sharedContentId = sharedContentId,
                timestamp = timestamp,
                isRead = isRead,
                type = type
            )
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error parsing notification: ${e.message}")
            null
        }
    }

    fun fetchUnreadNotificationsCount(callback: (Int) -> Unit) {
        firestore.collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val unreadCount = snapshot.size()
                callback(unreadCount)
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepository", "Failed to fetch unread notifications count: ${e.message}")
                callback(0)
            }
    }


    fun markNotificationAsRead(notificationId: String) {
        firestore.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("NotificationRepository", "Notification marked as read.")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepository", "Failed to mark notification as read: ${e.message}")
            }
    }

}
