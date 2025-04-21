package com.auth0.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.auth0.android.Auth0
import android.util.Log
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.auth0.sample.databinding.ActivityAdminDashboardBinding
import com.auth0.sample.databinding.ActivityAdminPageBinding
import com.auth0.sample.databinding.ActivityIndividualSignupBinding
import com.auth0.sample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class AdminPageActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private lateinit var binding: ActivityAdminPageBinding
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set up the account object with the Auth0 application details
        account = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )


        binding.buttonLogout.setOnClickListener {
            logout()
        }

        binding.buttonIndividualDashboard.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        binding.buttonOrganizationDashboard.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    // The user has been logged out!
                    cachedCredentials = null
                    cachedUserProfile = null
                    val intent = Intent(this@AdminPageActivity, MainActivity::class.java)
                    startActivity(intent)
                }

                override fun onFailure(exception: AuthenticationException) {
                    Log.d("Auth", "Failure: ${exception.getCode()}")
                }
            })
    }
}