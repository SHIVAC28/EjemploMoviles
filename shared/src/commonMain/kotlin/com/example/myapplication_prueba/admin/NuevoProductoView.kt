package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProductoView(product: Product? = null, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cameraManager = remember { CameraManager() }
    
    // Form States
    var nombre by remember { mutableStateOf(product?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(product?.descripcion ?: "") }
    var precio by remember { mutableStateOf(product?.precio?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var activo by remember { mutableStateOf(product?.activo ?: true) }
    var selectedCategoryId by remember { mutableStateOf(product?.category?.id) }
    
    // Media State
    var productImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Data States
    var categories by remember { mutableStateOf<List<ServiceCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            categories = Greeting().getProductCategories()
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
                    Text(if (product == null) "Nuevo Producto" else "Editar Producto", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Configura los detalles del catálogo", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Info Section
            ExpressCard(title = "Identidad del Producto", subtitle = "Nombre, descripción y categoría.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FormTextField(label = "Nombre del Producto", value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Matte Clay Pomade", isBlack = true)
                    FormTextField(label = "Descripción", value = descripcion, onValueChange = { descripcion = it }, placeholder = "Detalles del producto...", singleLine = false, minLines = 3)
                    
                    Column {
                        var showAddCategoryModal by remember { mutableStateOf(false) }
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
                            IconButton(
                                onClick = { showAddCategoryModal = true },
                                modifier = Modifier.background(Color(0xFFDC2626).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color(0xFFDC2626))
                            }
                        }

                        if (showAddCategoryModal) {
                            AddCategoryModal(
                                onDismiss = { showAddCategoryModal = false },
                                onConfirm = { catName ->
                                    coroutineScope.launch {
                                        val res = Greeting().createProductCategory(catName)
                                        if (res.success) {
                                            val updatedCats = Greeting().getProductCategories()
                                            categories = updatedCats
                                            selectedCategoryId = updatedCats.find { it.nombre == catName }?.id
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
                    
                    FormTextField(label = "SKU / Código Interno", value = sku, onValueChange = { sku = it }, placeholder = "WL-SKU-001")
                }
            }

            // Pricing Section
            ExpressCard(title = "Precios e Inventario", subtitle = "Valor unitario y stock disponible.") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(label = "Precio ($)", value = precio, onValueChange = { precio = it }, placeholder = "0.00", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f), isBlack = true)
                    FormTextField(label = "Stock", value = stock, onValueChange = { stock = it }, placeholder = "0", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f), isBlack = true)
                }
            }

            // Media & Visibility
            ExpressCard(title = "Multimedia y Visibilidad", subtitle = "Imagen y estado en tienda.") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .clickable { showImageSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = bytesToImageBitmap(productImageBytes ?: byteArrayOf())
                        if (bitmap != null) {
                            Image(bitmap = bitmap, null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                        } else if (!product?.imagenUrl.isNullOrBlank()) {
                            KamelImage(
                                resource = asyncPainterResource(product!!.imagenUrl!!),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                                onFailure = { Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.LightGray, modifier = Modifier.size(40.dp)) }
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                Text("Subir Imagen", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Visibilidad en Tienda", fontWeight = FontWeight.Bold)
                            Text("Habilita compras inmediatas", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(checked = activo, onCheckedChange = { activo = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFDC2626)))
                    }
                }
            }

            // Submit
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val prodData = Product(
                            id = product?.id,
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio.toDoubleOrNull() ?: 0.0,
                            stock = stock.toIntOrNull() ?: 0,
                            sku = sku,
                            activo = activo,
                            category = categories.find { it.id == selectedCategoryId }
                        )
                        val res = if (product == null) {
                            Greeting().createProduct(prodData, productImageBytes)
                        } else {
                            Greeting().updateProduct(prodData, productImageBytes)
                        }
                        isLoading = false
                        if (res.success) {
                            toastMessage = "Producto guardado"
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
                enabled = !isLoading && nombre.isNotBlank() && precio.isNotBlank() && stock.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(if (product == null) Icons.Default.Add else Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (product == null) "AÑADIR PRODUCTO" else "GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Seleccionar Imagen", fontWeight = FontWeight.Bold) },
                text = { Text("¿Desde dónde deseas obtener la imagen para el producto?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            coroutineScope.launch {
                                val image = cameraManager.takePhoto()
                                if (image != null) productImageBytes = image
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
                                if (image != null) productImageBytes = image
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

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}
