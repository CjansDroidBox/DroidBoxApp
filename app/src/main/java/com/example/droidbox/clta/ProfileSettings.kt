package com.example.droidbox.clta

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileSettings : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_settings)

        // Initialize Firebase and SharedPreferences
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        sharedPreferences = getSharedPreferences("ThemePreferences", MODE_PRIVATE)

        // Register the activity result launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    saveProfilePictureLocally(selectedImageUri)
                }
            }
        }

        // Bind Views
        val handle: TextView = findViewById(R.id.handle)
        val profilePicture: ImageView = findViewById(R.id.profilePicture)
        val logoutSection: View = findViewById(R.id.logoutSection)
        val deleteAccountSection: View = findViewById(R.id.deleteAccountSection)
        val registerAccountSection: View = findViewById(R.id.registerAccountSection)
        val signInSection: View = findViewById(R.id.signInSection)
        val usernameInput: TextView = findViewById(R.id.usernameInput)
        val lightModeSwitch: Switch = findViewById(R.id.lightModeSwitch)
        val darkModeSwitch: Switch = findViewById(R.id.darkModeSwitch)
        val matchSystemSwitch: Switch = findViewById(R.id.matchSystemSwitch)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Fetch the current user
        val currentUser = firebaseAuth.currentUser

        // Display user details
        if (currentUser != null) {
            val email = currentUser.email ?: "No email available"
            handle.text = email

            val userRef = firebaseDatabase.getReference("Users").child(currentUser.uid)
            userRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").value?.toString() ?: "Cjan Cortes"
                usernameInput.text = username
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load username.", Toast.LENGTH_SHORT).show()
            }

            // Hide Sign-In and Register Sections if logged in
            signInSection.visibility = View.GONE
            registerAccountSection.visibility = View.GONE
        } else {
            handle.text = "No user logged in"
            logoutSection.visibility = View.GONE
            deleteAccountSection.visibility = View.GONE
        }

        // Set up Theme Switches
        setupThemeSwitches(lightModeSwitch, darkModeSwitch, matchSystemSwitch)

        // Profile Picture Click Listener
        profilePicture.setOnClickListener {
            openGalleryForProfilePicture()
        }

        // Logout Section Listener
        logoutSection.setOnClickListener {
            showLogoutDialog()
        }

        // Delete Account Section Listener
        deleteAccountSection.setOnClickListener {
            showDeleteAccountDialog(currentUser)
        }

        // Sign-In Section Listener
        signInSection.setOnClickListener {
            navigateToSignIn()
        }

        // Register Section Listener
        registerAccountSection.setOnClickListener {
            navigateToRegister()
        }

        // Username Editing Listener
        usernameInput.setOnClickListener {
            if (currentUser != null) {
                showEditUsernameDialog(currentUser.uid, usernameInput)
            } else {
                Toast.makeText(this, "No user logged in to edit username.", Toast.LENGTH_SHORT).show()
            }
        }

        // Back Button Listener
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Load profile picture from local storage
        loadProfilePicture()
    }

    private fun openGalleryForProfilePicture() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveProfilePictureLocally(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "profile_picture.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // Update the profile picture in the UI using Glide with cache invalidation
            val profilePicture: ImageView = findViewById(R.id.profilePicture)
            Glide.with(this)
                .load(file)
                .signature(ObjectKey(System.currentTimeMillis().toString())) // Invalidate cache
                .into(profilePicture)
            Toast.makeText(this, "Profile picture saved locally!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to save profile picture locally: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePicture() {
        val file = File(filesDir, "profile_picture.jpg")
        if (file.exists()) {
            val profilePicture: ImageView = findViewById(R.id.profilePicture)
            Glide.with(this)
                .load(file)
                .signature(ObjectKey(System.currentTimeMillis().toString())) // Invalidate cache
                .into(profilePicture)
        }
    }

    private fun showEditUsernameDialog(userId: String, usernameInput: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_username, null)
        val editUsernameField = dialogView.findViewById<EditText>(R.id.editUsername)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        editUsernameField.setText(usernameInput.text)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val newUsername = editUsernameField.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                val userRef = firebaseDatabase.getReference("Users").child(userId)
                userRef.child("username").setValue(newUsername).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        usernameInput.text = newUsername
                        Toast.makeText(this, "Username updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update username.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_out, null)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelLogOutButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmLogOutButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            firebaseAuth.signOut()
            handlePostLogoutOrDelete()
            dialog.dismiss()
            recreate() // Refresh the activity
        }

        dialog.show()
    }

    private fun showDeleteAccountDialog(currentUser: FirebaseUser?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelDeleteAccountButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmDeleteAccountButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            deleteAccount(currentUser)
            dialog.dismiss()
            recreate() // Refresh the activity
        }

        dialog.show()
    }

    private fun deleteAccount(currentUser: FirebaseUser?) {
        currentUser?.let { user ->
            val userId = user.uid
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = firebaseDatabase.getReference("Users").child(userId)
                    userRef.removeValue()
                    handlePostLogoutOrDelete()
                } else {
                    Toast.makeText(this, "Error deleting account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "No user to delete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePostLogoutOrDelete() {
        findViewById<View>(R.id.signInSection).visibility = View.VISIBLE
        findViewById<View>(R.id.registerAccountSection).visibility = View.VISIBLE
        findViewById<View>(R.id.logoutSection).visibility = View.GONE
        findViewById<View>(R.id.deleteAccountSection).visibility = View.GONE
    }

    private fun navigateToSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun setupThemeSwitches(lightModeSwitch: Switch, darkModeSwitch: Switch, matchSystemSwitch: Switch) {
        val savedTheme = sharedPreferences.getString("Theme", "MatchSystem") ?: "MatchSystem"

        // Set the default state of switches based on saved theme
        when (savedTheme) {
            "Light" -> lightModeSwitch.isChecked = true
            "Dark" -> darkModeSwitch.isChecked = true
            else -> matchSystemSwitch.isChecked = true // Default to Match System
        }

        lightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                darkModeSwitch.isChecked = false
                matchSystemSwitch.isChecked = false
                applyTheme("Light")
            }
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lightModeSwitch.isChecked = false
                matchSystemSwitch.isChecked = false
                applyTheme("Dark")
            }
        }

        matchSystemSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lightModeSwitch.isChecked = false
                darkModeSwitch.isChecked = false
                applyTheme("MatchSystem")
            }
        }
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "MatchSystem" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        sharedPreferences.edit().putString("Theme", theme).apply()
        recreate()
    }
}