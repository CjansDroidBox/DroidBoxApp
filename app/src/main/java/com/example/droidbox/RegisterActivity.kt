package com.example.droidbox

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Bind views
        val usernameInput: EditText = findViewById(R.id.usernameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val confirmPasswordInput: EditText = findViewById(R.id.confirmPasswordInput)
        val registerButton: Button = findViewById(R.id.registerButton)
        val navigateToLogin: TextView = findViewById(R.id.navigateToLogin)

        // Google Sign-Up Section
        val googleSignUpSection: LinearLayout = findViewById(R.id.googleSignUpSection)

        // Handle registration
        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInputs(username, email, password, confirmPassword)) {
                saveUser(username, email, password)
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        }

        // Navigate to Login
        navigateToLogin.setOnClickListener {
            navigateToLogin()
        }

        // Handle Google Sign-Up
        googleSignUpSection.setOnClickListener {
            // Simulate Google sign-up
            simulateGoogleSignUp()
        }
    }

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveUser(username: String, email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun simulateGoogleSignUp() {
        // Simulate Google account details
        val googleUsername = "GoogleUser"
        val googleEmail = "googleuser@example.com"
        val googlePassword = "google123"

        // Save simulated Google account to SharedPreferences
        saveUser(googleUsername, googleEmail, googlePassword)

        // Notify user of success
        Toast.makeText(this, "Signed up with Google!", Toast.LENGTH_SHORT).show()

        // Navigate to Login or Main Activity
        navigateToLogin()
    }
}
