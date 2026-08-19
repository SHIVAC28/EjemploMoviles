package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@Composable
fun NuevoBarberoView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cameraManager = remember { CameraManager() }
    
    // Form States
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Media State
    var barberImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Specialties
    var categories by remember { mutableStateOf<List<ServiceCategory>>(emptyList()) }
    var selectedCategoryNames by remember { mutableStateOf(setOf<String>()) }
    
    // Schedule
    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            categories = Greeting().getServiceCategories()
            // Set default schedule: Mon-Fri all day, Sun partial
            val defaultSlots = mutableSetOf<String>()
            val hours = (10..20).map { "${it.toString().padStart(2, '0')}:00" }
            for (hour in hours) {
                for (day in 1..5) defaultSlots.add("$day-$hour")
                if (hour.take(2).toInt() <= 16) defaultSlots.add("0-$hour")
            }
            selectedSlots = defaultSlots
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
                    Text("Añadir Maestro Barbero", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Configure los detalles del nuevo miembro", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Info Section
            ExpressCard(title = "Información Personal", subtitle = "Datos básicos de contacto.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Avatar / Photo Selection
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showImageSourceDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = bytesToImageBitmap(barberImageBytes ?: byteArrayOf())
                            if (bitmap != null) {
                                Image(bitmap = bitmap, null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            }
                        }
                        Column {
                            Text("FOTOGRAFÍA EDITORIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            Text("Toca para añadir foto", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    FormTextField(label = "Nombre Completo", value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Julian Rossi")
                    FormTextField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "barbero@wolf.com", keyboardType = KeyboardType.Email)
                    FormTextField(label = "Teléfono", value = telefono, onValueChange = { telefono = it }, placeholder = "0000000000", keyboardType = KeyboardType.Phone)
                    FormTextField(label = "Contraseña de Acceso", value = password, onValueChange = { password = it }, placeholder = "Mín. 6 caracteres", keyboardType = KeyboardType.Password)
                    FormTextField(label = "Bio Corta", value = bio, onValueChange = { bio = it }, placeholder = "Maestro en...", singleLine = false, minLines = 3)
                }
            }

            // Specialties
            ExpressCard(title = "Especialidades", subtitle = "Categorías de servicios calificados.") {
                var showAddCategoryModal by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LISTA DE ESPECIALIDADES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        IconButton(
                            onClick = { showAddCategoryModal = true },
                            modifier = Modifier.size(32.dp).background(Color(0xFFDC2626).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        }
                    }

                    categories.forEach { cat ->
                        val isChecked = selectedCategoryNames.contains(cat.nombre)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedCategoryNames = if(isChecked) selectedCategoryNames - cat.nombre else selectedCategoryNames + cat.nombre
                                },
                            color = if(isChecked) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if(isChecked) Color(0xFFDC2626) else Color(0xFFE2E8F0))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if(isChecked) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (showAddCategoryModal) {
                    AddCategoryModal(
                        onDismiss = { showAddCategoryModal = false },
                        onConfirm = { catName ->
                            coroutineScope.launch {
                                val res = Greeting().createServiceCategory(catName)
                                if (res.success) {
                                    val updatedCats = Greeting().getServiceCategories()
                                    categories = updatedCats
                                    selectedCategoryNames = selectedCategoryNames + catName
                                    showAddCategoryModal = false
                                } else {
                                    toastMessage = res.message
                                    toastType = ToastType.ERROR
                                }
                            }
                        }
                    )
                }
            }

            // Schedule Grid
            ExpressCard(title = "Disponibilidad", subtitle = "Seleccione los bloques de horario activo.") {
                Column {
                    val days = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
                    val hours = (10..20).map { "${it.toString().padStart(2, '0')}:00" }
                    
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9)).padding(vertical = 4.dp)) {
                        Spacer(Modifier.width(50.dp))
                        days.forEach { day ->
                            Text(day, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                    
                    Box(modifier = Modifier.height(300.dp)) {
                        LazyColumn {
                            items(hours) { hour ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(hour, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), color = Color.Gray)
                                    (1..7).forEach { dayIndex ->
                                        val day = if(dayIndex == 7) 0 else dayIndex
                                        val slot = "$day-$hour"
                                        val isSelected = selectedSlots.contains(slot)
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) Color(0xFFDC2626) else Color(0xFFF1F5F9))
                                                .clickable {
                                                    selectedSlots = if (isSelected) selectedSlots - slot else selectedSlots + slot
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if(isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Submit
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val res = Greeting().createBarber(
                            Barber(
                                nombreCompleto = nombre,
                                email = email,
                                telefono = telefono,
                                bio = bio,
                                activo = true,
                                scheduleConfiguration = selectedSlots.joinToString(","),
                                specialties = selectedCategoryNames.toList()
                            ),
                            barberImageBytes,
                            password
                        )
                        isLoading = false
                        if (res.success) {
                            toastMessage = "Barbero registrado con éxito"
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
                enabled = !isLoading && nombre.isNotBlank() && email.isNotBlank() && telefono.isNotBlank() && password.length >= 6
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(Icons.Default.PersonAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text("FINALIZAR REGISTRO", fontWeight = FontWeight.Black)
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
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            coroutineScope.launch {
                                val image = cameraManager.takePhoto()
                                if (image != null) barberImageBytes = image
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
                                if (image != null) barberImageBytes = image
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

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}
