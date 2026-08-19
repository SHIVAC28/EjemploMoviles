package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
fun AdminEditarClienteView(customer: Cliente, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Form States
    var nombres by remember { mutableStateOf(customer.nombre) }
    var apellidos by remember { mutableStateOf(customer.apellido) }
    var email by remember { mutableStateOf(customer.correo) }
    var telefono by remember { mutableStateOf(customer.telefono) }
    var birthDate by remember { mutableStateOf(customer.fecha_cumpleanos ?: "") }
    var address by remember { mutableStateOf(customer.direccion ?: "") }
    var status by remember { mutableStateOf(customer.estado) }
    var notes by remember { mutableStateOf(customer.notas ?: "") }
    
    var isLoading by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

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
                IconButton(onClick = onBack, modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Editar Cliente", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Gestión de base de datos", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Sections
            ExpressCard(title = "1. Datos Personales", subtitle = "Información básica de contacto.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FormTextField(label = "Nombres", value = nombres, onValueChange = { nombres = it }, placeholder = "Ej. Juan Carlos")
                    FormTextField(label = "Apellidos", value = apellidos, onValueChange = { apellidos = it }, placeholder = "Ej. Pérez")
                    FormTextField(label = "Correo", value = email, onValueChange = { email = it }, placeholder = "correo@ejemplo.com", keyboardType = KeyboardType.Email)
                    FormTextField(label = "Teléfono", value = telefono, onValueChange = { telefono = it }, placeholder = "10 dígitos", keyboardType = KeyboardType.Phone)
                }
            }

            ExpressCard(title = "2. Información Adicional", subtitle = "Ubicación y fechas.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FormTextField(label = "Nacimiento", value = birthDate, onValueChange = { birthDate = it }, placeholder = "AAAA-MM-DD")
                    FormTextField(label = "Dirección", value = address, onValueChange = { address = it }, placeholder = "Calle, Número, Ciudad", singleLine = false, minLines = 2)
                }
            }

            ExpressCard(title = "3. Configuración", subtitle = "Notas y estado de cuenta.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text("ESTADO INICIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("active", "inactive").forEach { s ->
                                val isSelected = status == s
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { status = s },
                                    color = if(isSelected) Color(0xFFDC2626).copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if(isSelected) BorderStroke(1.dp, Color(0xFFDC2626)) else null
                                ) {
                                    Text(
                                        if(s == "active") "ACTIVO" else "INACTIVO", 
                                        modifier = Modifier.padding(vertical = 12.dp), 
                                        textAlign = TextAlign.Center, 
                                        fontWeight = FontWeight.Black, 
                                        color = if(isSelected) Color(0xFFDC2626) else Color.Gray, 
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    FormTextField(label = "Notas", value = notes, onValueChange = { notes = it }, placeholder = "Preferencias, alergias...", singleLine = false, minLines = 4)
                }
            }

            // Submit
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val res = Greeting().updateCustomer(
                            customer.id!!,
                            customer.copy(
                                nombre = nombres,
                                apellido = apellidos,
                                correo = email,
                                telefono = telefono,
                                fecha_cumpleanos = birthDate,
                                direccion = address,
                                estado = status,
                                notas = notes
                            )
                        )
                        isLoading = false
                        if (res.success) {
                            toastMessage = "Cliente actualizado"
                            toastType = ToastType.SUCCESS
                            kotlinx.coroutines.delay(1000)
                            onBack()
                        } else {
                            toastMessage = res.message; toastType = ToastType.ERROR
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && nombres.isNotBlank() && email.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}
