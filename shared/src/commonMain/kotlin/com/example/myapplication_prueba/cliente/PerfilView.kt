package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.PerfilCliente
import com.example.myapplication_prueba.SettingsManager
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import com.example.myapplication_prueba.sensor.CameraManager
import com.example.myapplication_prueba.sensor.bytesToImageBitmap
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun PerfilView(onBack: () -> Unit, onNavigateToSecurity: () -> Unit) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    
    // Foto temporal para previsualización
    var capturedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetchingData by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFD32F2F)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    if (showDatePicker) {
        com.example.myapplication_prueba.cliente.DatePickerModal(
            onDateSelected = { millis ->
                if (millis != null) {
                    fechaNacimiento = com.example.myapplication_prueba.cliente.formatMillisToDate(millis)
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val perfil = com.example.myapplication_prueba.Greeting().getProfile()
                if (perfil != null) {
                    nombres = perfil.nombres ?: ""
                    apellidos = perfil.apellidos ?: ""
                    email = perfil.email ?: ""
                    telefono = perfil.telefono ?: ""
                    fechaNacimiento = perfil.fechaNacimiento ?: ""
                    direccion = perfil.direccion ?: ""
                    imageUrl = perfil.imagenUrl
                } else {
                    toastType = ToastType.ERROR
                    toastMessage = "No se pudieron cargar los datos."
                }
            } catch (e: Exception) {
                toastType = ToastType.ERROR
                toastMessage = "Error de conexión: ${e.message}"
            } finally {
                isFetchingData = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        if (isFetchingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
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
                        
                        // Foto de Perfil / Cámara
                        val cameraManager = remember { CameraManager() }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .clickable {
                                        coroutineScope.launch {
                                            try {
                                                val image = cameraManager.takePhoto()
                                                if (image != null) {
                                                    capturedImageBytes = image
                                                    isLoading = true
                                                    val res = Greeting().uploadProfilePhoto(image)
                                                    isLoading = false
                                                    toastMessage = res.message
                                                    toastType = if(res.success) ToastType.SUCCESS else ToastType.ERROR
                                                }
                                            } catch (e: Exception) {
                                                toastMessage = "Error al abrir cámara: ${e.message}"
                                                toastType = ToastType.ERROR
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (capturedImageBytes != null) {
                                    bytesToImageBitmap(capturedImageBytes!!)?.let {
                                        androidx.compose.foundation.Image(
                                            bitmap = it,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                } else if (!imageUrl.isNullOrBlank()) {
                                    KamelImage(
                                        resource = asyncPainterResource(imageUrl!!),
                                        contentDescription = "Foto de perfil remota",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        onLoading = { progress -> 
                                            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(24.dp))
                                        },
                                        onFailure = { 
                                            Icon(Icons.Default.Person, null, tint = Color.LightGray)
                                        }
                                    )
                                } else {
                                    Icon(Icons.Default.CameraAlt, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                }
                            }
                            Column {
                                Text("FOTO DE PERFIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("Toca el círculo para tomar foto", fontSize = 10.sp, color = Color.LightGray)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PerfilTextField("NOMBRES", nombres, { nombres = it }, Modifier.weight(1f))
                            PerfilTextField("APELLIDOS", apellidos, { apellidos = it }, Modifier.weight(1f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PerfilTextField("TELÉFONO", telefono, { if(it.length <= 10) telefono = it }, Modifier.weight(1f), KeyboardType.Phone)
                            PerfilTextField("EMAIL", email, {}, Modifier.weight(1f), enabled = false)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                                PerfilTextField(
                                    label = "FECHA NACIMIENTO",
                                    value = fechaNacimiento,
                                    onValueChange = { },
                                    enabled = false,
                                    placeholder = "AAAA-MM-DD",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            PerfilTextField("DIRECCIÓN", direccion, { direccion = it }, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón a Seguridad
                        Button(
                            onClick = onNavigateToSecurity,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Security, null)
                            Spacer(Modifier.width(12.dp))
                            Text("SEGURIDAD Y ACCESO", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("VOLVER", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = Greeting().updateProfile(
                                            PerfilCliente(nombres, apellidos, email, telefono, fechaNacimiento, direccion),
                                            null
                                        )
                                        if (result.success) {
                                            toastType = ToastType.SUCCESS
                                            toastMessage = "Perfil actualizado con éxito"
                                        } else {
                                            toastType = ToastType.ERROR
                                            toastMessage = result.message
                                        }
                                        isLoading = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                enabled = !isLoading
                            ) {
                                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                else Text("GUARDAR", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (toastMessage != null) {
        WolfToast(toastMessage!!, toastType) { toastMessage = null }
    }
}

@Composable
fun PerfilTextField(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis); onDismiss() }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) { DatePicker(state = datePickerState) }
}

fun formatMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.UTC)
    val year = dateTime.year
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    return "$year-$month-$day"
}
