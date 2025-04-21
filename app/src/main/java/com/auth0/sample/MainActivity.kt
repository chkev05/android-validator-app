package com.auth0.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.auth0.android.Auth0
import com.auth0.android.jwt.JWT
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import android.content.Intent
import android.util.Log
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.auth0.sample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private lateinit var binding: ActivityMainBinding
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the account object with the Auth0 application details
        account = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )

        // Bind the button click with the login action
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonAdminLogin.setOnClickListener { loginWithBrowser() }
        binding.buttonIndividualSignup.setOnClickListener {
            val intent = Intent(this, IndividualSignupActivity::class.java)
            startActivity(intent)
        }
        binding.buttonOrganizationSignup.setOnClickListener {
            val intent = Intent(this, OrganizationSignupActivity::class.java)
            startActivity(intent)
        }

    }


    private fun loginWithBrowser() {
        // Setup the WebAuthProvider, using the custom scheme and scope.
        WebAuthProvider.login(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope("openid profile email read:current_user update:current_user_metadata")
            .withAudience("https://${getString(R.string.com_auth0_domain)}/api/v2/")

            // Launch the authentication passing the callback where the results will be received
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(exception: AuthenticationException) {
                    showSnackBar("Failure: ${exception.getCode()}")
                }

//                override fun onSuccess(credentials: Credentials) {
//                    cachedCredentials = credentials
//                    showSnackBar("Success: ${credentials.accessToken}")
//                    updateUI()
//                    showUserProfile()
//                }
                override fun onSuccess(credentials: Credentials) {
                    cachedCredentials = credentials
                    val accessToken = credentials.idToken
                    val jwt = JWT(accessToken)
                    val roles = jwt.getClaim("custom_roles").asList(String::class.java)
                    val userID = jwt.getClaim("sub").asString()

                    Log.d("Auth", "Roles: $roles")

                    if ("Central Application Admin" in roles) {
                        // Proceed with admin access
                        Log.d("Auth", "User has admin role!")
                        // Navigate to admin-specific activity, for example
                        showSnackBar("Success: has Admin Role")
                        val intent = Intent(this@MainActivity, AdminPageActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        // Handle non-admin access
                        Log.d("Auth", "User does not have admin role!")
                        Log.d("Auth", "Roles: $roles $jwt $userID")

                        showSnackBar("Failure: No Admin Role")
                        logout()
                    }
//                    showSnackBar("Success: ${credentials.accessToken}")
//                    updateUI()
//                    showUserProfile()
                }
            })
    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    // The user has been logged out!
                    cachedCredentials = null
                    cachedUserProfile = null
                }

                override fun onFailure(exception: AuthenticationException) {
                    showSnackBar("Failure: ${exception.getCode()}")
                }
            })
    }

    private fun showUserProfile() {
        val client = AuthenticationAPIClient(account)

        // Use the access token to call userInfo endpoint.
        // In this sample, we can assume cachedCredentials has been initialized by this point.
        client.userInfo(cachedCredentials!!.accessToken!!)
            .start(object : Callback<UserProfile, AuthenticationException> {
                override fun onFailure(exception: AuthenticationException) {
                    showSnackBar("Failure: ${exception.getCode()}")
                }

                override fun onSuccess(profile: UserProfile) {
                    cachedUserProfile = profile;
                }
            })
    }

    private fun getUserMetadata() {
//        // Create the user API client
//        val usersClient = UsersAPIClient(account, cachedCredentials!!.accessToken!!)
//
//        // Get the full user profile
//        usersClient.getProfile(cachedUserProfile!!.getId()!!)
//            .start(object : Callback<UserProfile, ManagementException> {
//                override fun onFailure(exception: ManagementException) {
//                    showSnackBar("Failure: ${exception.getCode()}")
//                }
//
//                override fun onSuccess(userProfile: UserProfile) {
//                    cachedUserProfile = userProfile;
//                    updateUI()
////
////                    val country = userProfile.getUserMetadata()["country"] as String?
////                    binding.inputEditMetadata.setText(country)
//                }
//            })
    }

    private fun patchUserMetadata() {
//        val usersClient = UsersAPIClient(account, cachedCredentials!!.accessToken!!)
////        val metadata = mapOf("country" to binding.inputEditMetadata.text.toString())
//
//        usersClient
//            .updateMetadata(cachedUserProfile!!.getId()!!, metadata)
//            .start(object : Callback<UserProfile, ManagementException> {
//                override fun onFailure(exception: ManagementException) {
//                    showSnackBar("Failure: ${exception.getCode()}")
//                }
//
//                override fun onSuccess(profile: UserProfile) {
//                    cachedUserProfile = profile
//                    updateUI()
//                    showSnackBar("Successful")
//                }
//            })
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(
            binding.root,
            text,
            Snackbar.LENGTH_LONG
        ).show()
    }
}