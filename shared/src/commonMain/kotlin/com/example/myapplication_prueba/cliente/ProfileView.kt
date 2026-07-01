package com.example.myapplication_prueba.cliente

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
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch

@Composable
fun ProfileView(onBack: () -> Unit) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("cliente@wolf.com") } // Disabled
    var fechaNacimiento by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    var isLoading by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFD32F2F)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Gray)
                }
                Text("DATOS DE LA CUENTA", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileTextField("NOMBRES", nombres, { nombres = it }, Modifier.weight(1f))
                        ProfileTextField("APELLIDOS", apellidos, { apellidos = it }, Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileTextField("TELÉFONO", telefono, { if(it.length <= 10) telefono = it }, Modifier.weight(1f), KeyboardType.Phone)
                        ProfileTextField("EMAIL", email, {}, Modifier.weight(1f), enabled = false)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileTextField("FECHA NACIMIENTO", fechaNacimiento, { fechaNacimiento = it }, Modifier.weight(1f), placeholder = "AAAA-MM-DD")
                        ProfileTextField("DIRECCIÓN", direccion, { direccion = it }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Seguridad Section
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = primaryColor)
                        Text("SEGURIDAD Y CREDENCIALES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Nueva Contraseña (Dejar en blanco para conservar actual)", fontSize = 10.sp, color = Color.Gray)
                        
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
                                if (password == confirmPassword) {
                                    isLoading = true
                                    // Simular update
                                    toastType = ToastType.SUCCESS
                                    toastMessage = "Perfil actualizado con éxito"
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isLoading && (password == confirmPassword)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("GUARDAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        toastMessage?.let {
            WolfToast(it, toastType, { toastMessage = null })
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    placeholder: String = ""
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = { if(placeholder.isNotEmpty()) Text(placeholder, fontSize = 12.sp) },
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF1F5F9),
                disabledBorderColor = Color(0xFFE2E8F0)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}
