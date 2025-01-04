package com.example.droidbox.clta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val notifications: MutableList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.notificationIcon)
        val title: TextView = view.findViewById(R.id.notificationTitle)
        val description: TextView = view.findViewById(R.id.notificationDescription)
        val ownerName: TextView = view.findViewById(R.id.notificationOwnerName)
        val time: TextView = view.findViewById(R.id.notificationTime)
        val type: TextView = view.findViewById(R.id.notificationType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.title.text = notification.title
        holder.description.text = notification.description
        holder.ownerName.text = "By: ${notification.ownerName}"
        holder.time.text = formatTimestamp(notification.timestamp)
        holder.type.text = notification.type // Display the type (e.g., Flashcard, Quiz)

        val textColor = if (notification.isRead) {
            ContextCompat.getColor(holder.itemView.context, R.color.readNotificationTextColor)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.unreadNotificationTextColor)
        }
        holder.title.setTextColor(textColor)

        holder.itemView.setOnClickListener {
            NotificationRepository.markNotificationAsRead(notification.id)
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
