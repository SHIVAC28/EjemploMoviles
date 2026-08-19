package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
fun NuevoServicioView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cameraManager = remember { CameraManager() }
    
    // Form States
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    
    // Media State
    var serviceImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Data States
    var categories by remember { mutableStateOf<List<ServiceCategory>>(emptyList()) }
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
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Nuevo Servicio", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Configura el catálogo", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    
                    FormTextField(label = "Nombre del Servicio", value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Corte Skin Fade", isBlack = true)
                    FormTextField(label = "Precio ($)", value = precio, onValueChange = { precio = it }, placeholder = "0.00", keyboardType = KeyboardType.Number, isBlack = true)

                    // Categoría
                    var showAddCategoryModal by remember { mutableStateOf(false) }
                    Column {
                        Text("CATEGORÍA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.weight(1f)
                            ) {
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
                            IconButton(onClick = { showAddCategoryModal = true }, modifier = Modifier.background(Color(0xFFDC2626).copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                                Icon(Icons.Default.Add, null, tint = Color(0xFFDC2626))
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
                                        val updated = Greeting().getServiceCategories()
                                        categories = updated
                                        selectedCategoryId = updated.find { it.nombre == catName }?.id
                                        showAddCategoryModal = false
                                    }
                                }
                            }
                        )
                    }

                    FormTextField(label = "Descripción", value = descripcion, onValueChange = { descripcion = it }, placeholder = "Detalles...", singleLine = false, minLines = 3)

                    // Duración
                    Column {
                        Text("DURACIÓN (MINUTOS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
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
                }
            }

            // Preview Section with Camera Action
            Column {
                Text("PREVISUALIZACIÓN CLIENTE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                ServicePreviewCard(
                    nombre = nombre.ifBlank { "Nombre del Servicio" },
                    precio = precio.ifBlank { "0.00" },
                    duracion = duracion.ifBlank { "0" },
                    descripcion = descripcion.ifBlank { "..." },
                    imageBitmap = bytesToImageBitmap(serviceImageBytes ?: byteArrayOf()),
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
                        val res = Greeting().createService(
                            Service(
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
                            toastMessage = "Servicio guardado"; toastType = ToastType.SUCCESS
                            kotlinx.coroutines.delay(1000); onBack()
                        } else {
                            toastMessage = res.message; toastType = ToastType.ERROR
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && nombre.isNotBlank() && precio.isNotBlank() && selectedCategoryId != null
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("GUARDAR SERVICIO", fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun ServicePreviewCard(
    nombre: String, 
    precio: String, 
    duracion: String, 
    descripcion: String, 
    imageBitmap: ImageBitmap?, 
    remoteImageUrl: String? = null,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onImageClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                if (imageBitmap != null) {
                    Image(bitmap = imageBitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else if (!remoteImageUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(remoteImageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                        onFailure = { Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.LightGray, modifier = Modifier.size(48.dp)) }
                    )
                } else {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                }
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Surface(modifier = Modifier.align(Alignment.TopEnd), color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp)) {
                        Text("$${precio}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(nombre, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF0F172A))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text("${duracion} mins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(descripcion, fontSize = 13.sp, color = Color(0xFF475569), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
