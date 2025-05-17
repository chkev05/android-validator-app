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
        put("first_name", firstName)
        put("middle_name", middleName)
        put("last_name", lastName)
        put("phone_number", phoneNumber)
        put("specialty_auth", specialtyAuth)
        put("ssn", ssn)
        put("date_of_birth", dateOfBirth)
        put("address_line_1", addressLine1)
        put("city", city)
        put("state", state)
        put("zipcode", zipcode)
        orgType?.let { put("org_type", it) }
    }

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