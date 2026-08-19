package com.example.myapplication_prueba.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.AdminProfileUpdateRequest
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.PerfilAdmin
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch

@Composable
fun AdminEditarPerfilView(perfil: PerfilAdmin?, onBack: () -> Unit) {
    // Usamos el perfil pasado como parámetro para inicializar los estados
    var nombres by remember(perfil) { mutableStateOf(perfil?.nombres ?: "") }
    var apellidos by remember(perfil) { mutableStateOf(perfil?.apellidos ?: "") }
    var telefono by remember(perfil) { mutableStateOf(perfil?.telefono ?: "") }
    var email by remember(perfil) { mutableStateOf(perfil?.email ?: "") }
    
    // Si el perfil cambia externamente, actualizamos los campos (por seguridad)
    LaunchedEffect(perfil) {
        perfil?.let {
            nombres = it.nombres
            apellidos = it.apellidos
            telefono = it.telefono ?: ""
            email = it.email
        }
    }
    
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    var isLoading by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFDC2626)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Gray)
                }
                Text("DATOS DEL ADMINISTRADOR", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EditField("NOMBRES", nombres, { nombres = it }, Modifier.weight(1f))
                        EditField("APELLIDOS", apellidos, { apellidos = it }, Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EditField("TELÉFONO", telefono, { if(it.length <= 10) telefono = it }, Modifier.weight(1f), KeyboardType.Phone)
                        EditField("EMAIL", email, { email = it }, Modifier.weight(1f), KeyboardType.Email)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Seguridad Section
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = primaryColor)
                        Text("SEGURIDAD", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Nueva Contraseña (Dejar en blanco para no cambiar)", fontSize = 10.sp, color = Color.Gray)
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Confirmar Nueva Contraseña", fontSize = 10.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("••••••••") },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                            Text("Las contraseñas no coinciden", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    toastMessage = "El correo electrónico no puede estar vacío"
                                    toastType = ToastType.ERROR
                                    return@Button
                                }
                                
                                if (password == confirmPassword) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = Greeting().updateAdminProfile(
                                            AdminProfileUpdateRequest(
                                                nombres = nombres,
                                                apellidos = apellidos,
                                                email = email,
                                                telefono = telefono,
                                                password = if (password.isNotBlank()) password else null
                                            )
                                        )
                                        
                                        if (result.success) {
                                            toastType = ToastType.SUCCESS
                                            toastMessage = "Perfil actualizado con éxito"
                                            password = ""
                                            confirmPassword = ""
                                            // Opcional: Volver atrás automáticamente tras éxito después de un delay
                                            kotlinx.coroutines.delay(1500)
                                            onBack()
                                        } else {
                                            toastType = ToastType.ERROR
                                            toastMessage = result.message
                                        }
                                        isLoading = false
                                    }
                                } else {
                                    toastMessage = "Las contraseñas no coinciden"
                                    toastType = ToastType.ERROR
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, kt: KeyboardType = KeyboardType.Text) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = kt),
            singleLine = true
        )
    }
}
