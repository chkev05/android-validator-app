package com.auth0.sample

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
    firstName: String,
    middleName: String,
    lastName: String,
    phoneNumber: String,
    specialtyAuth: String,
    ssn: String,
    dateOfBirth: String,
    addressLine1: String,
    city: String,
    state: String,
    zipcode: String,
    orgType: String? = null,
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
        put("phone_number", phoneNumber)
        put("specialty_auth", specialtyAuth)
        put("address_line_1", addressLine1)
        put("city", city)
        put("state", state)
        put("zipcode", zipcode)

        orgType?.let { organizationType ->
            put("org_type", organizationType)
            put("org_name", "orgName")

            when (organizationType) {
                in listOf("Commercial (For Profit)", "Other (Non-Profit)") -> {
                    put("fein", "fein") // Example of adding hospital-specific field
                }
                in listOf("Healthcare (For-Profit)", "Healthcare (Non-Profit)") -> {
                    put("npi", "fein")
                    put("fein", "fein")
                    put("taxID", "fein")
                }
                "Federal Government Agency" -> {
                    put("orgCode", "fein")
                    put("burName", "fein")
                }
                in listOf("State Government Agency", "Municipal Agency") -> {
                    put("stateDepID", "fein")
                    put("fein", "fein")
                }
                else -> {
                    // Additional conditions for other org types if needed
                }
            }
        } ?: run {
            // This block will execute if orgType is null
            put("first_name", firstName)
            put("middle_name", middleName)
            put("last_name", lastName)
            put("ssn", ssn)
            put("date_of_birth", dateOfBirth)
        }

    }

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
    if (password.length < 8) return false

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

    val typesMatched = listOf(hasLower, hasUpper, hasDigit, hasSpecial).count { it }
    return typesMatched >= 3
}

fun isGoodPhoneNumber(phone: String): Boolean {
    val cleaned = phone.replace("\\D".toRegex(), "") // Remove non-digit characters
    return cleaned.length in 10..15 // Adjust length based on your needs
}

suspend fun isGoodAddress(
    address: String,
    city: String,
    zipcode: String,
    state: String,
    backendUrl: String = "http://10.0.2.2:8000/validate-address"
): Boolean {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    return try {
        val jsonPayload = buildJsonObject {
            put("address", address)
            put("city", city)
            put("zipcode", zipcode)
            put("state", state)
        }

        val response: HttpResponse = client.post(backendUrl) {
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }

        // Parse the response body as a JSON element and extract the boolean value
        val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonPrimitive

        // Return the boolean value from the response
        jsonResponse.boolean
    } catch (e: Exception) {
        false
    } finally {
        client.close()
    }
}


