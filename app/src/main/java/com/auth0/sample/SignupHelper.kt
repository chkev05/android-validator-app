package com.auth0.sample

import android.text.InputFilter
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

fun signUpUser(
    email: String,
    password: String,
    firstName: String? = null,
    middleName: String = "",
    lastName: String? = null,
    phoneNumber: String,
    specialtyAuth: String? = null,
    ssn: String? = null,
    dateOfBirth: String? = null,
    addressLine1: String,
    city: String,
    state: String,
    zipcode: String,
    orgType: String? = null,
    // after this line its all org types
    organizationName: String? = null,
    fein: String? = null,
    npi: String? = null,
    orgCode: String? = null,
    bureauName: String? = null,
    stateDepID: String? = null,
    repUsername: String? = null,
    taxID: String? = null,
    backendUrl: String = "http://10.0.2.2:8000/signup",
    onResult: (Boolean, String) -> Unit
) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val jsonPayload = buildJsonObject {
        put("email", email)
        put("password", password)
        put("phone", phoneNumber)
        put("address_line_1", addressLine1)
        put("city", city)
        put("state", state)
        put("zipcode", zipcode)
        if (!orgType.isNullOrBlank()) {
            put("org_type", orgType)
            put("org_name", organizationName)
            put("org_RepUsername", repUsername)

            when (orgType) {
                in listOf("Commercial (For Profit)", "Other (Non-Profit)") -> {
                    put("fein", fein)
                    put("taxID", taxID)
                }
                in listOf("Healthcare (For-Profit)", "Healthcare (Non-Profit)") -> {
                    put("npi", npi)
                    put("fein", fein)
                    put("taxID", taxID)
                }
                "Federal Government Agency" -> {
                    put("orgCode", orgCode)
                    put("burName", bureauName)
                }
                in listOf("State Government Agency", "Municipal Agency") -> {
                    put("stateDepID", stateDepID)
                    put("fein", fein)
                }
            }
        } else {
            put("first_name", firstName)
            put("middle_name", middleName)
            put("last_name", lastName)
            put("ssn", ssn)
            put("date_of_birth", dateOfBirth)
            put("specialty_auth", specialtyAuth)
        }
    }

//    val jsonPayload = buildJsonObject {
//        put("email", email)
//        put("password", password)
//        put("phone_number", phoneNumber)
//        put("specialty_auth", specialtyAuth)
//        put("address_line_1", addressLine1)
//        put("city", city)
//        put("state", state)
//        put("zipcode", zipcode)
//
//        orgType?.let { organizationType ->
//            put("org_type", organizationType)
//            put("org_name", organizationName)
//
//            when (organizationType) {
//                in listOf("Commercial (For Profit)", "Other (Non-Profit)") -> {
//                    put("fein", fein)
//                    put("taxID", taxID)
//                }
//                in listOf("Healthcare (For-Profit)", "Healthcare (Non-Profit)") -> {
//                    put("npi", npi)
//                    put("fein", fein)
//                    put("taxID", taxID)
//                }
//                "Federal Government Agency" -> {
//                    put("orgCode", orgCode)
//                    put("burName", bureauName)
//                }
//                in listOf("State Government Agency", "Municipal Agency") -> {
//                    put("stateDepID", stateDepID)
//                    put("fein", fein)
//                }
//                else -> {
//                    // Additional conditions for other org types if needed
//                }
//            }
//        } ?: run {
//            // This block will execute if orgType is null
//            put("first_name", firstName)
//            put("middle_name", middleName)
//            put("last_name", lastName)
//            put("ssn", ssn)
//            put("date_of_birth", dateOfBirth)
//        }
//
//    }

//    // edit this to chagne depending on org or individaul
//    val jsonPayload1 = buildJsonObject {
//        put("email", email)
//        put("password", password)
//        put("first_name", firstName)
//        put("middle_name", middleName)
//        put("last_name", lastName)
//        put("phone_number", phoneNumber)
//        put("specialty_auth", specialtyAuth)
//        put("ssn", ssn)
//        put("date_of_birth", dateOfBirth)
//        put("address_line_1", addressLine1)
//        put("city", city)
//        put("state", state)
//        put("zipcode", zipcode)
//        orgType?.let { put("org_type", it) }
//    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response: HttpResponse = client.post(backendUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }

            val responseText = response.bodyAsText()

            if (response.status.isSuccess()) {
                onResult(true, responseText)
            } else {
                onResult(false, "Signup error (${response.status.value}): $responseText")
            }

        } catch (e: Exception) {
            onResult(false, "Signup failed: ${e.message}")
        } finally {
            client.close()
        }
    }
}


fun isGoodPassword(password: String): Boolean {
    if (password.length < 12) return false

    var hasLower = false
    var hasUpper = false
    var hasDigit = false
    var hasSpecial = false

    for (char in password) {
        when {
            char.isLowerCase() -> hasLower = true
            char.isUpperCase() -> hasUpper = true
            char.isDigit() -> hasDigit = true
            !char.isLetterOrDigit() -> hasSpecial = true
        }
    }

    return hasLower && hasUpper && hasDigit && hasSpecial
}

fun isGoodPhoneNumber(phone: String): Boolean {
    val regex = Regex("^\\(\\d{3}\\) \\d{3}-\\d{4}$")
    return regex.matches(phone)
}

//suspend fun isGoodAddress(
//    address: String,
//    city: String,
//    zipcode: String,
//    state: String,
//    backendUrl: String = "http://10.0.2.2:8000/validate-address"
////    backendUrl: String = "http://127.0.0.1:8000/validate-address"
//): Boolean {
//    val client = HttpClient(CIO) {
//        install(ContentNegotiation) {
//            json(Json { ignoreUnknownKeys = true })
//        }
//    }
//    return try {
//        println("Sending address: $address, city: $city, zip: $zipcode, state: $state")
//
//        val jsonPayload = buildJsonObject {
//            put("address", address)
//            put("city", city)
//            put("zipcode", zipcode)
//            put("state", state)
//        }
//
//        val response: HttpResponse = client.post(backendUrl) {
//            contentType(ContentType.Application.Json)
//            setBody(jsonPayload)
//        }
//
//        println("Status code: ${response.status.value}")
//        // Parse the response body as a JSON element and extract the boolean value
////        val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonPrimitive
//        val responseBody = response.bodyAsText()
//        println("Status code: ${response.status.value}")
//        println("Response body: $responseBody")
//
//        val jsonResponse = Json.parseToJsonElement(responseBody).jsonPrimitive
//        println("Parsed boolean: $jsonResponse")
//
//        // Return the boolean value from the response
//        println(jsonResponse)
//        jsonResponse.boolean
//    } catch (e: Exception) {
//        println("Exception in isGoodAddress: ${e.localizedMessage}")
//        e.printStackTrace()
//        false
//    } finally {
//        client.close()
//    }
//}

suspend fun isGoodAddress(
    address: String,
    city: String,
    zipcode: String,
    state: String,
    backendUrl: String = "http://10.0.2.2:8000/validate-address"
): Boolean {
    println("isGoodAddress() called with params: $address, $city, $zipcode, $state")
    val client = try {
        println("Creating HttpClient")
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    } catch (e: Exception) {
        println("HttpClient creation failed: ${e.localizedMessage}")
        e.printStackTrace()
        return false
    }

    try {
        println("Building JSON payload")
        val jsonPayload = buildJsonObject {
            put("address", address)
            put("city", city)
            put("zipcode", zipcode)
            put("state", state)
        }
        println("JSON payload: $jsonPayload")

        println("Sending request to $backendUrl")
        val response: HttpResponse = client.post(backendUrl) {
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }
        println("Response status code: ${response.status.value}")

        val responseBody = response.bodyAsText()
        println("Response body: $responseBody")

        val jsonResponse = Json.parseToJsonElement(responseBody).jsonPrimitive
        println("Parsed response boolean: $jsonResponse")

        val result = jsonResponse.boolean
        println("Returning result: $result")
        return result
    } catch (e: Exception) {
        println("Exception caught in request: ${e.localizedMessage}")
        e.printStackTrace()
        return false
    } finally {
        println("Closing HttpClient")
        client.close()
        println("HttpClient closed")
    }
}


fun setEmailCharacterFilter(editText: TextInputEditText) {
    val allowedChars = "[a-zA-Z0-9@._-]+"

    val filter = InputFilter { source, _, _, _, _, _ ->
        if (source.isEmpty()) return@InputFilter null // Allow backspace/delete

        // If all characters match allowed set, return null (i.e., keep input)
        if (source.matches(Regex(allowedChars))) null
        else "" // Otherwise, block input
    }

    editText.filters = arrayOf(filter)
}

fun setPasswordCharacterFilter(editText: TextInputEditText) {
    // Allowed characters: letters, digits, and common special characters
    val allowedChars = "[a-zA-Z0-9!@#\$%^&*()\\-_=+\\[\\]{}|;:'\",.<>?/`~\\\\]+"

    val filter = InputFilter { source, _, _, _, _, _ ->
        if (source.isEmpty()) return@InputFilter null // Allow backspace/delete

        if (source.matches(Regex(allowedChars))) null
        else ""
    }

    editText.filters = arrayOf(filter)
}

//fun setOrgUsernameFilter(editText: TextInputEditText) {
//    val filter = InputFilter { source, _, _, dest, _, _ ->
//        if (source.isEmpty()) return@InputFilter null  // allow deletions
//
//        val allowed = StringBuilder()
//
//        // Check if dest already contains a special character
//        val hasSpecialChar = dest.any { it == '_' || it == '@' }
//
//        for (char in source) {
//            when {
//                char.isLetterOrDigit() -> allowed.append(char)
//                (char == '_' || char == '@') && !hasSpecialChar && !allowed.contains('_') && !allowed.contains('@') -> allowed.append(char)
//                // skip anything else
//            }
//        }
//
//        // If everything was valid, return null to keep original
//        if (allowed.toString() == source.toString()) null else allowed.toString()
//    }
//
//    editText.filters = arrayOf(filter)
//}

// Character filter function
fun setCharacterFilter(editText: TextInputEditText, allowedCharsPattern: String) {
    // Build regex from the allowed characters pattern string
    val allowedRegex = Regex("[$allowedCharsPattern]+")

    val filter = InputFilter { source, _, _, _, _, _ ->
        if (source.isEmpty()) return@InputFilter null // allow deletion/backspace

        if (source.matches(allowedRegex)) null else ""
    }

    editText.filters = arrayOf(filter)
}
