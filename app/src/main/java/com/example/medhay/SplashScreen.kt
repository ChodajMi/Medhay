// SplashActivity.kt
package com.example.medhay

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for a few seconds to show the splash screen
        Handler().postDelayed({
            // Start RegistrationActivity after the splash screen
            val intent = Intent(this@SplashScreen, RegistrationActivity::class.java)
            startActivity(intent)
            finish() // Close the SplashActivity
        }, 2000) // 2 seconds delay
    }
}
