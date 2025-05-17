package com.auth0.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import android.util.Log
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.auth0.sample.databinding.ActivityAdminDashboardBinding
import kotlinx.serialization.Serializable
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*

@Serializable
data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val city: String = "",
    val zipcode: String = "",
    val address_line_1: String = ""
)

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private lateinit var binding: ActivityAdminDashboardBinding
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set up the account object with the Auth0 application details
        account = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )

        lifecycleScope.launch {
            val users = fetchUsers()
            Log.d("FetchedUsers", users.toString())
        }



        // temp mongoDB code
//        val connectionString = "mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/test?retryWrites=true&w=majority"
//        val client = KMongo.createClient(ConnectionString(connectionString))
//        val database = client.getDatabase("test_db")
//        val collection = database.getCollection<User>()
//
//        val newUser = User("Charlie", 28)
//        collection.insertOne(newUser)
//
//        val found = collection.findOne(User::name eq "Charlie")

        binding.buttonAdminHomepage.setOnClickListener {
            val intent = Intent(this, AdminPageActivity::class.java)
            startActivity(intent)
        }


        binding.buttonLogout.setOnClickListener {
            logout()
        }

    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    // The user has been logged out!
                    cachedCredentials = null
                    cachedUserProfile = null
                    val intent = Intent(this@AdminDashboardActivity, MainActivity::class.java)
                    startActivity(intent)
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.d("Auth", "Failure: ${error.getCode()}")
                }
            })
    }
}

private suspend fun fetchUsers(): List<User> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return try {
        client.get("http://10.0.2.2:8000/api/individual_users").body()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    } finally {
        client.close()
    }
}