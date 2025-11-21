package com.purav.resisolve.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.purav.resisolve.R
import com.purav.resisolve.adapters.IssueAdapter
import com.purav.resisolve.databinding.ActivityResidentHomeBinding
import com.purav.resisolve.models.Issue
import com.purav.resisolve.models.User
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ResidentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentHomeBinding
    private val repository = FirebaseRepository()
    private lateinit var currentUser: User
    private val issuesAdapter = IssueAdapter(mutableListOf(), false) { issue ->
        navigateToIssueDetail(issue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadUserData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            // Open drawer or menu
            showMenuDialog()
        }
    }

    private fun setupRecyclerView() {
        binding.rvMyIssues.apply {
            layoutManager = LinearLayoutManager(this@ResidentHomeActivity)
            adapter = issuesAdapter
        }
    }

    private fun setupListeners() {
        // Floating Action Button
        binding.fabRaiseIssue.setOnClickListener {
            navigateToRaiseIssue()
        }

        // Notifications
        binding.ivNotifications.setOnClickListener {
            navigateToNotifications()
        }

        // View All
        binding.tvViewAll.setOnClickListener {
            // Navigate to all issues screen
            Toast.makeText(this, "View All Issues", Toast.LENGTH_SHORT).show()
        }

        // Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_raise_issue -> {
                    navigateToRaiseIssue()
                    false
                }
                R.id.navigation_menu -> {
                    showMenuDialog()
                    false
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                // Load user profile
                val userResult = repository.getUser(userId)
                userResult.onSuccess { user ->
                    currentUser = user
                    displayUserInfo(user)
                    loadUserIssues(userId)
                }.onFailure {
                    Toast.makeText(
                        this@ResidentHomeActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun displayUserInfo(user: User) {
        binding.tvUserName.text = user.fullName
        binding.tvSocietyName.text = user.societyName
        binding.tvFlatNumber.text = "Flat ${user.flatNumber}"
        binding.tvPhoneNumber.text = user.phoneNumber
    }

    private fun loadUserIssues(userId: String) {
        lifecycleScope.launch {
            val result = repository.getIssuesByUser(userId)
            result.onSuccess { issues ->
                if (issues.isEmpty()) {
                    binding.rvMyIssues.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvMyIssues.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    issuesAdapter.updateIssues(issues)
                }
                binding.tvIssueCount.text = issues.size.toString()
            }.onFailure {
                Toast.makeText(
                    this@ResidentHomeActivity,
                    "Failed to load issues",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToRaiseIssue() {
        startActivity(Intent(this, RaiseIssueActivity::class.java))
    }

    private fun navigateToNotifications() {
        startActivity(Intent(this, NotificationsActivity::class.java))
    }

    private fun navigateToIssueDetail(issue: Issue) {
        val intent = Intent(this, IssueDetailActivity::class.java)
        intent.putExtra("ISSUE_ID", issue.id)
        intent.putExtra("IS_ADMIN", false)
        startActivity(intent)
    }

    private fun showMenuDialog() {
        val options = arrayOf("Profile Settings", "App Settings", "Rate the App", "Logout")

        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToProfileSettings()
                    1 -> Toast.makeText(this, "App Settings", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "Rate the App", Toast.LENGTH_SHORT).show()
                    3 -> showLogoutConfirmation()
                }
            }
            .show()
    }

    private fun navigateToProfileSettings() {
        val intent = Intent(this, ProfileSettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        repository.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Reload issues when returning to this screen
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            loadUserIssues(userId)
        }
    }
}