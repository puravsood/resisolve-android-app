package com.purav.resisolve.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.purav.resisolve.databinding.ActivityProfileSettingsBinding
import com.purav.resisolve.models.User
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private val repository = FirebaseRepository()
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        loadUserProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnSaveChanges.setOnClickListener {
            if (validateInput()) {
                saveChanges()
            }
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadUserProfile() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val result = repository.getUser(userId)
                result.onSuccess { user ->
                    currentUser = user
                    displayUserInfo(user)
                }.onFailure {
                    Toast.makeText(
                        this@ProfileSettingsActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun displayUserInfo(user: User) {
        binding.etFullName.setText(user.fullName)
        binding.etPhoneNumber.setText(user.phoneNumber)
        binding.etEmail.setText(user.email)
        binding.etFlatNumber.setText(user.flatNumber)
        binding.etSocietyName.setText(user.societyName)
        binding.etEmergencyContact.setText(user.emergencyContact)

        // Society name cannot be changed
        binding.etSocietyName.isEnabled = false
        binding.tvSocietyHelper.visibility = View.VISIBLE
    }

    private fun validateInput(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Full name is required"
            return false
        }

        if (phoneNumber.isEmpty()) {
            binding.tilPhoneNumber.error = "Phone number is required"
            return false
        }

        if (phoneNumber.length < 10) {
            binding.tilPhoneNumber.error = "Invalid phone number"
            return false
        }

        return true
    }

    private fun saveChanges() {
        showLoading(true)

        val updates = mapOf(
            "fullName" to binding.etFullName.text.toString().trim(),
            "phoneNumber" to binding.etPhoneNumber.text.toString().trim(),
            "flatNumber" to binding.etFlatNumber.text.toString().trim(),
            "emergencyContact" to binding.etEmergencyContact.text.toString().trim()
        )

        lifecycleScope.launch {
            val result = repository.updateUser(currentUser.uid, updates)

            result.onSuccess {
                showLoading(false)
                Toast.makeText(
                    this@ProfileSettingsActivity,
                    "Profile updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }.onFailure {
                showLoading(false)
                Toast.makeText(
                    this@ProfileSettingsActivity,
                    "Failed to update profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveChanges.isEnabled = !show
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        lifecycleScope.launch {
            val result = repository.deleteUser(currentUser.uid)

            result.onSuccess {
                Toast.makeText(
                    this@ProfileSettingsActivity,
                    "Account deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@ProfileSettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure {
                Toast.makeText(
                    this@ProfileSettingsActivity,
                    "Failed to delete account",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}