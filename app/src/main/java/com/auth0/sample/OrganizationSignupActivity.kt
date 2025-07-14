package com.auth0.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import android.content.Intent
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
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
import com.auth0.sample.isGoodPassword
import  com.auth0.sample.isGoodPhoneNumber
import com.auth0.sample.isGoodAddress
import com.auth0.sample.setEmailCharacterFilter
import kotlinx.coroutines.launch

class OrganizationSignupActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private lateinit var binding: ActivityOrganizationSignupBinding
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    private var stateNoTaxID: MutableList<String> = mutableListOf(
        "AK", // Alaska
        "FL", // Florida
        "NV", // Nevada
        "NH", // New Hampshire
        "SD", // South Dakota
        "TN", // Tennessee
        "TX", // Texas
        "WA", // Washington
        "WY"  // Wyoming
    )

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

        // validator stuff
        val feinEditText = findViewById<TextInputEditText>(R.id.editFEIN)
        val orgCodeEditText = findViewById<TextInputEditText>(R.id.editOrgCode)
        addFEINFormatter(feinEditText)
//        addOrgCodeFormatter(orgCodeEditText)
        val npiEditText = findViewById<TextInputEditText>(R.id.editNPI)
        npiEditText.filters = arrayOf(InputFilter.LengthFilter(10))
        npiEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        // filters
        setEmailCharacterFilter(findViewById<TextInputEditText>(R.id.editEmail))
        setPasswordCharacterFilter(findViewById<TextInputEditText>(R.id.editPassword))

        setCharacterFilter(findViewById<TextInputEditText>(R.id.editAddress), "A-Za-z0-9 \\-")
        setCharacterFilter(findViewById<TextInputEditText>(R.id.editCity), "A-Za-z \\-")

        setCharacterFilter(findViewById<TextInputEditText>(R.id.editOrgRepUsername), "A-Za-z0-9_@")

        setCharacterFilter(findViewById<TextInputEditText>(R.id.editOrganizationName), "A-Za-z0-9 \\-")
        setCharacterFilter(findViewById<TextInputEditText>(R.id.editBurName), "a-zA-Z")


        // organization chooser
        spinnerSpecialty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                val selectedState = spinnerStates.selectedItem?.toString() ?: ""

                updateVisibilityBasedOnSelection(selectedItem, selectedState)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle no selection
            }
        }

        // state refresher
        spinnerStates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedState = parent.getItemAtPosition(position).toString()
                val selectedItem = spinnerSpecialty.selectedItem?.toString() ?: ""

                updateVisibilityBasedOnSelection(selectedItem, selectedState)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        addPhoneFormatter(findViewById<TextInputEditText>(R.id.editPhoneNumber))

        binding.buttonSubmitSignup.setOnClickListener {

            lifecycleScope.launch {

                val emailLayout = findViewById<TextInputLayout>(R.id.emailInputLayout)
                val email = findViewById<TextInputEditText>(R.id.editEmail).text.toString()

                val passwordLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
                val password = findViewById<TextInputEditText>(R.id.editPassword).text.toString()

                val organizationNameLayout = findViewById<TextInputLayout>(R.id.organizationNameInputLayout)
                val organizationName = findViewById<TextInputEditText>(R.id.editOrganizationName).text.toString()

                val phonenumberLayout = findViewById<TextInputLayout>(R.id.phoneNumberInputLayout)
                val phonenumber =
                    findViewById<TextInputEditText>(R.id.editPhoneNumber).text.toString()

                val addressLayout = findViewById<TextInputLayout>(R.id.addressInputLayout)
                val address = findViewById<TextInputEditText>(R.id.editAddress).text.toString()

                val cityLayout = findViewById<TextInputLayout>(R.id.cityInputLayout)
                val city = findViewById<TextInputEditText>(R.id.editCity).text.toString()

                val zipcodeLayout = findViewById<TextInputLayout>(R.id.zipcodeInputLayout)
                val zipcode = findViewById<TextInputEditText>(R.id.editZipcode).text.toString()

                val orgRepUsernameLayout = findViewById<TextInputLayout>(R.id.orgRepUsernameInputLayout)
                val orgRepUsername = findViewById<TextInputEditText>(R.id.editOrgRepUsername).text.toString()

                // situational ones

                // Commercial For Profit || Other Non Profit
                // FEIN

                // Healthcare For Profit || Healthcare Non Profit
                // NPI FEIN taxID

                // Federal Governmetn Agency
                // Org Code BureauName

                // State Governmetn Agency || Municipal Agency
                // statedepid fein

                val feinLayout = findViewById<TextInputLayout>(R.id.feinInputLayout)
                val fein = findViewById<TextInputEditText>(R.id.editFEIN).text.toString()

                val npiLayout = findViewById<TextInputLayout>(R.id.npiInputLayout)
                val npi = findViewById<TextInputEditText>(R.id.editNPI).text.toString()

                val taxIDLayout = findViewById<TextInputLayout>(R.id.taxIDInputLayout)
                val taxID = findViewById<TextInputEditText>(R.id.editTaxID).text.toString()

                val orgCodeLayout = findViewById<TextInputLayout>(R.id.organizationCodeInputLayout)
                val orgCode = findViewById<TextInputEditText>(R.id.editOrgCode).text.toString()

                val bureauNameLayout = findViewById<TextInputLayout>(R.id.bureauNameInputLayout)
                val bureauName = findViewById<TextInputEditText>(R.id.editBurName).text.toString()

                val stateDepIDLayout = findViewById<TextInputLayout>(R.id.stateDepIDInputLayout)
                val stateDepID = findViewById<TextInputEditText>(R.id.editStateDepID).text.toString()

                var isValid = true

                var organization = spinnerSpecialty.selectedItem.toString()

                // FEIN Check
                if (organization == "Commercial (For Profit)" || organization == "Other (Non-Profit)" ||
                    organization == "Healthcare (For-Profit)" || organization == "Healthcare (Non-Profit)" ||
                    organization == "State Government Agency" || organization == "Municipal Agency") {
                    if (!isGoodFEIN(fein)) {
                        feinLayout.error = "FEIN must be in format XX-XXXXXXX"
                        isValid = false
                        println("Bad fein")
                    }
                }
                // NPI Check
                if (organization == "Healthcare (For-Profit)" || organization == "Healthcare (Non-Profit)") {
                    if (!isGoodNPI(npi)) {
                        npiLayout.error = "NPI must be 10 digits"
                        isValid = false
                        println("Bad npi")
                    }
                }

                // TaxID Check
                if (organization == "Commercial (For Profit)" || organization == "Other (Non-Profit)" ||
                    organization == "Healthcare (For-Profit)" || organization == "Healthcare (Non-Profit)") {
                    if (taxID.isEmpty()) {
                        taxIDLayout.error = "Missing Tax ID"
                        isValid = false
                        println("Bad Tax ID")
                    }
                }

                // StateDepID Check
                if (organization == "State Government Agency" || organization == "Municipal Agency") {
                    if (stateDepID.isEmpty()) {
                        stateDepIDLayout.error = "Missing StateDepID"
                        isValid = false
                        println("Bad State Dep ID")
                    }
                }

                // Bur Name and Org Code Check
                if (organization == "Federal Government Agency") {
                    if (bureauName.isEmpty()) {
                        bureauNameLayout.error = "Missing Bureau Name"
                        isValid = false
                        println("Missing Bureau Name")
                    }
                    if (!isGoodOrgCode(orgCode)) {
                        orgCodeLayout.error = "Bad Org Code"
                        isValid = false
                        println("Bad Org code")
                    }
                }

                // email check
                if (email.isEmpty()) {
                    emailLayout.error = "Missing Email"
                    isValid = false
                    println("Missing Email")
                }

                // name check
                if (organizationName.isEmpty()) {
                    organizationNameLayout.error = "Missing Org Name"
                    isValid = false
                    println("Missing Org Code")
                }

                if (!isGoodPassword(password)) {
                    passwordLayout.error = "Bad Password"
                    isValid = false
                }

                if (!isGoodPhoneNumber(phonenumber)) {
                    phonenumberLayout.error = "Bad Phone Number"
                    isValid = false
                }

                if (orgRepUsername.isEmpty() || !isGoodOrgUsername(orgRepUsername)) {
                    orgRepUsernameLayout.error = "Needs to have 1 @ or _"
                    isValid = false
                    println("orgRepUsername: '$orgRepUsername'")
                    println("isGoodOrgUsername: ${isGoodOrgUsername(orgRepUsername)}")
                }

                val selectedState = spinnerStates.selectedItem.toString()
//                val addressValid = isGoodAddress(address, city, zipcode, selectedState)
////                val addressValid = isGoodAddress("160 Broadway", "New York", "10038", "NY")
//                if (!addressValid) {
//                    println("❌ Invalid Address")
//                    addressLayout.error = "Invalid Address"
//                    cityLayout.error = "Invalid City"
//                    zipcodeLayout.error = "Invalid Zipcode"
//                    isValid = false
//                }
//                else {
//                    println("✅ Valid Address")
//                }
                lifecycleScope.launch {
                    val addressValid = isGoodAddress(address, city, zipcode, selectedState)
                    println("Result from isGoodAddress: $addressValid")

                    if (!addressValid) {
                        println("❌ Invalid Address")
                        addressLayout.error = "Invalid Address"
                        cityLayout.error = "Invalid City"
                        zipcodeLayout.error = "Invalid Zipcode"
                        isValid = false  // make sure `isValid` is declared in the outer scope
                    } else {
                        println("✅ Valid Address")
                    }
                }

                if (isValid) {
                    signUpUser(
                        organizationName = organizationName,
                        email = email,
                        password = password,
                        phoneNumber = phonenumber,
                        addressLine1 = address,
                        city = city,
                        state = selectedState,
                        zipcode = zipcode,
                        orgType = spinnerSpecialty.selectedItem.toString(),
                        fein = fein,
                        npi = npi,
                        orgCode = orgCode,
                        bureauName = bureauName,
                        stateDepID = stateDepID,
                        taxID = taxID,
                        repUsername = orgRepUsername,
                        onResult = { success, message ->
                            if (success) {
                                println("✅ Signup successful: $message")
                                val intent = Intent(
                                    this@OrganizationSignupActivity,
                                    SuccessfulSignupActivity::class.java
                                )
                                startActivity(intent)
                                // redirect when successful
                            } else {
                                println("❌ Signup failed: $message")
                            }
                        }
                    )
                }
            }

        }

        binding.buttonHomepage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }

    fun addFEINFormatter(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                // Get the raw input and remove any non-digit characters
                val input = s.toString().replace(Regex("[^\\d]"), "")

                // Apply the FEIN format: XX-XXXXXXX
                val formatted = when {
                    input.length <= 2 -> input
                    input.length <= 9 -> "${input.substring(0, 2)}-${input.substring(2)}"
                    else -> "${input.substring(0, 2)}-${input.substring(2, 9)}"
                }

                // Set the formatted text back to the EditText and position the cursor correctly
                editText.setText(formatted)
                editText.setSelection(formatted.length)

                isFormatting = false
            }
        })
    }

//    fun addOrgCodeFormatter(editText: TextInputEditText) {
//        editText.addTextChangedListener(object : TextWatcher {
//            private var isFormatting = false
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                if (isFormatting || s == null) return
//                isFormatting = true
//
//                // Strip to alphanumeric only
//                val input = s.toString().replace(Regex("[^\\d]"), "")
//
//                // Truncate to 10 characters max
//                val trimmed = if (input.length > 10) input.substring(0, 10) else input
//
//                editText.setText(trimmed)
//                editText.setSelection(trimmed.length)
//
//                isFormatting = false
//            }
//        })
//    }

    // Validation for FEIN (XX-XXXXXXX format)
    fun isGoodFEIN(fein: String): Boolean {
        val feinRegex = Regex("^\\d{2}-\\d{7}$")
        return feinRegex.matches(fein)
    }

    // Validation for NPI (10 digits long)
    fun isGoodNPI(npi: String): Boolean {
        val npiRegex = Regex("^\\d{10}$")
        return npiRegex.matches(npi)
    }

    fun isGoodOrgCode(code: String): Boolean {
        val npiRegex = Regex("^\\d{12}$")
        return npiRegex.matches(code)
    }

    fun isGoodOrgUsername(code: String): Boolean {
        val allowedCharsRegex = Regex("^[A-Za-z0-9_@]+$")

        // Check if it matches allowed characters at all
        if (!allowedCharsRegex.matches(code)) {
            return false
        }

        val specialCharCount = code.count { it == '_' || it == '@' }

        return specialCharCount <= 1 && code.isNotEmpty()
    }

    fun addPhoneFormatter(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var prevText = ""
            private var deleting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deleting = count > after // Detect if user is deleting
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                val currentText = s.toString()
                if (currentText == prevText) return

                isFormatting = true

                val digitsOnly = currentText.replace(Regex("[^\\d]"), "")
                val formatted = formatPhoneNumber(digitsOnly)

                if (!deleting || currentText != formatted) {
                    editText.setText(formatted)
                    editText.setSelection(formatted.length.coerceAtMost(editText.text?.length ?: 0))
                }

                prevText = formatted
                isFormatting = false
            }

            private fun formatPhoneNumber(digits: String): String {
                return when (digits.length) {
                    in 0..3 -> digits
                    in 4..6 -> "(${digits.substring(0, 3)}) ${digits.substring(3)}"
                    in 7..10 -> "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
                    else -> "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6, 10)}"
                }
            }
        })
    }

    private fun updateVisibilityBasedOnSelection(selectedItem: String, selectedState: String) {

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

            if (selectedItem == "Commercial (For Profit)" || selectedItem == "Other (Non-Profit)") {
                findViewById<TextInputLayout>(R.id.feinInputLayout).visibility = View.VISIBLE
                if (selectedState !in stateNoTaxID) {
                    findViewById<TextInputLayout>(R.id.taxIDInputLayout).visibility = View.VISIBLE
                }
            }
            else if (selectedItem == "Healthcare (For-Profit)" || selectedItem == "Healthcare (Non-Profit)") {
                findViewById<TextInputLayout>(R.id.npiInputLayout).visibility = View.VISIBLE
                findViewById<TextInputLayout>(R.id.feinInputLayout).visibility = View.VISIBLE
                if (selectedState !in stateNoTaxID) {
                    findViewById<TextInputLayout>(R.id.taxIDInputLayout).visibility = View.VISIBLE
                }
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

}
