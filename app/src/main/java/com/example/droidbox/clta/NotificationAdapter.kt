package com.example.droidbox.clta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val notifications: MutableList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.notificationIcon)
        val message: TextView = view.findViewById(R.id.notificationMessage)
        val time: TextView = view.findViewById(R.id.notificationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        // Update the message text
        holder.message.text = notification.message
        holder.time.text = formatTimestamp(notification.timestamp)

        // Change text color based on read/unread status
        val textColor = if (notification.isRead) {
            ContextCompat.getColor(holder.itemView.context, R.color.readNotificationTextColor)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.unreadNotificationTextColor)
        }
        holder.message.setTextColor(textColor)

        holder.itemView.setOnClickListener {
            // Show a toast for the clicked notification
            Toast.makeText(
                holder.itemView.context,
                "Notification clicked: ${notification.message}",
                Toast.LENGTH_SHORT
            ).show()

            // Mark the notification as read in Firestore
            NotificationRepository.markNotificationAsRead(notification.id)

            // Update the local list and notify RecyclerView
            val updatedNotification = notification.copy(isRead = true)
            notifications[position] = updatedNotification
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = notifications.size

    private fun formatTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}
