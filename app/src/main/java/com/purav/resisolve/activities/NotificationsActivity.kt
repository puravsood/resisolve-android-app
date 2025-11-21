package com.purav.resisolve.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.purav.resisolve.adapters.NotificationAdapter
import com.purav.resisolve.databinding.ActivityNotificationsBinding
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val repository = FirebaseRepository()
    private val notificationAdapter = NotificationAdapter(mutableListOf()) { notification ->
        // Mark as read when clicked
        markAsRead(notification.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = notificationAdapter
        }
    }

    private fun loadNotifications() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val result = repository.getNotifications(userId)
                result.onSuccess { notifications ->
                    if (notifications.isEmpty()) {
                        binding.rvNotifications.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvNotifications.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        notificationAdapter.updateNotifications(notifications)
                    }
                }.onFailure {
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Failed to load notifications",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun markAsRead(notificationId: String) {
        lifecycleScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }
}