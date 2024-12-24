package com.example.droidbox

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Bind views
        val usernameInput: EditText = findViewById(R.id.usernameInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.loginButton)
        val googleSignIn: ImageView = findViewById(R.id.googleSignIn) // Google Sign-In ImageView

        // Handle Login Button Click
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                login(username, password)
            }
        }

        // Handle Google Sign-In Click
        googleSignIn.setOnClickListener {
            simulateGoogleSignIn()
        }
    }

    private fun login(username: String, password: String) {
        // Fetch saved credentials
        val savedUsername = sharedPreferences.getString("username", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (savedUsername != null && savedPassword != null) {
            if (username == savedUsername && password == savedPassword) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                // Save login state
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                // Navigate to ProfileSettings
                val intent = Intent(this, ProfileSettings::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No account found. Please register first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun simulateGoogleSignIn() {
        // Simulate a Google sign-in process
        Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()

        // Navigate to ProfileSettings
        val intent = Intent(this, ProfileSettings::class.java)
        startActivity(intent)
        finish()
    }
}
