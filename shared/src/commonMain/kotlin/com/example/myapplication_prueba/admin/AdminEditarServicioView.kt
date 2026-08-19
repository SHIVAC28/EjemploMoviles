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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import com.example.myapplication_prueba.sensor.CameraManager
import com.example.myapplication_prueba.sensor.bytesToImageBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditarServicioView(service: Service, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cameraManager = remember { CameraManager() }
    
    // Form States
    var nombre by remember { mutableStateOf(service.nombre) }
    var precio by remember { mutableStateOf(service.precio?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(service.descripcion ?: "") }
    var duracion by remember { mutableStateOf(service.duracion?.toString() ?: "") }
    var activo by remember { mutableStateOf(service.activo) }
    var selectedCategoryId by remember { mutableStateOf(service.serviceCategory?.id) }
    
    // Media State
    var serviceImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Data States
    var categories by remember { mutableStateOf<List<ServiceCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf(service.imagenUrl) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            categories = Greeting().getServiceCategories()
        }
    }

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
                    Text("Editar Servicio", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Modifica las configuraciones de tu servicio", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Form Section
            ExpressCard(title = "Detalles del Servicio", subtitle = "Nombre, precio y categoría.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FormTextField(label = "Nombre del Servicio", value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Corte Skin Fade", isBlack = true)
                    FormTextField(label = "Precio ($)", value = precio, onValueChange = { precio = it }, placeholder = "0.00", keyboardType = KeyboardType.Number, isBlack = true)

                    Column {
                        Text("CATEGORÍA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = categories.find { it.id == selectedCategoryId }?.nombre ?: "Seleccionar Categoría",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0))
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat.nombre) }, onClick = { selectedCategoryId = cat.id; expanded = false })
                                }
                            }
                        }
                    }

                    FormTextField(label = "Descripción", value = descripcion, onValueChange = { descripcion = it }, placeholder = "Explica la técnica...", singleLine = false, minLines = 3)
                    
                    Column {
                        Text("DURACIÓN (MINUTOS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("30", "45", "60", "90").forEach { time ->
                                val isSelected = duracion == time
                                Surface(
                                    modifier = Modifier.clickable { duracion = time },
                                    color = if(isSelected) Color(0xFFDC2626).copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if(isSelected) BorderStroke(1.dp, Color(0xFFDC2626)) else null
                                ) {
                                    Text("${time}m", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), fontWeight = FontWeight.Black, color = if(isSelected) Color(0xFFDC2626) else Color.Gray)
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Estado del Servicio", fontWeight = FontWeight.Bold)
                        Switch(checked = activo, onCheckedChange = { activo = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFDC2626)))
                    }
                }
            }

            // Client Preview
            Column {
                Text("PREVISUALIZACIÓN CLIENTE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                ServicePreviewCard(
                    nombre = nombre.ifBlank { "Nombre del Servicio" },
                    precio = precio.ifBlank { "0.00" },
                    duracion = duracion.ifBlank { "0" },
                    descripcion = descripcion.ifBlank { "Descripción aquí..." },
                    imageBitmap = bytesToImageBitmap(serviceImageBytes ?: byteArrayOf()),
                    remoteImageUrl = imageUrl,
                    onImageClick = { showImageSourceDialog = true }
                )
            }

            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Seleccionar Imagen", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Desde dónde deseas obtener la imagen para el servicio?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showImageSourceDialog = false
                                coroutineScope.launch {
                                    val image = cameraManager.takePhoto()
                                    if (image != null) serviceImageBytes = image
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cámara")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showImageSourceDialog = false
                                coroutineScope.launch {
                                    val image = cameraManager.pickImage()
                                    if (image != null) serviceImageBytes = image
                                }
                            }
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Galería")
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Submit
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val res = Greeting().updateService(
                            service.copy(
                                nombre = nombre,
                                precio = precio.toDoubleOrNull() ?: 0.0,
                                duracion = duracion.toIntOrNull() ?: 0,
                                activo = activo,
                                descripcion = descripcion,
                                serviceCategory = categories.find { it.id == selectedCategoryId }
                            ),
                            serviceImageBytes
                        )
                        isLoading = false
                        if (res.success) {
                            toastMessage = "Servicio actualizado"
                            toastType = ToastType.SUCCESS
                            kotlinx.coroutines.delay(1000)
                            onBack()
                        } else {
                            toastMessage = res.message
                            toastType = ToastType.ERROR
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && nombre.isNotBlank() && precio.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ACTUALIZAR SERVICIO", fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}
