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
import androidx.compose.ui.text.style.TextDecoration
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
fun AdminEditarPromocionView(promotion: Promotion, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cameraManager = remember { CameraManager() }
    
    // Form States
    var nombre by remember { mutableStateOf(promotion.nombre) }
    var descripcion by remember { mutableStateOf(promotion.descripcion ?: "") }
    var promoPrice by remember { mutableStateOf(promotion.precioPromocional.toString()) }
    var startDate by remember { mutableStateOf(promotion.fechaInicio) }
    var endDate by remember { mutableStateOf(promotion.fechaFinal) }
    var selectedServiceIds by remember { mutableStateOf(promotion.selectedServiceIds.toSet()) }
    
    // Media State
    var promoImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageUrl by remember { mutableStateOf(promotion.imagenUrl) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Data States
    var servicesList by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val originalPrice = servicesList.filter { selectedServiceIds.contains(it.id) }.sumOf { it.precio ?: 0.0 }
    val discount = if(originalPrice > 0 && promoPrice.isNotEmpty()) {
        val p = promoPrice.toDoubleOrNull() ?: 0.0
        if (p < originalPrice) ((originalPrice - p) / originalPrice * 100).toInt() else 0
    } else 0

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            servicesList = Greeting().getServices()
        }
    }

    if (showStartPicker) {
        com.example.myapplication_prueba.cliente.DatePickerModal(
            onDateSelected = { it?.let { startDate = com.example.myapplication_prueba.cliente.formatMillisToDate(it) } },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        com.example.myapplication_prueba.cliente.DatePickerModal(
            onDateSelected = { it?.let { endDate = com.example.myapplication_prueba.cliente.formatMillisToDate(it) } },
            onDismiss = { showEndPicker = false }
        )
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
                    Text("Editar Promoción", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Actualiza el combo y su vigencia", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Identidad del Combo
            ExpressCard(title = "Detalles Generales", subtitle = "Nombre y servicios incluidos.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FormTextField(label = "Nombre de Promoción", value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Pack Wolf", isBlack = true)
                    FormTextField(label = "Descripción", value = descripcion, onValueChange = { descripcion = it }, placeholder = "Detalles...", singleLine = false, minLines = 2)
                    
                    Column {
                        Text("SERVICIOS INCLUIDOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        Spacer(Modifier.height(12.dp))
                        servicesList.forEach { service ->
                            val isSelected = selectedServiceIds.contains(service.id)
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                                    selectedServiceIds = if(isSelected) selectedServiceIds - service.id!! else selectedServiceIds + service.id!!
                                },
                                color = if(isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(service.nombre, fontWeight = FontWeight.Bold)
                                        Text("$${service.precio}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    if(isSelected) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // Precio y Fechas
            ExpressCard(title = "Precio y Disponibilidad", subtitle = "Vigencia de la oferta.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormTextField(label = "Precio Promo", value = promoPrice, onValueChange = { promoPrice = it }, placeholder = "0.00", keyboardType = KeyboardType.Number, isBlack = true)
                            if(discount > 0) {
                                Surface(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFDC2626).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                    Text("$discount% OFF", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFDC2626), fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ORIGINAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("$${originalPrice.format(2)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.LightGray, textDecoration = TextDecoration.LineThrough)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f).clickable { showStartPicker = true }) {
                            Text("INICIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(startDate.ifBlank { "Seleccionar" }, modifier = Modifier.padding(top = 8.dp).fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(16.dp), fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f).clickable { showEndPicker = true }) {
                            Text("FIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(endDate.ifBlank { "Seleccionar" }, modifier = Modifier.padding(top = 8.dp).fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(16.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Banner Image
            Column {
                Text("BANNER PROMOCIONAL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF121212)).clickable {
                        showImageSourceDialog = true
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (promoImageBytes != null) {
                        val bitmap = bytesToImageBitmap(promoImageBytes!!)
                        if (bitmap != null) Image(bitmap = bitmap, null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    } else if (!imageUrl.isNullOrBlank()) {
                        KamelImage(
                            resource = asyncPainterResource(imageUrl!!),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                            onFailure = { Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(40.dp)) }
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Añadir Imagen", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Seleccionar Imagen", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Desde dónde deseas obtener el banner de la promoción?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showImageSourceDialog = false
                                coroutineScope.launch {
                                    val image = cameraManager.takePhoto()
                                    if (image != null) promoImageBytes = image
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
                                    if (image != null) promoImageBytes = image
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
                        val res = Greeting().updatePromotion(
                            promotion.copy(
                                nombre = nombre,
                                descripcion = descripcion,
                                precioOriginal = originalPrice,
                                precioPromocional = promoPrice.toDoubleOrNull() ?: 0.0,
                                fechaInicio = startDate,
                                fechaFinal = endDate,
                                selectedServiceIds = selectedServiceIds.toList()
                            ),
                            promoImageBytes
                        )
                        isLoading = false
                        if(res.success) {
                            toastMessage = "Promoción actualizada"; toastType = ToastType.SUCCESS
                            kotlinx.coroutines.delay(1000); onBack()
                        } else {
                            toastMessage = res.message; toastType = ToastType.ERROR
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && nombre.isNotBlank() && selectedServiceIds.isNotEmpty()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("ACTUALIZAR PROMOCIÓN", fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}
