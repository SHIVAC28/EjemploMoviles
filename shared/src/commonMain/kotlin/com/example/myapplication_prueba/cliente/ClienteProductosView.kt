package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.Product
import com.example.myapplication_prueba.admin.EmptyPlaceholder
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@Composable
fun ClienteProductosView() {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            products = Greeting().getProducts().filter { it.activo }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFDC2626))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Productos Wolf-Look",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color(0xFF0F172A)
                )

                if (products.isEmpty()) {
                    EmptyPlaceholder("No hay productos disponibles por el momento", Icons.Default.Inventory)
                } else {
                    products.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { product ->
                                ClienteProductCard(product, Modifier.weight(1f))
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ClienteProductCard(product: Product, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFF8FAFC))) {
                if (!product.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(product.imagenUrl!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.Center)) },
                        onFailure = { Icon(Icons.Default.Inventory2, null, tint = Color(0xFFE2E8F0), modifier = Modifier.size(48.dp).align(Alignment.Center)) }
                    )
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color(0xFFE2E8F0), modifier = Modifier.size(48.dp).align(Alignment.Center))
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.category?.nombre?.uppercase() ?: "GENERAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Text(product.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$${product.precio}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                
                if (product.stock > 0) {
                    Text("Disponible", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                } else {
                    Text("Agotado", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
