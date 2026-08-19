package com.example.myapplication_prueba

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- MODELOS DE DATOS ---

@Serializable
data class UsuarioPrueba(val id: Int, val nombre: String, val rol: String)

@Serializable
data class Cliente(
    val id: Int? = null,
    val nombre: String,
    val apellido: String,
    @SerialName("fecha_cumpleanos") val fecha_cumpleanos: String? = null,
    val telefono: String,
    val correo: String,
    @SerialName("fecha_registro") val fechaRegistro: String? = null,
    val estado: String = "active",
    val direccion: String? = null,
    val notas: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null
)

@Serializable
data class ClienteStats(
    @SerialName("total_global") val totalGlobal: Int,
    val activos: Int,
    val inactivos: Int
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val success: Boolean, 
    val message: String, 
    val token: String? = null, 
    val rol: String? = null
)

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

@Serializable
data class PerfilCliente(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val fechaNacimiento: String? = null,
    val direccion: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null
)

@Serializable
data class ProfileUpdateRequest(
    val nombres: String,
    val apellidos: String,
    val telefono: String,
    val fechaNacimiento: String? = null,
    val direccion: String? = null,
    val password: String? = null
)

@Serializable
data class PerfilAdmin(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val rol: String,
    val telefono: String? = null
)

@Serializable
data class AdminProfileUpdateRequest(
    val nombres: String,
    val apellidos: String,
    val telefono: String,
    val email: String,
    val password: String? = null
)

@Serializable
data class AdminUser(
    val id: Int? = null,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null
)

@Serializable
data class AdminUserRequest(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val password: String
)

@Serializable
data class Barber(
    val id: Int? = null,
    @SerialName("nombre_completo") val nombreCompleto: String,
    val telefono: String? = null,
    val email: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val activo: Boolean = true,
    val bio: String? = null,
    @SerialName("configuracion_horario") val scheduleConfiguration: String? = null,
    @SerialName("especialidades") val specialties: List<String> = emptySet<String>().toList()
)

@Serializable
data class BarberRequest(
    val id: Int? = null,
    @SerialName("nombre_completo") val nombreCompleto: String,
    val telefono: String? = null,
    val email: String? = null,
    val activo: Boolean = true,
    val bio: String? = null,
    @SerialName("especialidades") val specialties: List<String> = emptyList(),
    @SerialName("configuracion_horario") val scheduleConfiguration: String? = null,
    val password: String? = null
)

@Serializable
data class BarberStats(
    @SerialName("total_barberos") val totalBarbers: Int,
    @SerialName("barberos_activos") val activeBarbers: Int,
    @SerialName("barberos_off") val offBarbers: Int
)

@Serializable
data class ServiceCategory(
    val id: Int,
    val nombre: String
)

@Serializable
data class Service(
    val id: Int? = null,
    val nombre: String,
    val precio: Double? = null,
    val duracion: Int? = null,
    val activo: Boolean = true,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("service_category") val serviceCategory: ServiceCategory? = null,
    val descripcion: String? = null
)

@Serializable
data class Promotion(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String? = null,
    @SerialName("precio_original") val precioOriginal: Double? = null,
    @SerialName("precio_promocional") val precioPromocional: Double,
    val activo: Boolean = true,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_final") val fechaFinal: String,
    @SerialName("selected_service_ids") val selectedServiceIds: List<Int> = emptyList(),
    @SerialName("nombre_servicios") val nombreServicios: List<String> = emptyList()
)

@Serializable
data class ServiceStats(
    val totalServices: Int,
    val totalPromotions: Int
)

@Serializable
data class Product(
    val id: Int? = null,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val sku: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val activo: Boolean = true,
    @SerialName("product_category") val category: ServiceCategory? = null,
    val descripcion: String? = null
)

@Serializable
data class InventoryStats(
    @SerialName("total_products") val totalProducts: Int,
    @SerialName("low_stock") val lowStock: Int,
    @SerialName("inventory_value") val inventoryValue: Double
)

@Serializable
data class ReportStats(
    val totalApps: Int,
    val totalIncome: Double,
    val topBarberName: String? = null,
    val topBarberCount: Int = 0,
    val topBarberImage: String? = null
)

@Serializable
data class SoldProduct(
    val name: String,
    val quantity: Int,
    val price: Double,
    val date: String,
    val customer: String
)

@Serializable
data class SaleDetail(
    val name: String,
    val price: String
)

@Serializable
data class QuickReduceResponse(
    val success: Boolean,
    val message: String,
    val newStock: Int? = null
)

@Serializable
data class CartItem(
    val id: Int,
    val type: String,
    val name: String,
    val price: Double,
    val duration: Int = 0
)

@Serializable
data class GhostAppointmentRequest(
    val barberId: Int,
    val paymentMethod: String,
    val cartItems: List<CartItem>,
    val amountReceived: Double,
    val customerId: Int? = null,
    val ghostName: String? = null
)

@Serializable
data class Appointment(
    val id: Int,
    val customer: Cliente? = null,
    val date: String,
    val startTime: String,
    val endTime: String? = null,
    val status: String,
    val service: Service? = null,
    val barber: Barber? = null,
    val promotion: String? = null,
    val totalPrice: Double = 0.0,
    val paymentMethod: String? = null
)

// --- CLIENTE API ---

object ApiClient {
    var sessionToken: String? = null

    fun getClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 20000
                connectTimeoutMillis = 20000
                socketTimeoutMillis = 20000
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
                sessionToken?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
            }
        }
    }
    const val BASE_URL = "https://proyecto-backend-ktor-production.up.railway.app"
}

class Greeting {
    private val baseUrl = ApiClient.BASE_URL
    
    private fun getClient() = ApiClient.getClient()

    suspend fun greet(): String {
        return try {
            val response = getClient().get("$baseUrl/usuarios")
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
            val response = getClient().post("$baseUrl/clientes") {
                setBody(nuevoCliente)
            }
            if (response.status.isSuccess()) {
                "¡Enviado con éxito al servidor!"
            } else {
                val errorMsg = try { response.bodyAsText() } catch (e: Exception) { "Error desconocido" }
                "Error del servidor (${response.status.value}): $errorMsg"
            }
        } catch (e: Exception) {
            "Error de red: ${e.message}"
        }
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val response = getClient().post("$baseUrl/login") {
                setBody(LoginRequest(email, password))
            }
            if (response.status.isSuccess()) {
                val loginRes = response.body<LoginResponse>()
                ApiClient.sessionToken = loginRes.token
                loginRes
            } else {
                val errorMsg = try { response.bodyAsText() } catch (e: Exception) { "Error desconocido" }
                LoginResponse(false, "Error (${response.status.value}): $errorMsg")
            }
        } catch (e: Exception) {
            LoginResponse(false, "Error de red: ${e.message}")
        }
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/register") {
                setBody(request)
            }
            if (response.status.isSuccess()) {
                response.body<RegisterResponse>()
            } else if (response.status == HttpStatusCode.Conflict) {
                RegisterResponse(false, "Este correo ya está registrado (409)")
            } else {
                val errorMsg = try { response.bodyAsText() } catch (e: Exception) { "Error del servidor" }
                RegisterResponse(false, "Error (${response.status.value}): $errorMsg")
            }
        } catch (e: Exception) {
            RegisterResponse(false, "Error de red o formato: ${e.message}")
        }
    }

    suspend fun getProfile(): PerfilCliente? {
        return try {
            val response = getClient().get("$baseUrl/customer/profile")
            val responseText = response.bodyAsText()
            println("DEBUG: Perfil recibido: $responseText")
            
            if (response.status == HttpStatusCode.OK) {
                Json { ignoreUnknownKeys = true }.decodeFromString<PerfilCliente>(responseText)
            } else null
        } catch (e: Exception) {
            println("DEBUG: Error cargando perfil: ${e.message}")
            null
        }
    }

    suspend fun updateProfile(perfil: PerfilCliente, newPassword: String?): RegisterResponse {
        return try {
            val request = ProfileUpdateRequest(
                nombres = perfil.nombres,
                apellidos = perfil.apellidos,
                telefono = perfil.telefono,
                fechaNacimiento = perfil.fechaNacimiento,
                direccion = perfil.direccion,
                password = if (newPassword.isNullOrBlank()) null else newPassword
            )

            println("DEBUG: Enviando actualización de perfil: $request")

            val response = getClient().put("$baseUrl/customer/profile/update") {
                setBody(request)
            }

            val responseText = response.bodyAsText()
            println("DEBUG: Respuesta de actualización: $responseText")
            
            if (response.status.isSuccess()) {
                try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<RegisterResponse>(responseText)
                } catch (e: Exception) {
                    RegisterResponse(true, "Perfil actualizado")
                }
            } else {
                RegisterResponse(false, "Error (${response.status.value}): $responseText")
            }
        } catch (e: Exception) {
            println("DEBUG: Excepción en updateProfile: ${e.message}")
            RegisterResponse(false, "Error de comunicación: ${e.message}")
        }
    }

    suspend fun getAdminProfile(): PerfilAdmin? {
        return try {
            val response = getClient().get("$baseUrl/admin/profile")
            if (response.status == HttpStatusCode.OK) {
                response.body<PerfilAdmin>()
            } else {
                val errorText = response.bodyAsText()
                println("Error en getAdminProfile: ${response.status} - $errorText")
                null
            }
        } catch (e: Exception) {
            println("Excepción en getAdminProfile: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getAdministrators(): List<AdminUser> {
        return try {
            val response = getClient().get("$baseUrl/admin/list")
            if (response.status == HttpStatusCode.OK) {
                response.body<List<AdminUser>>()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addAdministrator(request: AdminUserRequest): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/add") {
                setBody(request)
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) {
                try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<RegisterResponse>(responseText)
                } catch (e: Exception) {
                    RegisterResponse(true, "Admin agregado")
                }
            } else {
                RegisterResponse(false, "Error (${response.status.value}): $responseText")
            }
        } catch (e: Exception) {
            RegisterResponse(false, "Error de red: ${e.message}")
        }
    }

    suspend fun updateAdminProfile(request: AdminProfileUpdateRequest): RegisterResponse {
        return try {
            val response = getClient().put("$baseUrl/admin/profile/update") {
                setBody(request)
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) {
                try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<RegisterResponse>(responseText)
                } catch (e: Exception) {
                    RegisterResponse(true, "Perfil actualizado")
                }
            } else {
                RegisterResponse(false, "Error (${response.status.value}): $responseText")
            }
        } catch (e: Exception) {
            RegisterResponse(false, "Error de red o formato: ${e.message}")
        }
    }

    suspend fun verifyPassword(password: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/auth/verify-password") {
                setBody(mapOf("password" to password))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Verificado")
            else RegisterResponse(false, "Contraseña incorrecta")
        } catch (e: Exception) { RegisterResponse(false, "Error de conexión") }
    }

    suspend fun changePassword(newPassword: String): RegisterResponse {
        return try {
            val response = getClient().put("$baseUrl/auth/change-password") {
                setBody(mapOf("newPassword" to newPassword))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Contraseña actualizada")
            else RegisterResponse(false, "Error al actualizar")
        } catch (e: Exception) { RegisterResponse(false, "Error de conexión") }
    }

    suspend fun getAppointments(date: String): List<Appointment> {
        return try {
            val response = getClient().get("$baseUrl/admin/appointments") {
                parameter("date", date)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<List<Appointment>>()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun cancelAppointment(id: Int): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/appointments/$id/cancel")
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) {
                RegisterResponse(true, "Cita cancelada con éxito")
            } else {
                RegisterResponse(false, "Error: $responseText")
            }
        } catch (e: Exception) {
            RegisterResponse(false, "Error de red")
        }
    }

    suspend fun getServices(): List<Service> {
        return try {
            val response = getClient().get("$baseUrl/admin/services")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getPromotions(): List<Promotion> {
        return try {
            val response = getClient().get("$baseUrl/admin/promotions")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getServiceStats(): ServiceStats {
        return try {
            val response = getClient().get("$baseUrl/admin/services/stats")
            if (response.status == HttpStatusCode.OK) response.body() else ServiceStats(0, 0)
        } catch (e: Exception) { ServiceStats(0, 0) }
    }

    suspend fun togglePromotionStatus(id: Int): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/promotions/$id/toggle")
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Estado cambiado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun deletePromotion(id: Int): RegisterResponse {
        return try {
            val response = getClient().delete("$baseUrl/admin/promotions/$id")
            if (response.status.isSuccess()) RegisterResponse(true, "Eliminada")
            else RegisterResponse(false, "Error al eliminar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun deleteService(id: Int): RegisterResponse {
        return try {
            val response = getClient().delete("$baseUrl/admin/services/$id")
            if (response.status.isSuccess()) RegisterResponse(true, "Servicio eliminado")
            else RegisterResponse(false, "Error al eliminar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun getProducts(): List<Product> {
        return try {
            val response = getClient().get("$baseUrl/admin/products")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getInventoryStats(): InventoryStats {
        return try {
            val response = getClient().get("$baseUrl/admin/inventory/stats")
            if (response.status == HttpStatusCode.OK) response.body() else InventoryStats(0, 0, 0.0)
        } catch (e: Exception) { InventoryStats(0, 0, 0.0) }
    }

    suspend fun quickReduceStock(id: Int, cantidad: Int = 1): QuickReduceResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/products/$id/reduce-stock") {
                setBody(mapOf("cantidad" to cantidad))
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) {
                Json { ignoreUnknownKeys = true }.decodeFromString<QuickReduceResponse>(responseText)
            } else {
                QuickReduceResponse(false, "Error: $responseText")
            }
        } catch (e: Exception) { QuickReduceResponse(false, "Error de red") }
    }

    suspend fun deleteProduct(id: Int): RegisterResponse {
        return try {
            val response = getClient().delete("$baseUrl/admin/products/$id")
            if (response.status.isSuccess()) RegisterResponse(true, "Producto eliminado")
            else RegisterResponse(false, "Error al eliminar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun getBarbers(): List<Barber> {
        return try {
            val response = getClient().get("$baseUrl/admin/barbers")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getBarberStats(): BarberStats {
        return try {
            val response = getClient().get("$baseUrl/admin/barbers/stats")
            if (response.status == HttpStatusCode.OK) response.body() else BarberStats(0, 0, 0)
        } catch (e: Exception) { BarberStats(0, 0, 0) }
    }

    suspend fun updateBarberStatus(id: Int, active: Boolean): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/barbers/$id/status") {
                parameter("active", active)
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Estado actualizado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun updateBarberSchedule(id: Int, config: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/barbers/$id/horario") {
                setBody(mapOf("config" to config))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Horario guardado")
            else RegisterResponse(false, "Error al guardar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun deleteBarber(id: Int): RegisterResponse {
        return try {
            val response = getClient().delete("$baseUrl/admin/barbers/$id")
            if (response.status.isSuccess()) RegisterResponse(true, "Barbero eliminado")
            else RegisterResponse(false, "Error al eliminar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }
    
    suspend fun getCustomers(): List<Cliente> {
        return try {
            val response = getClient().get("$baseUrl/admin/customers")
            val responseText = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                Json { ignoreUnknownKeys = true }.decodeFromString<List<Cliente>>(responseText)
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getCustomerStats(): ClienteStats {
        return try {
            val response = getClient().get("$baseUrl/admin/customers/stats")
            if (response.status == HttpStatusCode.OK) response.body() else ClienteStats(0, 0, 0)
        } catch (e: Exception) { ClienteStats(0, 0, 0) }
    }

    suspend fun toggleCustomerStatus(id: Int): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/customers/$id/toggle")
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Estado actualizado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun deleteCustomer(id: Int): RegisterResponse {
        return try {
            val response = getClient().delete("$baseUrl/admin/customers/$id")
            if (response.status.isSuccess()) RegisterResponse(true, "Eliminado")
            else RegisterResponse(false, "Error al eliminar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun createCustomer(customer: Cliente, password: String?): RegisterResponse {
        return try {
            val payload = mapOf(
                "nombre" to customer.nombre,
                "apellido" to customer.apellido,
                "telefono" to customer.telefono,
                "correo" to customer.correo,
                "password" to (password ?: ""),
                "fecha_cumpleanos" to (customer.fecha_cumpleanos ?: ""),
                "direccion" to (customer.direccion ?: ""),
                "notas" to (customer.notas ?: "")
            )
            val response = getClient().post("$baseUrl/admin/customers") {
                setBody(payload)
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Cliente creado")
            else RegisterResponse(false, response.bodyAsText())
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun updateCustomer(id: Int, customer: Cliente): RegisterResponse {
        return try {
            val payload = mapOf(
                "nombre" to customer.nombre,
                "apellido" to customer.apellido,
                "telefono" to customer.telefono,
                "correo" to customer.correo,
                "fecha_cumpleanos" to (customer.fecha_cumpleanos ?: ""),
                "direccion" to (customer.direccion ?: ""),
                "notas" to (customer.notas ?: ""),
                "estado" to customer.estado
            )
            val response = getClient().put("$baseUrl/admin/customers/$id") {
                setBody(payload)
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Cliente actualizado")
            else response.body<RegisterResponse>()
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun processGhostSale(request: GhostAppointmentRequest): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/ghost-sale") {
                setBody(request)
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) {
                try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<RegisterResponse>(responseText)
                } catch (e: Exception) {
                    RegisterResponse(true, "Venta realizada")
                }
            } else {
                RegisterResponse(false, "Error (${response.status.value}): $responseText")
            }
        } catch (e: Exception) {
            RegisterResponse(false, "Error de comunicación")
        }
    }

    suspend fun getReportStats(startDate: String, endDate: String, barberId: Int?, serviceId: Int?): ReportStats {
        return try {
            val response = getClient().get("$baseUrl/admin/reports/stats") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                if (barberId != null) parameter("barberId", barberId)
                if (serviceId != null) parameter("serviceId", serviceId)
            }
            if (response.status == HttpStatusCode.OK) response.body() else ReportStats(0, 0.0)
        } catch (e: Exception) { ReportStats(0, 0.0) }
    }

    suspend fun getFilteredAppointments(startDate: String, endDate: String, barberId: Int?, serviceId: Int?): List<Appointment> {
        return try {
            val response = getClient().get("$baseUrl/admin/reports/appointments") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                if (barberId != null) parameter("barberId", barberId)
                if (serviceId != null) parameter("serviceId", serviceId)
            }
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getSoldProducts(startDate: String, endDate: String): List<SoldProduct> {
        return try {
            val response = getClient().get("$baseUrl/admin/reports/sold-products") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getSaleDetails(appointmentId: Int): List<SaleDetail> {
        return try {
            val response = getClient().get("$baseUrl/admin/reports/sale-details/$appointmentId")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getServiceCategories(): List<ServiceCategory> {
        return try {
            val response = getClient().get("$baseUrl/admin/service-categories")
            val responseText = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                Json { ignoreUnknownKeys = true }.decodeFromString<List<ServiceCategory>>(responseText)
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getProductCategories(): List<ServiceCategory> {
        return try {
            val response = getClient().get("$baseUrl/admin/product-categories")
            val responseText = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                Json { ignoreUnknownKeys = true }.decodeFromString<List<ServiceCategory>>(responseText)
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun createServiceCategory(nombre: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/service-categories") {
                setBody(mapOf("nombre" to nombre))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Categoría creada")
            else RegisterResponse(false, "Error al crear")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun createProductCategory(nombre: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/product-categories") {
                setBody(mapOf("nombre" to nombre))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Categoría de producto creada")
            else RegisterResponse(false, "Error al crear")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun createService(service: Service, imageBytes: ByteArray?): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/services") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            // Datos del servicio como JSON string
                            append("service", Json.encodeToString(Service.serializer(), service))
                            
                            // Imagen opcional
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"service.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Servicio guardado")
            else {
                val errorMsg = response.bodyAsText()
                RegisterResponse(false, "Error: $errorMsg")
            }
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun createPromotion(promotion: Promotion, imageBytes: ByteArray?): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/promotions") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("promotion", Json.encodeToString(Promotion.serializer(), promotion))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"promotion.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Promoción creada")
            else {
                val errorMsg = response.bodyAsText()
                RegisterResponse(false, "Error: $errorMsg")
            }
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun createProduct(product: Product, imageBytes: ByteArray?): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/products") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("product", Json.encodeToString(Product.serializer(), product))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"product.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Producto guardado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun updateProduct(product: Product, imageBytes: ByteArray?): RegisterResponse {
        return try {
            val response = getClient().put("$baseUrl/admin/products/${product.id}") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("product", Json.encodeToString(Product.serializer(), product))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"product_update.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Producto actualizado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun updateService(service: Service, imageBytes: ByteArray?): RegisterResponse {
        return try {
            val response = getClient().put("$baseUrl/admin/services/${service.id}") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("service", Json.encodeToString(Service.serializer(), service))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"service_update.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Servicio actualizado")
            else {
                val errorMsg = response.bodyAsText()
                RegisterResponse(false, "Error: $errorMsg")
            }
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun updatePromotion(promotion: Promotion, imageBytes: ByteArray?): RegisterResponse {
        return try {
            val response = getClient().put("$baseUrl/admin/promotions/${promotion.id}") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("promotion", Json.encodeToString(Promotion.serializer(), promotion))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"promotion_update.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Promoción actualizada")
            else {
                val errorMsg = response.bodyAsText()
                RegisterResponse(false, "Error: $errorMsg")
            }
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun updateBarber(barber: Barber, imageBytes: ByteArray?, password: String? = null): RegisterResponse {
        return try {
            val response = getClient().put("$baseUrl/admin/barbers/${barber.id}") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            val request = BarberRequest(
                                id = barber.id,
                                nombreCompleto = barber.nombreCompleto,
                                telefono = barber.telefono,
                                email = barber.email,
                                activo = barber.activo,
                                bio = barber.bio,
                                specialties = barber.specialties,
                                password = password
                            )
                            append("barber", Json.encodeToString(BarberRequest.serializer(), request))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"barber_update.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Barbero actualizado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun createBarber(barber: Barber, imageBytes: ByteArray?, password: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/barbers") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            val request = BarberRequest(
                                nombreCompleto = barber.nombreCompleto,
                                telefono = barber.telefono,
                                email = barber.email,
                                activo = barber.activo,
                                bio = barber.bio,
                                specialties = barber.specialties,
                                password = password
                            )
                            append("barber", Json.encodeToString(BarberRequest.serializer(), request))
                            if (imageBytes != null) {
                                append("image", imageBytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"barber.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) RegisterResponse(true, "Barbero creado")
            else RegisterResponse(false, responseText)
        } catch (e: Exception) { RegisterResponse(false, "Error de red: ${e.message}") }
    }

    suspend fun uploadProfilePhoto(imageBytes: ByteArray): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/customer/profile/photo") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("image", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"profile.jpg\"")
                            })
                        }
                    )
                )
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Foto actualizada")
            else RegisterResponse(false, "Error al subir")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun biometricLogin(token: String): LoginResponse {
        return try {
            val response = getClient().post("$baseUrl/login/biometric") {
                setBody(mapOf("token" to token))
            }
            if (response.status.isSuccess()) {
                val res = response.body<LoginResponse>()
                ApiClient.sessionToken = res.token
                res
            } else LoginResponse(false, "Error biométrico")
        } catch (e: Exception) { LoginResponse(false, "Error de red") }
    }

    suspend fun updateAppointmentStatus(id: Int, status: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/appointments/$id/status") {
                setBody(mapOf("status" to status))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Estado actualizado")
            else RegisterResponse(false, "Error al actualizar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun createBooking(request: GhostAppointmentRequest): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/client/booking") {
                setBody(request)
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Cita agendada")
            else RegisterResponse(false, "Error al agendar")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }

    suspend fun registerBiometric(token: String): RegisterResponse {
        return try {
            val response = getClient().post("$baseUrl/admin/biometric/register") {
                setBody(mapOf("token" to token))
            }
            if (response.status.isSuccess()) RegisterResponse(true, "Huella vinculada")
            else RegisterResponse(false, "Error al vincular")
        } catch (e: Exception) { RegisterResponse(false, "Error de red") }
    }
}
