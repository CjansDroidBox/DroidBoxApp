package com.example.droidbox

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class ProfileSettings : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Bind views
        val username: TextView = findViewById(R.id.username)
        val accountType: TextView = findViewById(R.id.accountType)
        val editProfileButton: Button = findViewById(R.id.editProfileButton)
        val upgradeAccountButton: Button = findViewById(R.id.upgradeAccountButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        // Load default or saved user data
        val defaultUser = User("DefaultUser", "Free Account")
        val currentUser = getCurrentUser() ?: defaultUser

        username.text = currentUser.username
        accountType.text = currentUser.accountType

        // Handle button clicks
        editProfileButton.setOnClickListener { showEditProfileDialog() }
        upgradeAccountButton.setOnClickListener { upgradeAccount() }
        logoutButton.setOnClickListener { logout() }

        val registerButton: Button = findViewById(R.id.registerButton)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }

    private fun getCurrentUser(): User? {
        val username = sharedPreferences.getString("username", null)
        val accountType = sharedPreferences.getString("accountType", null)

        return if (username != null && accountType != null) {
            User(username, accountType)
        } else null
    }

    private fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putString("username", user.username)
            putString("accountType", user.accountType)
            apply()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val usernameInput = dialogView.findViewById<TextView>(R.id.usernameInput)

        val currentUser = getCurrentUser()
        usernameInput.text = currentUser?.username

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedUsername = usernameInput.text.toString().trim()

                if (validateUsername(updatedUsername)) {
                    val updatedUser = currentUser?.copy(username = updatedUsername)
                    if (updatedUser != null) {
                        saveUser(updatedUser)
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                } else {
                    Toast.makeText(this, "Invalid username. Must be 3-15 characters with no special symbols.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun upgradeAccount() {
        val updatedUser = getCurrentUser()?.copy(accountType = "Premium Account")
        if (updatedUser != null) {
            saveUser(updatedUser)
            Toast.makeText(this, "Account upgraded to Premium!", Toast.LENGTH_SHORT).show()
            recreate()
        }
    }

    private fun logout() {
        sharedPreferences.edit().clear().apply()
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()

        // Navigate back to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun validateUsername(username: String): Boolean {
        val regex = "^[a-zA-Z0-9]{3,15}$".toRegex()
        return username.matches(regex)
    }
}

data class User(
    val username: String,
    val accountType: String
)
