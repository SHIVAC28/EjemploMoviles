package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch

@Composable
fun AdminNuevoClienteView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Form States
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    val isFormValid = nombres.isNotBlank() && apellidos.isNotBlank() && 
                      email.isNotBlank() && telefono.isNotBlank() && 
                      password.isNotBlank()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Nuevo Cliente", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Gestión de Base de Datos / Alta de Perfil", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Main Content Grid (Single column for mobile, but logically structured)
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                
                // Section 1: Datos Personales
                CustomerSection(
                    title = "1. Datos Personales",
                    icon = Icons.Default.AccountCircle,
                    accentColor = Color(0xFFDC2626)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormTextField(label = "Nombres *", value = nombres, onValueChange = { nombres = it }, placeholder = "Ej. Juan Carlos", isBlack = true)
                        FormTextField(label = "Apellidos *", value = apellidos, onValueChange = { apellidos = it }, placeholder = "Ej. Pérez Gómez", isBlack = true)
                        FormTextField(label = "Correo Electrónico *", value = email, onValueChange = { email = it }, placeholder = "juan@ejemplo.com", keyboardType = KeyboardType.Email, isBlack = true)
                        FormTextField(label = "Teléfono Celular *", value = telefono, onValueChange = { 
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) telefono = it 
                        }, placeholder = "0000000000", keyboardType = KeyboardType.Phone, isBlack = true)
                    }
                }

                // Section 2: Información Adicional
                CustomerSection(
                    title = "2. Información Adicional",
                    icon = Icons.Default.LocationOn,
                    accentColor = Color.Gray
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormTextField(label = "Fecha de Nacimiento (AAAA-MM-DD)", value = fechaNacimiento, onValueChange = { fechaNacimiento = it }, placeholder = "1990-01-01", isBlack = true)
                        FormTextField(label = "Dirección Completa", value = direccion, onValueChange = { direccion = it }, placeholder = "Calle, Número, Ciudad, CP", singleLine = false, minLines = 2, isBlack = true)
                    }
                }

                // Section 3: Configuración de Cuenta
                CustomerSection(
                    title = "3. Configuración de Cuenta",
                    icon = Icons.Default.SettingsApplications,
                    accentColor = Color.Gray
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormTextField(label = "Contraseña Temporal *", value = password, onValueChange = { password = it }, placeholder = "Contraseña de acceso", keyboardType = KeyboardType.Password, isBlack = true)
                        FormTextField(label = "Notas y Preferencias", value = notas, onValueChange = { notas = it }, placeholder = "Alergias, productos preferidos...", singleLine = false, minLines = 4, isBlack = true)
                    }
                }

                // Sidebar Logic (rendered as a card at the end for mobile)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("ESTADO DEL REGISTRO", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F172A), letterSpacing = 1.sp)
                        Spacer(Modifier.height(24.dp))
                        
                        StatusStep(title = "Formulario Iniciado", subtitle = "Sesión de administrador activa", isDone = true)
                        Spacer(Modifier.height(16.dp))
                        StatusStep(
                            title = if (isFormValid) "Datos Completos" else "Campos Obligatorios",
                            subtitle = if (isFormValid) "Todos los campos han sido llenados" else "Datos requeridos incompletos",
                            isDone = isFormValid,
                            isWarning = !isFormValid
                        )

                        if (isFormValid) {
                            Spacer(Modifier.height(24.dp))
                            Surface(
                                color = Color(0xFFDC2626).copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.TaskAlt, null, tint = Color(0xFFDC2626))
                                    Text("LISTO PARA GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626), letterSpacing = 1.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    val res = Greeting().createCustomer(
                                        Cliente(
                                            nombre = nombres,
                                            apellido = apellidos,
                                            correo = email,
                                            telefono = telefono,
                                            fecha_cumpleanos = fechaNacimiento,
                                            direccion = direccion,
                                            notas = notas
                                        ),
                                        password
                                    )
                                    isLoading = false
                                    if (res.success) {
                                        toastMessage = "Cliente registrado con éxito"
                                        toastType = ToastType.SUCCESS
                                        kotlinx.coroutines.delay(1500)
                                        onBack()
                                    } else {
                                        toastMessage = res.message
                                        toastType = ToastType.ERROR
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFormValid) Color(0xFFDC2626) else Color(0xFFE2E8F0),
                                contentColor = if (isFormValid) Color.White else Color.Gray
                            ),
                            enabled = isFormValid && !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("GUARDAR CLIENTE", fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                        }

                        Text(
                            "Al registrar al cliente, se le enviará un correo de bienvenida automático con los términos de sesión.",
                            modifier = Modifier.padding(top = 24.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun CustomerSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Box {
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(accentColor).align(Alignment.CenterStart))
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
                    Text(title.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A), letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(32.dp))
                content()
            }
        }
    }
}

@Composable
fun StatusStep(title: String, subtitle: String, isDone: Boolean, isWarning: Boolean = false) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isDone) Color(0xFFDC2626).copy(alpha = 0.1f) else if (isWarning) Color(0xFFDC2626).copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isDone) Icons.Default.Check else if (isWarning) Icons.Default.PriorityHigh else Icons.Default.Circle,
                null,
                tint = if (isDone) Color(0xFF10B981) else if (isWarning) Color(0xFFDC2626) else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
