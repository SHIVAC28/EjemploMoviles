package com.example.myapplication_prueba

import com.example.myapplication_prueba.cuenta.LoginView
import com.example.myapplication_prueba.cuenta.RegisterView
import com.example.myapplication_prueba.admin.AdminNavigationWrapper
import com.example.myapplication_prueba.admin.FormularioCliente
import com.example.myapplication_prueba.cliente.CustomerNavigationWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(val id: Int = 0, val nombre: String, val rol: String)

class KtorClient {
    private val client = HttpClient {
        install(ContentNegotiation) { json() }
    }
    private val urlBackend = "https://proyecto-backend-ktor-production.up.railway.app/usuarios"

    suspend fun getUsuarios(): List<Usuario> {
        return try { client.get(urlBackend).body() } catch (e: Exception) { emptyList() }
    }
    suspend fun crearUsuario(nombre: String, rol: String) {
        try {
            client.post(urlBackend) {
                contentType(ContentType.Application.Json)
                setBody(Usuario(nombre = nombre, rol = rol))
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
    suspend fun eliminarUsuario(id: Int) {
        try { client.delete("$urlBackend/$id") } catch (e: Exception) { e.printStackTrace() }
    }
}

@Composable
@Preview
fun App() {
    var screenState by remember { mutableStateOf("LOGIN") } // "LOGIN", "REGISTER", "MAIN"
    var userRole by remember { mutableStateOf("CLIENTE") }
    var registrationSuccessMessage by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        when (screenState) {
            "LOGIN" -> {
                LoginView(
                    onRegisterClick = { screenState = "REGISTER" },
                    onLoginSuccess = { role ->
                        userRole = role
                        screenState = "MAIN"
                    },
                    successMessage = registrationSuccessMessage
                )
                // Limpiamos el mensaje después de mostrarlo para que no se repita al volver a la pantalla
                LaunchedEffect(registrationSuccessMessage) {
                    if (registrationSuccessMessage != null) {
                        kotlinx.coroutines.delay(6000)
                        registrationSuccessMessage = null
                    }
                }
            }
            "REGISTER" -> {
                RegisterView(
                    onNavigateToLogin = { screenState = "LOGIN" },
                    onRegisterSuccess = { 
                        registrationSuccessMessage = "¡Cuenta creada con éxito! Ya puedes iniciar sesión."
                        screenState = "LOGIN" 
                    }
                )
            }
            "MAIN" -> {
                if (userRole == "ADMIN") {
                    AdminNavigationWrapper(role = userRole) {
                        MainContent(userRole)
                    }
                } else if (userRole == "CLIENTE") {
                    CustomerNavigationWrapper(role = userRole) {
                        MainContent(userRole)
                    }
                } else {
                    MainContent(userRole)
                }
            }
        }
    }
}

@Composable
fun MainContent(role: String) {
    var pestañaSeleccionada by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestañaSeleccionada) {
            when (role) {
                "ADMIN" -> {
                    Tab(selected = pestañaSeleccionada == 0, onClick = { pestañaSeleccionada = 0 }, text = { Text("Usuarios") })
                    Tab(selected = pestañaSeleccionada == 1, onClick = { pestañaSeleccionada = 1 }, text = { Text("Clientes") })
                }
                "BARBERO" -> {
                    Tab(selected = pestañaSeleccionada == 0, onClick = { pestañaSeleccionada = 0 }, text = { Text("Clientes") })
                    Tab(selected = pestañaSeleccionada == 1, onClick = { pestañaSeleccionada = 1 }, text = { Text("Mi Agenda") })
                }
                else -> { // CLIENTE o cualquier otro
                    Tab(selected = pestañaSeleccionada == 0, onClick = { pestañaSeleccionada = 0 }, text = { Text("Mis Citas") })
                    Tab(selected = pestañaSeleccionada == 1, onClick = { pestañaSeleccionada = 1 }, text = { Text("Perfil") })
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (role) {
                "ADMIN" -> when (pestañaSeleccionada) {
                    0 -> ModuloUsuariosCompañero()
                    1 -> FormularioCliente()
                }
                "BARBERO" -> when (pestañaSeleccionada) {
                    0 -> FormularioCliente()
                    1 -> Text("Agenda del Barbero - Próximamente")
                }
                else -> when (pestañaSeleccionada) {
                    0 -> Text("Bienvenido! Aquí verás tus citas.")
                    1 -> Text("Perfil del Cliente - Próximamente")
                }
            }
        }
    }
}

@Composable
fun FormularioClienteMio() {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var cumpleanos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Registro de Clientes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = cumpleanos, onValueChange = { cumpleanos = it }, label = { Text("Fecha de Cumpleaños") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val resultado = Greeting().enviarCliente(nombre, apellido, cumpleanos, telefono, correo)
                    println("Respuesta de Railway: $resultado")
                    if(resultado.contains("éxito")){
                        nombre = ""; apellido = ""; cumpleanos = ""; telefono = ""; correo = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Guardar Cliente", fontSize = 16.sp)
        }
    }
}

@Composable
fun ModuloUsuariosCompañero() {
    val client = remember { KtorClient() }
    val scope = rememberCoroutineScope()

    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try { usuarios = client.getUsuarios() } catch (e: Exception) { e.printStackTrace() } finally { loading = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GESTIÓN DE USUARIOS", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(16.dp))

        TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TextField(value = rol, onValueChange = { rol = it }, label = { Text("Rol") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    client.crearUsuario(nombre, rol)
                    usuarios = client.getUsuarios()
                    nombre = ""
                    val resultado = rol
                    rol = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear usuario")
        }

        Spacer(Modifier.height(20.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            usuarios.forEach { usuario ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${usuario.nombre} - ${usuario.rol}", modifier = Modifier.weight(1f))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            scope.launch {
                                client.eliminarUsuario(usuario.id)
                                usuarios = client.getUsuarios()
                            }
                        }
                    ) {
                        Text("Borrar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}