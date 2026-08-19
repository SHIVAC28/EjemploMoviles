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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import com.example.myapplication_prueba.sensor.CameraManager
import com.example.myapplication_prueba.sensor.bytesToImageBitmap
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditarBarberoView(barber: Barber, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cameraManager = remember { CameraManager() }
    
    // Form States
    var nombre by remember { mutableStateOf(barber.nombreCompleto) }
    var email by remember { mutableStateOf(barber.email ?: "") }
    var telefono by remember { mutableStateOf(barber.telefono ?: "") }
    var bio by remember { mutableStateOf(barber.bio ?: "") }
    var password by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(barber.activo) }
    
    // Media State
    var barberImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageUrl by remember { mutableStateOf(barber.imagenUrl) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Specialties
    var categories by remember { mutableStateOf<List<ServiceCategory>>(emptyList()) }
    var selectedCategoryNames by remember { mutableStateOf(barber.specialties.toSet()) }
    
    var isLoading by remember { mutableStateOf(false) }
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
                IconButton(onClick = onBack, modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Editar Perfil", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text(barber.nombreCompleto, fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Profile Section
            ExpressCard(title = "Datos del Profesional", subtitle = "Información pública y contacto.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Image Selection
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showImageSourceDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (barberImageBytes != null) {
                                val bitmap = bytesToImageBitmap(barberImageBytes!!)
                                if (bitmap != null) Image(bitmap = bitmap, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else if (!imageUrl.isNullOrBlank()) {
                                KamelImage(
                                    resource = asyncPainterResource(imageUrl!!),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onLoading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) } },
                                    onFailure = { Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(40.dp)) }
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            }
                        }
                        Column {
                            Text("FOTO DE PERFIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            Text("Toca para cambiar", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    FormTextField(label = "Nombre Completo", value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Julian Rossi")
                    FormTextField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "barbero@wolf.com", keyboardType = KeyboardType.Email)
                    FormTextField(label = "Teléfono", value = telefono, onValueChange = { telefono = it }, placeholder = "0000000000", keyboardType = KeyboardType.Phone)
                    FormTextField(label = "Nueva Contraseña (Opcional)", value = password, onValueChange = { password = it }, placeholder = "Dejar vacío para no cambiar", keyboardType = KeyboardType.Password)
                    FormTextField(label = "Biografía", value = bio, onValueChange = { bio = it }, placeholder = "Maestro en...", singleLine = false, minLines = 3)
                }
            }

            // Specialties
            ExpressCard(title = "Especialidades", subtitle = "Define en qué servicios destaca.") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        val isChecked = selectedCategoryNames.contains(cat.nombre)
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                selectedCategoryNames = if(isChecked) selectedCategoryNames - cat.nombre else selectedCategoryNames + cat.nombre
                            },
                            color = if(isChecked) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if(isChecked) Color(0xFFDC2626) else Color(0xFFE2E8F0))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat.nombre, fontWeight = FontWeight.Bold)
                                if(isChecked) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Status Toggle
            ExpressCard(title = "Estado de Cuenta", subtitle = "Habilita o deshabilita al barbero.") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(if(activo) "CUENTA ACTIVA" else "CUENTA INACTIVA", fontWeight = FontWeight.Bold, color = if(activo) Color(0xFF10B981) else Color.Gray)
                    Switch(checked = activo, onCheckedChange = { activo = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981)))
                }
            }

            // Submit
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val res = Greeting().updateBarber(
                            barber.copy(
                                nombreCompleto = nombre,
                                email = email,
                                telefono = telefono,
                                bio = bio,
                                activo = activo,
                                specialties = selectedCategoryNames.toList()
                            ),
                            barberImageBytes,
                            if(password.isBlank()) null else password
                        )
                        isLoading = false
                        if(res.success) {
                            toastMessage = "Barbero actualizado"; toastType = ToastType.SUCCESS
                            kotlinx.coroutines.delay(1000); onBack()
                        } else {
                            toastMessage = res.message; toastType = ToastType.ERROR
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && nombre.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Seleccionar Foto", fontWeight = FontWeight.Bold) },
                text = { Text("¿Desde dónde deseas obtener la foto del barbero?") },
                confirmButton = {
                    Button(onClick = {
                        showImageSourceDialog = false
                        coroutineScope.launch {
                            val image = cameraManager.takePhoto()
                            if (image != null) barberImageBytes = image
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cámara")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showImageSourceDialog = false
                        coroutineScope.launch {
                            val image = cameraManager.pickImage()
                            if (image != null) barberImageBytes = image
                        }
                    }) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Galería")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}
