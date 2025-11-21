package com.purav.resisolve.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.purav.resisolve.repository.FirebaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in after a short delay
        lifecycleScope.launch {
            delay(2000) // 2 second splash
            checkLoginStatus()
        }
    }

    private fun checkLoginStatus() {
        if (repository.isUserLoggedIn()) {
            // User is logged in, check user type and navigate accordingly
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                lifecycleScope.launch {
                    val result = repository.getUser(userId)
                    result.onSuccess { user ->
                        when (user.userType) {
                            "admin" -> navigateToAdminDashboard()
                            else -> navigateToResidentHome()
                        }
                    }.onFailure {
                        navigateToLogin()
                    }
                }
            } else {
                navigateToLogin()
            }
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
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