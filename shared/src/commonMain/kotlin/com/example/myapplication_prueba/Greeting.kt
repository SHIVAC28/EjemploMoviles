package com.example.myapplication_prueba

// --- ESTO SE AGREGÓ ---
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Greeting {
    private val platform = getPlatform()

    // --- ESTO SE AGREGÓ ---
    private val client = HttpClient()

    // --- ESTO SE MODIFICÓ ---
    suspend fun greet(): String {
        return try {
            val response = client.get("https://proyecto-backend-ktor-production.up.railway.app/")
            response.bodyAsText()
        } catch (e: Exception) {
            "Error de red: ${e.message}"
        }
    }
}