package com.auth0.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.auth0.sample.databinding.ActivityIndividualSignupBinding
import com.auth0.sample.databinding.ActivityMainBinding
import com.auth0.sample.databinding.ActivityOrganizationSignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.auth0.sample.signUpUser

class OrganizationSignupActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private lateinit var binding: ActivityOrganizationSignupBinding
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the account object with the Auth0 application details
        account = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )

        val spinnerSpecialty: Spinner = findViewById(R.id.spinnerSpecialty)
        val specialtyItems = resources.getStringArray(R.array.specialty_array)
        val specialtyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialtyItems)
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = specialtyAdapter

        val spinnerStates: Spinner = findViewById(R.id.spinnerStates)
        val stateItems = resources.getStringArray(R.array.state_abbreviation_array)
        val stateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stateItems)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStates.adapter = stateAdapter

        spinnerSpecialty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // hide these values first in case of swapping elements
                findViewById<TextInputLayout>(R.id.organizationNameInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.addressInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.cityInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.spinnerStatesInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.zipcodeInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.phoneNumberInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.emailInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.passwordInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.orgRepUsernameInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.feinInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.npiInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.organizationCodeInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.bureauNameInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.stateDepIDInputLayout).visibility = View.GONE
                findViewById<TextInputLayout>(R.id.taxIDInputLayout).visibility = View.GONE

                // Universal fields that will open up
                findViewById<TextInputLayout>(R.id.organizationNameInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.addressInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.cityInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.spinnerStatesInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.zipcodeInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.phoneNumberInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.emailInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.passwordInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.orgRepUsernameInputLayout).visibility = View.VISIBLE

                val selectedItem = parent.getItemAtPosition(position).toString()

                if (selectedItem == "Commercial (For Profit)" || selectedItem == "Other (Non-Profit)") {
                    findViewById<TextInputLayout>(R.id.feinInputLayout).visibility = View.VISIBLE
                }
                else if (selectedItem == "Healthcare (For-Profit)" || selectedItem == "Healthcare (Non-Profit)") {
                    findViewById<TextInputLayout>(R.id.npiInputLayout).visibility = View.VISIBLE
                    findViewById<TextInputLayout>(R.id.feinInputLayout).visibility = View.VISIBLE
                    findViewById<TextInputLayout>(R.id.taxIDInputLayout).visibility = View.VISIBLE
                }
                else if (selectedItem == "Federal Government Agency") {
                    findViewById<TextInputLayout>(R.id.organizationCodeInputLayout).visibility = View.VISIBLE
                    findViewById<TextInputLayout>(R.id.bureauNameInputLayout).visibility = View.VISIBLE
                }
                else if (selectedItem == "State Government Agency" || selectedItem == "Municipal Agency") {
                    findViewById<TextInputLayout>(R.id.stateDepIDInputLayout).visibility = View.VISIBLE
                    findViewById<TextInputLayout>(R.id.feinInputLayout).visibility = View.VISIBLE
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Your logic when no item is selected (can be left empty)
            }
        }

        binding.buttonSubmitSignup.setOnClickListener {
            signUpUser(
                email = "email",
                password = "password",
                firstName = "firstname",
                middleName = "middlename",
                lastName = "lastname",
                phoneNumber = "phonenumber",
                specialtyAuth = spinnerSpecialty.selectedItem.toString(),
                ssn = "ssn",
                dateOfBirth = "dob",
                addressLine1 = "address",
                city = "city",
                state = spinnerStates.selectedItem.toString(),
                zipcode = "zipcode",
                onResult = { success, message ->
                    if (success) {
                        println("✅ Signup successful: $message")
                        val intent = Intent(this@OrganizationSignupActivity, SuccessfulSignupActivity::class.java)
                        startActivity(intent)
                        // redirect when successful
                    } else {
                        println("❌ Signup failed: $message")
                    }
                }
            )
        }

        binding.buttonHomepage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}
