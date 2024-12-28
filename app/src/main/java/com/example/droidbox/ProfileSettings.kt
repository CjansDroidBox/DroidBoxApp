// Refactored ProfileSettings.kt with Firebase integration
package com.example.droidbox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileSettings : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_settings)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        // Bind views
        val username: TextView = findViewById(R.id.username)
        val accountType: TextView = findViewById(R.id.accountType)
        val editProfileButton: Button = findViewById(R.id.editProfileButton)
        val upgradeAccountButton: Button = findViewById(R.id.upgradeAccountButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        if (currentUser == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch user data from Firebase Realtime Database
        val userId = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val userData = snapshot.value as? Map<*, *>
            username.text = userData?.get("username")?.toString() ?: "Unknown User"
            accountType.text = userData?.get("accountType")?.toString() ?: "Limited"
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
        }

        // Edit Profile Button
        editProfileButton.setOnClickListener { showEditProfileDialog(userRef) }

        // Upgrade Account Button
        upgradeAccountButton.setOnClickListener {
            userRef.child("accountType").setValue("Premium").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account upgraded to Premium!", Toast.LENGTH_SHORT).show()
                    accountType.text = "Premium"
                } else {
                    Toast.makeText(this, "Failed to upgrade account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Delete Account Button
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    currentUser.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userRef.removeValue()
                            Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, RegisterActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to delete account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }

        // Logout Button
        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showEditProfileDialog(userRef: DatabaseReference) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val usernameInput = dialogView.findViewById<TextView>(R.id.usernameInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedUsername = usernameInput.text.toString().trim()
                if (updatedUsername.isNotEmpty()) {
                    userRef.child("username").setValue(updatedUsername).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                            recreate() // Refresh the activity to show updates
                        } else {
                            Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
