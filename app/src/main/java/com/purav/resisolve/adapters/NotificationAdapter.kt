package com.purav.resisolve.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.purav.resisolve.databinding.ItemNotificationBinding
import com.purav.resisolve.models.AppNotification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val notifications: MutableList<AppNotification>,
    private val onNotificationClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    fun updateNotifications(newNotifications: List<AppNotification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) {
            binding.apply {
                tvNotificationTitle.text = notification.title
                tvNotificationMessage.text = notification.message
                tvNotificationTime.text = formatDate(notification.timestamp)

                // Highlight unread notifications
                root.alpha = if (notification.isRead) 0.6f else 1.0f

                root.setOnClickListener {
                    onNotificationClick(notification)
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}