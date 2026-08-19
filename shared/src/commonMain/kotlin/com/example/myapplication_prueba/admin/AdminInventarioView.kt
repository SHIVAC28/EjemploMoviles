package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@Composable
fun AdminInventarioView(
    onNavigateToNewProduct: () -> Unit,
    onNavigateToEditProduct: (Product) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var stats by remember { mutableStateOf(InventoryStats(0, 0, 0.0)) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productToReduce by remember { mutableStateOf<Product?>(null) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            products = Greeting().getProducts()
            stats = Greeting().getInventoryStats()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Inventario", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color(0xFF0F172A))
                    Text("Control de stock y tienda", fontSize = 14.sp, color = Color.Gray)
                }
                Button(
                    onClick = onNavigateToNewProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir", fontWeight = FontWeight.Bold)
                }
            }

            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InventoryStatCard(Modifier.weight(1f), "TOTAL", stats.totalProducts.toString(), Icons.Default.Inventory2, Color(0xFF3B82F6))
                InventoryStatCard(Modifier.weight(1f), "STOCK BAJO", stats.lowStock.toString(), Icons.Default.Warning, Color(0xFFEF4444), isAlert = true)
                InventoryStatCard(Modifier.weight(1f), "VALOR", "$${stats.inventoryValue.format(0)}", Icons.Default.Payments, Color(0xFF10B981))
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o SKU...", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // Products Grid
            val filteredProducts = products.filter { 
                it.nombre.contains(searchQuery, ignoreCase = true) || (it.sku?.contains(searchQuery, ignoreCase = true) == true)
            }

            if (filteredProducts.isEmpty() && !isLoading) {
                EmptyInventoryPlaceholder()
            } else {
                filteredProducts.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { product ->
                            ProductInventoryCard(
                                product = product,
                                modifier = Modifier.weight(1f),
                                onEdit = { onNavigateToEditProduct(product) },
                                onDelete = { productToDelete = product },
                                onReduce = { productToReduce = product }
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Modals
        if (productToDelete != null) {
            ConfirmDialog(
                title = "¿Eliminar Producto?",
                text = "Esta acción borrará permanentemente \"${productToDelete!!.nombre}\" del catálogo.",
                confirmText = "Eliminar",
                confirmColor = Color(0xFFEF4444),
                onConfirm = {
                    coroutineScope.launch {
                        productToDelete?.id?.let { id ->
                            val res = Greeting().deleteProduct(id)
                            if (res.success) refreshData()
                            else { toastMessage = res.message; toastType = ToastType.ERROR }
                        }
                        productToDelete = null
                    }
                },
                onDismiss = { productToDelete = null }
            )
        }

        if (productToReduce != null) {
            var cantidad by remember { mutableStateOf("1") }
            AlertDialog(
                onDismissRequest = { productToReduce = null },
                title = { Text("Reducir Stock", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("¿Cuántas unidades de \"${productToReduce!!.nombre}\" deseas descontar?")
                        OutlinedTextField(
                            value = cantidad,
                            onValueChange = { if(it.all { char -> char.isDigit() }) cantidad = it },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Cantidad") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val cant = cantidad.toIntOrNull() ?: 1
                                val res = Greeting().quickReduceStock(productToReduce!!.id!!, cant)
                                if (res.success) {
                                    toastMessage = "Stock actualizado"; toastType = ToastType.SUCCESS
                                    refreshData()
                                } else {
                                    toastMessage = res.message; toastType = ToastType.ERROR
                                }
                                productToReduce = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) { Text("REDUCIR") }
                },
                dismissButton = {
                    TextButton(onClick = { productToReduce = null }) { Text("CANCELAR", color = Color.Gray) }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFDC2626))
            }
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}

@Composable
fun InventoryStatCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, isAlert: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = if (isAlert) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun ProductInventoryCard(product: Product, modifier: Modifier, onEdit: () -> Unit, onDelete: () -> Unit, onReduce: () -> Unit) {
    val isLowStock = product.stock <= 3

    Card(
        modifier = modifier.clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Image Area
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFF8FAFC))) {
                if (!product.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(product.imagenUrl!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.Center)) },
                        onFailure = { Icon(Icons.Default.Inventory2, null, tint = Color(0xFFE2E8F0), modifier = Modifier.size(48.dp).align(Alignment.Center)) }
                    )
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color(0xFFE2E8F0), modifier = Modifier.size(48.dp).align(Alignment.Center))
                }
                
                // Badges
                Row(modifier = Modifier.padding(8.dp).align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isLowStock) {
                        Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(6.dp)) {
                            Text("STOCK BAJO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    if (!product.activo) {
                        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(6.dp)) {
                            Text("OCULTO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                // Actions
                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onReduce,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.9f), CircleShape).size(28.dp)
                    ) {
                        Icon(Icons.Default.RemoveShoppingCart, null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.9f), CircleShape).size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.category?.nombre?.uppercase() ?: "GENERAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Text(product.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$${product.precio}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("STOCK", fontSize = 8.sp, color = Color.Gray)
                        Text("${product.stock} un.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if(isLowStock) Color(0xFFEF4444) else Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SKU", fontSize = 8.sp, color = Color.Gray)
                        Text(product.sku ?: "N/A", fontWeight = FontWeight.Medium, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyInventoryPlaceholder() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
        Text("No se encontraron productos", color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

private fun Double.format(digits: Int): String {
    val s = this.toString()
    if (!s.contains(".")) return s
    val parts = s.split(".")
    val decimal = parts[1].padEnd(digits, '0').take(digits)
    return if (digits > 0) "${parts[0]}.$decimal" else parts[0]
}
