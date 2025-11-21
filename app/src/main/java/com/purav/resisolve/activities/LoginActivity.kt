package com.purav.resisolve.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.purav.resisolve.databinding.ActivityLoginBinding
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository = FirebaseRepository()
    private var selectedUserType = "resident"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Set resident as default
        binding.btnResident.isChecked = true
    }

    private fun setupListeners() {
        // User type toggle
        binding.toggleUserType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedUserType = when (checkedId) {
                    binding.btnAdmin.id -> "admin"
                    else -> "resident"
                }
            }
        }

        // Login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Password reset feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilUsername.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilUsername.error = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            val result = repository.signIn(email, password)

            result.onSuccess { userId ->
                // Get user profile
                val userResult = repository.getUser(userId)
                userResult.onSuccess { user ->
                    if (user.userType == selectedUserType) {
                        // Check if profile is complete
                        if (user.fullName.isEmpty()) {
                            // Profile not complete, navigate to profile creation
                            navigateToProfileCreation(userId)
                        } else {
                            // Profile complete, navigate to appropriate home
                            when (selectedUserType) {
                                "admin" -> navigateToAdminDashboard()
                                else -> navigateToResidentHome()
                            }
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(
                            this@LoginActivity,
                            "Please select the correct user type",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.onFailure {
                    // User doesn't exist, navigate to profile creation
                    navigateToProfileCreation(userId)
                }
            }.onFailure { exception ->
                showLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun navigateToProfileCreation(userId: String) {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_TYPE", selectedUserType)
        startActivity(intent)
        finish()
    }

    private fun navigateToResidentHome() {
        startActivity(Intent(this, ResidentHomeActivity::class.java))
        finish()
    }

    private fun navigateToAdminDashboard() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }
}