package com.example.myapplication_prueba.cuenta

import com.example.myapplication_prueba.Greeting
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginView(onRegisterClick: () -> Unit, onLoginSuccess: (String) -> Unit, successMessage: String? = null) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf(successMessage) }
    var toastType by remember { mutableStateOf(if (successMessage != null) ToastType.SUCCESS else ToastType.ERROR) }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Wolf-Look Colors
    val primaryColor = Color(0xFFDC2626) // Red
    val backgroundColor = Color(0xFFF8F6F6)
    val darkBackgroundColor = Color(0xFF121212)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo / Icon (Using Brush as a generic alternative to cut if not available)
            Icon(
                imageVector = Icons.Default.Face, // Fallback icon
                contentDescription = "Logo",
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "Wolf-Look",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = darkBackgroundColor
            )
            
            Text(
                text = "Iniciar Sesión",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = darkBackgroundColor
            )
            
            Text(
                text = "Bienvenido de nuevo. Por favor, ingresa tus datos.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                placeholder = { Text("nombre@ejemplo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    placeholder = { Text("••••••••") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                        .clickable { /* Handle forgot password */ }
                )
            }

            // Error Message
            toastMessage?.let {
                WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
            }

            // Login Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        toastMessage = null
                        coroutineScope.launch {
                            val response = Greeting().login(email, password)
                            isLoading = false
                            if (response.success) {
                                toastType = ToastType.SUCCESS
                                toastMessage = response.message
                                onLoginSuccess(response.rol ?: "CLIENTE")
                            } else {
                                toastType = ToastType.ERROR
                                toastMessage = response.message
                            }
                        }
                    } else {
                        toastType = ToastType.WARNING
                        toastMessage = "Por favor, completa todos los campos"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }

            // Register Link
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes una cuenta? ", color = Color.Gray)
                Text(
                    text = "Regístrate",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(color = Color.LightGray)
            
            Text(
                text = "© 2026 Barbería Wolf. Todos los derechos reservados.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
