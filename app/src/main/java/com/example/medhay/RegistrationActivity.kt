package com.example.medhay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signUpButton: Button
    private lateinit var alreadyRegisteredText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference // Initialize Firebase Database reference

        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        signUpButton = findViewById(R.id.btn_sign_up)
        alreadyRegisteredText = findViewById(R.id.tv_already_registered)

        signUpButton.setOnClickListener { handleSignUp() }
        alreadyRegisteredText.setOnClickListener { navigateToLogin() }
    }

    private fun handleSignUp() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Simple validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration success
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    val user: FirebaseUser? = auth.currentUser

                    // Save the user's data to Firebase Realtime Database
                    saveUserToDatabase(user, name, email)

                    // Navigate directly to the Dashboard after successful registration
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Save user data to Firebase Realtime Database
    private fun saveUserToDatabase(user: FirebaseUser?, name: String, email: String) {
        user?.let {
            val userId = it.uid
            val userMap = mapOf(
                "name" to name,
                "email" to email,
                "profileImageUrl" to "", // You can leave this empty initially or update later with the user's profile image
                "uid" to userId
            )

            // Save data to "Users" node
            database.child("Users").child(userId).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
