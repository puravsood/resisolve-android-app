package com.purav.resisolve.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.purav.resisolve.databinding.ActivityIssueDetailBinding
import com.purav.resisolve.models.Issue
import com.purav.resisolve.models.AppNotification
import com.purav.resisolve.models.TimelineEvent
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class IssueDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIssueDetailBinding
    private val repository = FirebaseRepository()
    private lateinit var issueId: String
    private var isAdmin = false
    private lateinit var currentIssue: Issue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIssueDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        issueId = intent.getStringExtra("ISSUE_ID") ?: ""
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        setupToolbar()
        setupListeners()
        loadIssueDetails()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        if (isAdmin) {
            binding.layoutAdminActions.visibility = View.VISIBLE
            binding.layoutResidentActions.visibility = View.GONE

            binding.btnSaveAndNotify.setOnClickListener {
                updateIssueStatus(true)
            }

            binding.btnSaveWithoutNotification.setOnClickListener {
                updateIssueStatus(false)
            }
        } else {
            binding.layoutAdminActions.visibility = View.GONE
            binding.layoutResidentActions.visibility = View.VISIBLE

            binding.btnCancelIssue.setOnClickListener {
                showCancelConfirmation()
            }
        }
    }

    private fun loadIssueDetails() {
        lifecycleScope.launch {
            val result = repository.getIssue(issueId)
            result.onSuccess { issue ->
                currentIssue = issue
                displayIssueDetails(issue)
            }.onFailure {
                Toast.makeText(
                    this@IssueDetailActivity,
                    "Failed to load issue details",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun displayIssueDetails(issue: Issue) {
        // Header
        binding.tvIssueTitle.text = issue.title
        binding.chipStatus.text = issue.status
        binding.chipPriority.text = issue.priority

        // Details
        binding.tvCategory.text = issue.category
        binding.tvReportedDate.text = formatDate(issue.createdAt)
        binding.tvDescription.text = issue.description

        if (issue.location.isNotEmpty()) {
            binding.tvLocation.visibility = View.VISIBLE
            binding.tvLocation.text = "Location: ${issue.location}"
        }

        // Reporter info (visible to admin)
        if (isAdmin) {
            binding.layoutReporterInfo.visibility = View.VISIBLE
            binding.tvReporterName.text = issue.reportedByName
            binding.tvReporterFlat.text = "Flat ${issue.reportedByFlat}"
        }

        // Timeline
        displayTimeline(issue.timeline)

        // Admin notes (if any)
        if (issue.adminNotes.isNotEmpty()) {
            binding.tvAdminNotes.visibility = View.VISIBLE
            binding.tvAdminNotes.text = "Admin Notes: ${issue.adminNotes}"
        }
    }

    private fun displayTimeline(timeline: List<TimelineEvent>) {
        val timelineText = StringBuilder()
        timeline.sortedByDescending { it.timestamp }.forEach { event ->
            timelineText.append("${event.status}\n")
            timelineText.append("${event.description}\n")
            timelineText.append("${formatDate(event.timestamp)}\n\n")
        }
        binding.tvTimeline.text = timelineText.toString()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun updateIssueStatus(notify: Boolean) {
        val newStatus = binding.spinnerStatus.selectedItem.toString()
        val adminNotes = binding.etAdminNotes.text.toString().trim()

        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "adminNotes" to adminNotes
        )

        lifecycleScope.launch {
            // Update issue
            val updateResult = repository.updateIssue(issueId, updates)

            updateResult.onSuccess {
                // Add timeline event
                val event = TimelineEvent(
                    status = newStatus,
                    description = adminNotes.ifEmpty { "Status updated to $newStatus" },
                    timestamp = System.currentTimeMillis(),
                    by = "Admin"
                )

                repository.addTimelineEvent(issueId, event)

                if (notify) {
                    val notification = AppNotification(
                        id = "",
                        userId = currentIssue.reportedBy,
                        title = "Issue Updated",
                        message = "Your issue '${currentIssue.title}' status was updated to '$newStatus'. ${if (adminNotes.isNotBlank()) "Admin: $adminNotes" else "" }",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    repository.createNotification(notification)
                }


                Toast.makeText(
                    this@IssueDetailActivity,
                    "Issue updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }.onFailure {
                Toast.makeText(
                    this@IssueDetailActivity,
                    "Failed to update issue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Issue")
            .setMessage("Are you sure you want to cancel this issue? This action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelIssue()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelIssue() {
        lifecycleScope.launch {
            val result = repository.deleteIssue(issueId)
            result.onSuccess {
                Toast.makeText(
                    this@IssueDetailActivity,
                    "Issue cancelled successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }.onFailure {
                Toast.makeText(
                    this@IssueDetailActivity,
                    "Failed to cancel issue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}