package com.example.myapplication_prueba

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.* // <-- IMPORTANTE: Agregamos esto para que reconozca ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

// 1. Creamos el "molde" para los datos que vienen del backend
@Serializable
data class UsuarioPrueba(val id: Int, val nombre: String, val rol: String)

@Serializable
data class Cliente(
    val id: Int? = null,
    val nombre: String,
    val apellido: String,
    val fecha_cumpleanos: String,
    val telefono: String,
    val correo: String
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val success: Boolean, val message: String, val token: String? = null, val rol: String? = null)

@Serializable
data class RegisterRequest(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val password: String
)

@Serializable
data class RegisterResponse(val success: Boolean, val message: String)

class Greeting {
    private val platform = getPlatform()


    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }


    suspend fun greet(): String {
        return try {
            val response = client.get("https://proyecto-backend-ktor-production.up.railway.app/usuarios")

            val usuarios: List<UsuarioPrueba> = response.body()

            "¡Conexión exitosa!\nEl primer usuario es ${usuarios[0].nombre} (${usuarios[0].rol})"

        } catch (e: Exception) {
            "Error de red: ${e.message}"
        }
    }


    suspend fun enviarCliente(nombre: String, apellido: String, cumpleanos: String, telefono: String, correo: String): String {
        return try {

            val nuevoCliente = Cliente(
                nombre = nombre,
                apellido = apellido,
                fecha_cumpleanos = cumpleanos,
                telefono = telefono,
                correo = correo
            )

            val response = client.post("https://proyecto-backend-ktor-production.up.railway.app/clientes") {
                contentType(ContentType.Application.Json)
                setBody(nuevoCliente)
            }
            "¡Enviado con éxito al servidor!"
        } catch (e: Exception) {
            "Error de red: ${e.message}"
        }
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val response = client.post("https://proyecto-backend-ktor-production.up.railway.app/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            // Asumiendo que el backend devuelve un objeto LoginResponse
            response.body()
        } catch (e: Exception) {
            LoginResponse(false, "Error de red: ${e.message}")
        }
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return try {
            val response = client.post("https://proyecto-backend-ktor-production.up.railway.app/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                response.body()
            } else if (response.status == HttpStatusCode.Conflict) {
                RegisterResponse(false, "Este correo ya está registrado (409)")
            } else {
                RegisterResponse(false, "Error del servidor: ${response.status.value}")
            }
        } catch (e: Exception) {
            RegisterResponse(false, "Error de red o formato: ${e.message}")
        }
    }
}