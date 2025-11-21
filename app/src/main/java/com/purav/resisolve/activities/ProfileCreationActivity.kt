package com.purav.resisolve.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.purav.resisolve.databinding.ActivityProfileCreationBinding
import com.purav.resisolve.models.User
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ProfileCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileCreationBinding
    private val repository = FirebaseRepository()
    private lateinit var userId: String
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        userId = intent.getStringExtra("USER_ID") ?: ""
        userType = intent.getStringExtra("USER_TYPE") ?: "resident"

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvHeader.text = if (userType == "admin") {
            "Create Admin Profile"
        } else {
            "Create Resident Profile"
        }
    }

    private fun setupListeners() {
        binding.btnCreateProfile.setOnClickListener {
            if (validateInput()) {
                createProfile()
            }
        }
    }

    private fun validateInput(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val streetAddress = binding.etStreetAddress.text.toString().trim()
        val societyName = binding.etSocietyName.text.toString().trim()
        val flatNumber = binding.etFlatNumber.text.toString().trim()

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

        if (streetAddress.isEmpty()) {
            binding.tilStreetAddress.error = "Street address is required"
            return false
        }

        if (societyName.isEmpty()) {
            binding.tilSocietyName.error = "Society name is required"
            return false
        }

        if (flatNumber.isEmpty() && userType == "resident") {
            binding.tilFlatNumber.error = "Flat number is required"
            return false
        }

        return true
    }

    private fun createProfile() {
        showLoading(true)

        val user = User(
            uid = userId,
            email = repository.getCurrentUserId() ?: "",
            fullName = binding.etFullName.text.toString().trim(),
            phoneNumber = binding.etPhoneNumber.text.toString().trim(),
            address = binding.etStreetAddress.text.toString().trim(),
            societyName = binding.etSocietyName.text.toString().trim(),
            flatNumber = binding.etFlatNumber.text.toString().trim(),
            emergencyContact = binding.etEmergencyContact.text.toString().trim(),
            userType = userType
        )

        lifecycleScope.launch {
            val result = repository.saveUser(user)

            result.onSuccess {
                showLoading(false)
                showSuccessDialog()
            }.onFailure { exception ->
                showLoading(false)
                Toast.makeText(
                    this@ProfileCreationActivity,
                    "Failed to create profile: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateProfile.isEnabled = !show
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Profile Created!")
            .setMessage("Your profile has been successfully created. Please login again with your credentials to continue.")
            .setPositiveButton("Go to Login") { _, _ ->
                // Sign out and go to login
                repository.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}