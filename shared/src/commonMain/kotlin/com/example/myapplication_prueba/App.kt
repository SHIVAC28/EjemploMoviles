package com.example.myapplication_prueba

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication_prueba.admin.AdminNavigationWrapper
import com.example.myapplication_prueba.admin.FormularioCliente
import com.example.myapplication_prueba.cliente.ClienteDashboard
import com.example.myapplication_prueba.cliente.ClienteNavigationWrapper
import com.example.myapplication_prueba.cliente.PerfilView
import com.example.myapplication_prueba.cuenta.LoginView
import com.example.myapplication_prueba.cuenta.RegistroView
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    // Check for saved session IMMEDIATELY
    val savedState = remember { SettingsManager.getString(SettingsManager.SCREEN_STATE) ?: "LOGIN" }
    val savedRole = remember { SettingsManager.getString(SettingsManager.USER_ROLE) ?: "CLIENTE" }
    
    var screenState by remember { mutableStateOf(savedState) } 
    var userRole by remember { mutableStateOf(savedRole) }
    
    // Restore ApiClient token if exists
    LaunchedEffect(Unit) {
        val savedToken = SettingsManager.getString(SettingsManager.SESSION_TOKEN)
        if (!savedToken.isNullOrBlank()) {
            ApiClient.sessionToken = savedToken
        }
    }

    var registrationSuccessMessage by remember { mutableStateOf<String?>(null) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "SECURITY"

    MaterialTheme {
        if (currentSubScreen == "SECURITY") {
            SeguridadView(email = if(userRole == "ADMIN") "admin@wolf.com" else "cliente@wolf.com") { 
                currentSubScreen = null 
            }
        } else {
            when (screenState) {
                "LOGIN" -> {
                    LoginView(
                        onRegisterClick = { screenState = "REGISTER" },
                        onLoginSuccess = { role ->
                            userRole = role
                            screenState = "MAIN"
                            // Save session
                            SettingsManager.saveString(SettingsManager.SCREEN_STATE, "MAIN")
                            SettingsManager.saveString(SettingsManager.USER_ROLE, role)
                            SettingsManager.saveString(SettingsManager.SESSION_TOKEN, ApiClient.sessionToken ?: "")
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
                    RegistroView(
                        onNavigateToLogin = { screenState = "LOGIN" },
                        onRegisterSuccess = { 
                            registrationSuccessMessage = "¡Cuenta creada con éxito! Ya puedes iniciar sesión."
                            screenState = "LOGIN" 
                        }
                    )
                }
                "MAIN" -> {
                    val onLogoutAction = {
                        SettingsManager.saveString(SettingsManager.SCREEN_STATE, "LOGIN")
                        SettingsManager.saveString(SettingsManager.SESSION_TOKEN, "")
                        ApiClient.sessionToken = null
                        screenState = "LOGIN"
                    }
                    
                    when(userRole) {
                        "ADMIN" -> AdminNavigationWrapper(role = userRole, onLogout = onLogoutAction) {
                            MainContent(role = userRole, onLogout = onLogoutAction, onNavigateToSecurity = { currentSubScreen = "SECURITY" })
                        }
                        "BARBERO" -> com.example.myapplication_prueba.barbero.BarberoNavigationWrapper(role = userRole, onLogout = onLogoutAction) {
                            MainContent(role = userRole, onLogout = onLogoutAction, onNavigateToSecurity = { currentSubScreen = "SECURITY" })
                        }
                        else -> ClienteNavigationWrapper(
                            role = userRole,
                            onLogout = onLogoutAction
                        ) {
                            MainContent(role = userRole, onLogout = onLogoutAction, onNavigateToSecurity = { currentSubScreen = "SECURITY" })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(role: String, onLogout: () -> Unit, onNavigateToSecurity: () -> Unit) {
    var pestanaSeleccionada by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestanaSeleccionada) {
            when (role) {
                "ADMIN" -> {
                    Tab(selected = pestanaSeleccionada == 0, onClick = { pestanaSeleccionada = 0 }, text = { Text("Usuarios") })
                    Tab(selected = pestanaSeleccionada == 1, onClick = { pestanaSeleccionada = 1 }, text = { Text("Clientes") })
                }
                "BARBERO" -> {
                    Tab(selected = pestanaSeleccionada == 0, onClick = { pestanaSeleccionada = 0 }, text = { Text("Clientes") })
                    Tab(selected = pestanaSeleccionada == 1, onClick = { pestanaSeleccionada = 1 }, text = { Text("Mi Agenda") })
                }
                else -> { // CLIENTE o cualquier otro
                    Tab(selected = pestanaSeleccionada == 0, onClick = { pestanaSeleccionada = 0 }, text = { Text("Mis Citas") })
                    Tab(selected = pestanaSeleccionada == 1, onClick = { pestanaSeleccionada = 1 }, text = { Text("Perfil") })
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (role) {
                "ADMIN" -> when (pestanaSeleccionada) {
                    0 -> ModuloUsuariosCompanero()
                    1 -> FormularioCliente()
                }
                "BARBERO" -> when (pestanaSeleccionada) {
                    0 -> FormularioCliente()
                    1 -> Text("Agenda del Barbero - Próximamente")
                }
                else -> when (pestanaSeleccionada) {
                    0 -> ClienteDashboard()
                    1 -> PerfilView(onBack = { pestanaSeleccionada = 0 }, onNavigateToSecurity = onNavigateToSecurity)
                }
            }
        }
    }
}

@Composable
fun FormularioCliente() {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var cumpleanos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrar Cliente", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") })
        OutlinedTextField(value = cumpleanos, onValueChange = { cumpleanos = it }, label = { Text("Cumpleaños (AAAA-MM-DD)") })
        OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
        OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                mensaje = Greeting().enviarCliente(nombre, apellido, cumpleanos, telefono, correo)
            }
        }) {
            Text("Enviar")
        }
        Text(mensaje)
    }
}

@Composable
fun ModuloUsuariosCompanero() {
    var usuarios by remember { mutableStateOf(listOf<UsuarioPrueba>()) }
    val scope = rememberCoroutineScope()
    val greeting = remember { Greeting() }

    LaunchedEffect(Unit) {
        // Podríamos exponer una función en Greeting para esto o usar ApiClient directamente
        // Para simplificar, usaremos Greeting().greet() que ya hace un GET /usuarios
        // Pero idealmente Greeting debería tener un método getUsuarios()
    }

    Column {
        Text("Gestión de Usuarios", style = MaterialTheme.typography.headlineMedium)
        usuarios.forEach { user ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${user.nombre} - ${user.rol}")
                IconButton(onClick = {
                    scope.launch {
                        // Lógica para eliminar si fuera necesario
                    }
                }) {
                    // Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
