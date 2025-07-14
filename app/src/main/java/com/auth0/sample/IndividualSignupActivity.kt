package com.auth0.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.util.Patterns
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.auth0.sample.databinding.ActivityIndividualSignupBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import android.text.TextWatcher
import android.text.Editable
import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import com.auth0.sample.signUpUser
import com.auth0.sample.isGoodPassword
import  com.auth0.sample.isGoodPhoneNumber
import com.auth0.sample.isGoodAddress


class IndividualSignupActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private lateinit var binding: ActivityIndividualSignupBinding
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualSignupBinding.inflate(layoutInflater)
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

        // filters
        setEmailCharacterFilter(findViewById<TextInputEditText>(R.id.editEmail))
        setPasswordCharacterFilter(findViewById<TextInputEditText>(R.id.editPassword))
        setCharacterFilter(findViewById<TextInputEditText>(R.id.editFirstName), "a-zA-Z \\-'")
        setCharacterFilter(findViewById<TextInputEditText>(R.id.editMiddleName), "a-zA-Z \\-'")
        setCharacterFilter(findViewById<TextInputEditText>(R.id.editLastName), "a-zA-Z \\-'")

        setCharacterFilter(findViewById<TextInputEditText>(R.id.editAddress), "A-Za-z0-9 \\-")
        setCharacterFilter(findViewById<TextInputEditText>(R.id.editCity), "A-Za-z \\-")

        addPhoneFormatter(findViewById<TextInputEditText>(R.id.editPhoneNumber))
        addSSNFormatter(findViewById<TextInputEditText>(R.id.editSSN))
        addDOBFormatter(findViewById<TextInputEditText>(R.id.editDoB), this)

        // call signup user when signup button is clicked
        binding.buttonSubmitSignup.setOnClickListener {
            lifecycleScope.launch {
                val emailLayout = findViewById<TextInputLayout>(R.id.emailInputLayout)
                val email = findViewById<TextInputEditText>(R.id.editEmail).text.toString()

                val passwordLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
                val password = findViewById<TextInputEditText>(R.id.editPassword).text.toString()

                val firstnameLayout = findViewById<TextInputLayout>(R.id.firstNameInputLayout)
                val firstname = findViewById<TextInputEditText>(R.id.editFirstName).text.toString()

                val middlename =
                    findViewById<TextInputEditText>(R.id.editMiddleName).text.toString()

                val lastnameLayout = findViewById<TextInputLayout>(R.id.lastNameInputLayout)
                val lastname = findViewById<TextInputEditText>(R.id.editLastName).text.toString()

                val phonenumberLayout = findViewById<TextInputLayout>(R.id.phoneNumberInputLayout)
                val phonenumber =
                    findViewById<TextInputEditText>(R.id.editPhoneNumber).text.toString()

                val ssnLayout = findViewById<TextInputLayout>(R.id.ssnInputLayout)
                val ssn = findViewById<TextInputEditText>(R.id.editSSN).text.toString()

                val dobLayout = findViewById<TextInputLayout>(R.id.dobInputLayout)
                val dob = findViewById<TextInputEditText>(R.id.editDoB).text.toString()

                val addressLayout = findViewById<TextInputLayout>(R.id.addressInputLayout)
                val address = findViewById<TextInputEditText>(R.id.editAddress).text.toString()

                val cityLayout = findViewById<TextInputLayout>(R.id.cityInputLayout)
                val city = findViewById<TextInputEditText>(R.id.editCity).text.toString()

                val zipcodeLayout = findViewById<TextInputLayout>(R.id.zipcodeInputLayout)
                val zipcode = findViewById<TextInputEditText>(R.id.editZipcode).text.toString()


//            val choice = findViewById<TextInputEditText>(R.id.spinnerSpecialty).text.toString()
//            val state = findViewById<TextInputEditText>(R.id.spinnerStates).text.toString()

                var isValid = true
                // ----- check values ----------

                // email check
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailLayout.error = "Invalid Email Address"
                    isValid = false
                }

                // password check
                if (!isGoodPassword(password)) {
                    passwordLayout.error =
                        "Invalid Password must be 8 long and contain capital and special character"
                    isValid = false
                }

                //firtname, lastname check
                if (firstname.isEmpty()) {
                    firstnameLayout.error = "Missing First Name"
                    isValid = false
                }
                if (lastname.isEmpty()) {
                    lastnameLayout.error = "Missing Last Name"
                    isValid = false
                }

                // phonenumber check
                if (!isGoodPhoneNumber(phonenumber)) {
                    phonenumberLayout.error = "Invalid Phone Number"
                    isValid = false
                }

                if (ssn.isEmpty()) {
                    ssnLayout.error = "Missing SSN"
                    isValid = false
                }

                // call backend api for valid address
//                isGoodAddress(address, city, zipcode) { success, message ->
//                    if (success) {
//                        println("✅ Valid Address: $message")
//                    } else {
//                        println("❌ Invalid Address: $message")
//                        addressLayout.error = "Invalid Address"
//                        cityLayout.error = "Invalid City"
//                        zipcodeLayout.error = "Invalid Zipcode"
//                        isValid = false
//                    }
//                }
//                val selectedState = spinnerStates.selectedItem.toString()
//                println("Calling isGoodAddress")
//                val addressValid = isGoodAddress(address, city, zipcode, selectedState)
//                println("Result from isGoodAddress: $addressValid")
////
////                lifecycleScope.launch {
////                    val valid = isGoodAddress(address, city, zipcode, selectedState)
////                    println("Result from isGoodAddress 4: $valid")
////                }
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
                val selectedState = spinnerStates.selectedItem.toString()
                println("Calling isGoodAddress")

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

                // SSN Check
                if (isValid) {
                    signUpUser(
                        email = email,
                        password = password,
                        firstName = firstname,
                        middleName = middlename,
                        lastName = lastname,
                        phoneNumber = phonenumber,
                        specialtyAuth = spinnerSpecialty.selectedItem.toString(),
                        ssn = ssn,
                        dateOfBirth = dob,
                        addressLine1 = address,
                        city = city,
                        state = spinnerStates.selectedItem.toString(),
                        zipcode = zipcode,
                        onResult = { success, message ->
                            if (success) {
                                println("✅ Signup successful: $message")
                                val intent = Intent(this@IndividualSignupActivity, SuccessfulSignupActivity::class.java)
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

    // testing
    // ask if he wants to get it from the backend or just hardcode it?

//    fun signUpUser(
//        email: String,
//        password: String,
//        firstName: String,
//        middleName: String,
//        lastName: String,
//        phoneNumber: String,
//        specialtyAuth: String,
//        ssn: String,
//        dateOfBirth: String,
//        addressLine1: String,
//        city: String,
//        state: String,
//        zipcode: String,
//        orgType: String? = null,
//        backendUrl: String = "http://10.0.2.2:8000/signup",
//        onResult: (Boolean, String) -> Unit
//    ) {
//        val client = HttpClient(CIO) {
//            install(ContentNegotiation) {
//                json(Json { ignoreUnknownKeys = true })
//            }
//        }
//
//        val jsonPayload = buildJsonObject {
//            put("email", email)
//            put("password", password)
//            put("first_name", firstName)
//            put("middle_name", middleName)
//            put("last_name", lastName)
//            put("phone_number", phoneNumber)
//            put("specialty_auth", specialtyAuth)
//            put("ssn", ssn)
//            put("date_of_birth", dateOfBirth)
//            put("address_line_1", addressLine1)
//            put("city", city)
//            put("state", state)
//            put("zipcode", zipcode)
//            orgType?.let { put("org_type", it) }
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response: HttpResponse = client.post(backendUrl) {
//                    contentType(ContentType.Application.Json)
//                    setBody(jsonPayload)
//                }
//
//                val responseText = response.bodyAsText()
//
//                if (response.status.isSuccess()) {
//                    onResult(true, responseText)
//                } else {
//                    onResult(false, "Signup error (${response.status.value}): $responseText")
//                }
//
//            } catch (e: Exception) {
//                onResult(false, "Signup failed: ${e.message}")
//            } finally {
//                client.close()
//            }
//        }
//    }


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

    fun addSSNFormatter(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var prevText = ""
            private var deleting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                val currentText = s.toString()
                if (currentText == prevText) return

                isFormatting = true

                val digitsOnly = currentText.replace(Regex("[^\\d]"), "")
                val formatted = formatSSN(digitsOnly)

                if (!deleting || currentText != formatted) {
                    editText.setText(formatted)
                    editText.setSelection(formatted.length.coerceAtMost(editText.text?.length ?: 0))
                }

                prevText = formatted
                isFormatting = false
            }

            private fun formatSSN(digits: String): String {
                val length = digits.length
                return when {
                    length <= 3 -> digits
                    length <= 5 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                    length <= 9 -> "${digits.substring(0, 3)}-${digits.substring(3, 5)}-${digits.substring(5)}"
                    else -> "${digits.substring(0, 3)}-${digits.substring(3, 5)}-${digits.substring(5, 9)}"
                }
            }
        })
    }

    fun addDOBFormatter(editText: TextInputEditText, context: Context) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)

        // Add a click listener to open DatePicker dialog
        editText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    // Format and set the date when the user selects it
                    calendar.set(year, monthOfYear, dayOfMonth)
                    val selectedDate = dateFormat.format(calendar.time)
                    editText.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Add a TextWatcher to ensure that the date is always formatted correctly
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var prevText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                val currentText = s.toString()
                if (currentText == prevText) return

                isFormatting = true

                // Remove non-digit characters (like -)
                val digitsOnly = currentText.replace(Regex("[^\\d]"), "")

                // Format the date in the "yyyy-MM-dd" format
                val formattedDate = formatDOB(digitsOnly)

                if (currentText != formattedDate) {
                    editText.setText(formattedDate)
                    editText.setSelection(formattedDate.length.coerceAtMost(editText.text?.length ?: 0))
                }

                prevText = formattedDate
                isFormatting = false
            }

            private fun formatDOB(digits: String): String {
                return when (digits.length) {
                    in 0..2 -> digits
                    in 3..4 -> "${digits.substring(0, 2)}-${digits.substring(2)}"
                    in 5..8 -> "${digits.substring(0, 2)}-${digits.substring(2, 4)}-${digits.substring(4)}"
                    else -> "${digits.substring(0, 2)}-${digits.substring(2, 4)}-${digits.substring(4, 8)}"
                }
            }
        })
    }


//    fun isGoodPhoneNumber(phone: String): Boolean {
//        val digitsOnly = phone.replace("\\D".toRegex(), "") // Remove dashes and any non-digits
//        return digitsOnly.length == 10
//    }

    








//    suspend fun createUserInAuth0() {
//        // get management api token
//
//        val token = "YOUR_MANAGEMENT_API_TOKEN"
//        val domain = getString(R.string.com_auth0_domain) // like: your-tenant.us.auth0.com
//
//        val client = HttpClient(CIO) {
//            install(ContentNegotiation) {
//                json()
//            }
//        }
//
//        val newUser = NewUser(
//            email = "test@example.com",
//            password = "StrongPassword123!",
//            userMetadata = mapOf("role" to "admin", "signup_source" to "kotlin-app")
//        )
//
//        val response = client.post("https://$domain/api/v2/users") {
//            headers {
//                append(HttpHeaders.Authorization, "Bearer $token")
//                append(HttpHeaders.ContentType, "application/json")
//            }
//            setBody(newUser)
//        }
//
//        val responseBody = response.bodyAsText()
//        println("User created: $responseBody")
//    }

}