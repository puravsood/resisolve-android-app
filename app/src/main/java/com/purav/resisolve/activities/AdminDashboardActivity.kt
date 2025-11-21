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
import com.purav.resisolve.databinding.ActivityAdminDashboardBinding
import com.purav.resisolve.models.Issue
import com.purav.resisolve.models.User
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val repository = FirebaseRepository()
    private lateinit var currentUser: User
    private var allIssues = mutableListOf<Issue>()
    private val issuesAdapter = IssueAdapter(mutableListOf(), true) { issue ->
        navigateToIssueDetail(issue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadAdminData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            showMenuDialog()
        }
    }

    private fun setupRecyclerView() {
        binding.rvAllIssues.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = issuesAdapter
        }
    }

    private fun setupListeners() {
        // Search functionality
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            filterIssues(query)
            true
        }

        // Filter chips
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAll.id -> showAllIssues()
                    binding.chipReceived.id -> filterByStatus("Received")
                    binding.chipInProgress.id -> filterByStatus("In Progress")
                    binding.chipResolved.id -> filterByStatus("Resolved")
                }
            }
        }

        // Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> true
                R.id.navigation_raise_issue -> {
                    startActivity(Intent(this, RaiseIssueActivity::class.java))
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

    private fun loadAdminData() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                // Load admin profile
                val userResult = repository.getUser(userId)
                userResult.onSuccess { user ->
                    currentUser = user
                    loadSocietyIssues(user.societyName)
                }.onFailure {
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadSocietyIssues(societyName: String) {
        lifecycleScope.launch {
            val result = repository.getIssuesBySociety(societyName)
            result.onSuccess { issues ->
                allIssues.clear()
                allIssues.addAll(issues)

                if (issues.isEmpty()) {
                    binding.rvAllIssues.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvAllIssues.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    issuesAdapter.updateIssues(issues)
                }

                updateStats(issues)
            }.onFailure {
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Failed to load issues",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateStats(issues: List<Issue>) {
        binding.tvTotalIssues.text = issues.size.toString()

        val pending = issues.count { it.status in listOf("Received", "In Progress") }
        binding.tvPendingIssues.text = pending.toString()

        val resolved = issues.count { it.status == "Resolved" }
        binding.tvResolvedIssues.text = resolved.toString()
    }

    private fun filterIssues(query: String) {
        if (query.isEmpty()) {
            issuesAdapter.updateIssues(allIssues)
            return
        }

        val filtered = allIssues.filter { issue ->
            issue.title.contains(query, ignoreCase = true) ||
                    issue.description.contains(query, ignoreCase = true) ||
                    issue.reportedByName.contains(query, ignoreCase = true) ||
                    issue.reportedByFlat.contains(query, ignoreCase = true)
        }

        issuesAdapter.updateIssues(filtered)
    }

    private fun showAllIssues() {
        issuesAdapter.updateIssues(allIssues)
    }

    private fun filterByStatus(status: String) {
        val filtered = allIssues.filter { it.status == status }
        issuesAdapter.updateIssues(filtered)
    }

    private fun navigateToIssueDetail(issue: Issue) {
        val intent = Intent(this, IssueDetailActivity::class.java)
        intent.putExtra("ISSUE_ID", issue.id)
        intent.putExtra("IS_ADMIN", true)
        startActivity(intent)
    }

    private fun showMenuDialog() {
        val options = arrayOf("Profile Settings", "App Settings", "Logout")

        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, ProfileSettingsActivity::class.java))
                    1 -> Toast.makeText(this, "App Settings", Toast.LENGTH_SHORT).show()
                    2 -> showLogoutConfirmation()
                }
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                repository.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::currentUser.isInitialized) {
            loadSocietyIssues(currentUser.societyName)
        }
    }
}