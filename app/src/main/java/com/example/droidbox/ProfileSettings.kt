package com.example.droidbox

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide

class ProfileSettings : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Bind views
        val profilePicture: ImageView = findViewById(R.id.profilePicture)
        val username: TextView = findViewById(R.id.username)
        val accountType: TextView = findViewById(R.id.accountType)
        val editProfileButton: Button = findViewById(R.id.editProfileButton)
        val upgradeAccountButton: Button = findViewById(R.id.upgradeAccountButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        // Load default or saved user data
        val defaultUser = User("DefaultUser", "Free Account", "")
        val currentUser = getCurrentUser() ?: defaultUser

        username.text = currentUser.username
        accountType.text = currentUser.accountType

        // Load profile picture using Glide
        loadProfilePicture(profilePicture, currentUser.profilePictureUrl)

        // Handle button clicks
        editProfileButton.setOnClickListener { showEditProfileDialog() }
        upgradeAccountButton.setOnClickListener { upgradeAccount() }
        logoutButton.setOnClickListener { logout() }
    }

    private fun getCurrentUser(): User? {
        val username = sharedPreferences.getString("username", null)
        val accountType = sharedPreferences.getString("accountType", null)
        val profilePictureUrl = sharedPreferences.getString("profilePictureUrl", null)

        return if (username != null && accountType != null) {
            User(username, accountType, profilePictureUrl ?: "")
        } else null
    }

    private fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putString("username", user.username)
            putString("accountType", user.accountType)
            putString("profilePictureUrl", user.profilePictureUrl)
            apply()
        }
    }

    private fun loadProfilePicture(imageView: ImageView, url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_error)
            .into(imageView)
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val usernameInput = dialogView.findViewById<TextView>(R.id.usernameInput)
        val profilePictureInput = dialogView.findViewById<TextView>(R.id.profilePictureInput)

        val currentUser = getCurrentUser()
        usernameInput.text = currentUser?.username
        profilePictureInput.text = currentUser?.profilePictureUrl

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedUsername = usernameInput.text.toString().trim()
                val updatedProfilePictureUrl = profilePictureInput.text.toString().trim()

                if (updatedUsername.isNotEmpty()) {
                    val updatedUser = currentUser?.copy(
                        username = updatedUsername,
                        profilePictureUrl = updatedProfilePictureUrl
                    )
                    if (updatedUser != null) {
                        saveUser(updatedUser)
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                } else {
                    Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
        finish()
    }
}

data class User(
    val username: String,
    val accountType: String,
    val profilePictureUrl: String
)
