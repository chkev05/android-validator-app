package com.auth0.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.auth0.sample.databinding.ActivityAdminPageBinding
import android.os.Handler
import android.os.Looper
import com.auth0.sample.databinding.ActivitySuccessfulSignupBinding


class SuccessfulSignupActivity : AppCompatActivity() {
    private lateinit var account: Auth0
    private lateinit var binding: ActivitySuccessfulSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessfulSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close this activity so user can't go back to it
        }, 5000) // 5000ms = 5 seconds
    }
}