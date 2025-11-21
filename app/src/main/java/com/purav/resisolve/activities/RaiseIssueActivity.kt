package com.purav.resisolve.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.purav.resisolve.R
import com.purav.resisolve.databinding.ActivityRaiseIssueBinding
import com.purav.resisolve.models.Issue
import com.purav.resisolve.models.TimelineEvent
import com.purav.resisolve.models.User
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class RaiseIssueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRaiseIssueBinding
    private val repository = FirebaseRepository()
    private lateinit var currentUser: User

    private val categories = arrayOf(
        "Maintenance",
        "Security",
        "Parking",
        "Noise",
        "Cleanliness",
        "Amenities",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaiseIssueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategoryDropdown()
        setupListeners()
        loadCurrentUser()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnPublishIssue.setOnClickListener {
            if (validateInput()) {
                publishIssue()
            }
        }
    }

    private fun loadCurrentUser() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val result = repository.getUser(userId)
                result.onSuccess { user ->
                    currentUser = user
                }.onFailure {
                    Toast.makeText(
                        this@RaiseIssueActivity,
                        "Failed to load user data",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.etIssueTitle.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.tilIssueTitle.error = "Title is required"
            return false
        }

        if (category.isEmpty()) {
            binding.tilCategory.error = "Category is required"
            return false
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            return false
        }

        return true
    }

    private fun getSelectedPriority(): String {
        return when (binding.chipGroupPriority.checkedChipId) {
            binding.chipHigh.id -> "High"
            binding.chipLow.id -> "Low"
            else -> "Medium"
        }
    }

    private fun publishIssue() {
        val title = binding.etIssueTitle.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val priority = getSelectedPriority()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        val issue = Issue(
            title = title,
            description = description,
            category = category,
            priority = priority,
            status = "Received",
            reportedBy = currentUser.uid,
            reportedByName = currentUser.fullName,
            reportedByFlat = currentUser.flatNumber,
            societyName = currentUser.societyName,
            location = location
        )

        binding.btnPublishIssue.isEnabled = false

        lifecycleScope.launch {
            val result = repository.createIssue(issue)

            result.onSuccess { issueId ->
                showSuccessDialog(issueId)
            }.onFailure { exception ->
                binding.btnPublishIssue.isEnabled = true
                Toast.makeText(
                    this@RaiseIssueActivity,
                    "Failed to publish issue: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showSuccessDialog(issueId: String) {
        AlertDialog.Builder(this)
            .setTitle("Issue Published!")
            .setMessage("Your issue has been successfully reported to the admin. You will receive notifications about status updates.\n\nIssue ID: #${issueId.take(5)}")
            .setPositiveButton("Back to Home") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}