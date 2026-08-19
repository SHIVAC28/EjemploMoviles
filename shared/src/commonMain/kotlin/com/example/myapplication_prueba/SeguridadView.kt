package com.example.myapplication_prueba

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
import com.example.myapplication_prueba.admin.ExpressCard
import com.example.myapplication_prueba.admin.FormTextField
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import com.example.myapplication_prueba.sensor.BiometricAuth
import com.example.myapplication_prueba.sensor.BiometricResult
import kotlinx.coroutines.launch

@Composable
fun SeguridadView(email: String, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFDC2626)
    
    // States
    var isVerified by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    var isLoading by remember { mutableStateOf(false) }

    val biometricAuth = remember { BiometricAuth() }
    val savedToken = remember { SettingsManager.getString(SettingsManager.BIOMETRIC_TOKEN) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text("Seguridad y Acceso", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }

            if (!isVerified) {
                // Barrera de Verificación
                ExpressCard(title = "Verificación Requerida", subtitle = "Confirme su identidad para gestionar su seguridad.") {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        
                        if (biometricAuth.isBiometricAvailable() && !savedToken.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val result = biometricAuth.authenticate(
                                            "Verificación de Seguridad",
                                            "Confirma tu identidad",
                                            "Usa tu huella para acceder"
                                        )
                                        if (result is BiometricResult.Success) {
                                            isVerified = true
                                            toastMessage = "Acceso concedido"
                                            toastType = ToastType.SUCCESS
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Fingerprint, null)
                                Spacer(Modifier.width(8.dp))
                                Text("DESBLOQUEAR CON HUELLA", fontWeight = FontWeight.Black)
                            }
                            
                            Text("— O —", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        FormTextField(
                            label = "Contraseña Actual",
                            value = currentPasswordInput,
                            onValueChange = { currentPasswordInput = it },
                            placeholder = "••••••••",
                            keyboardType = KeyboardType.Password,
                            isBlack = true
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    val res = Greeting().verifyPassword(currentPasswordInput)
                                    isLoading = false
                                    if (res.success) {
                                        isVerified = true
                                        toastMessage = "Acceso concedido"
                                        toastType = ToastType.SUCCESS
                                    } else {
                                        toastMessage = res.message
                                        toastType = ToastType.ERROR
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading && currentPasswordInput.isNotBlank()
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("VERIFICAR CONTRASEÑA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Configuración de Seguridad (Desbloqueada)
                
                // 1. Cambio de Contraseña
                ExpressCard(title = "Cambiar Contraseña", subtitle = "Establezca una nueva clave de acceso.") {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormTextField(label = "Nueva Contraseña", value = newPassword, onValueChange = { newPassword = it }, placeholder = "••••••••", keyboardType = KeyboardType.Password, isBlack = true)
                        FormTextField(label = "Confirmar Contraseña", value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "••••••••", keyboardType = KeyboardType.Password, isBlack = true)

                        Button(
                            onClick = {
                                if (newPassword == confirmPassword && newPassword.isNotBlank()) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val res = Greeting().changePassword(newPassword)
                                        isLoading = false
                                        if (res.success) {
                                            toastMessage = "Contraseña actualizada"
                                            toastType = ToastType.SUCCESS
                                            newPassword = ""
                                            confirmPassword = ""
                                        } else {
                                            toastMessage = res.message; toastType = ToastType.ERROR
                                        }
                                    }
                                } else {
                                    toastMessage = "Las contraseñas no coinciden"; toastType = ToastType.ERROR
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading && newPassword.isNotBlank()
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("ACTUALIZAR CONTRASEÑA", fontWeight = FontWeight.Black)
                        }
                    }
                }

                // 2. Configuración de Huella
                ExpressCard(title = "Acceso Biométrico", subtitle = "Gestione el inicio de sesión con huella.") {
                    var isBiometricEnabled by remember { 
                        val token = SettingsManager.getString(SettingsManager.BIOMETRIC_TOKEN)
                        mutableStateOf(!token.isNullOrBlank()) 
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Habilitar Huella", fontWeight = FontWeight.Bold)
                            Text(if(isBiometricEnabled) "Activo" else "Inactivo", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    coroutineScope.launch {
                                        val result = biometricAuth.authenticate("Activar Huella", "Confirma tu identidad", "Usa tu huella para esta app")
                                        if (result is BiometricResult.Success) {
                                            val token = "token_${email.replace("@", "_")}"
                                            val res = Greeting().registerBiometric(token)
                                            if (res.success) {
                                                SettingsManager.saveString(SettingsManager.BIOMETRIC_TOKEN, token)
                                                isBiometricEnabled = true
                                                toastMessage = "Huella activada con éxito"
                                                toastType = ToastType.SUCCESS
                                            } else {
                                                toastMessage = res.message; toastType = ToastType.ERROR
                                            }
                                        }
                                    }
                                } else {
                                    SettingsManager.saveString(SettingsManager.BIOMETRIC_TOKEN, "")
                                    isBiometricEnabled = false
                                    toastMessage = "Huella desactivada"; toastType = ToastType.WARNING
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                        )
                    }
                }
            }
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}
