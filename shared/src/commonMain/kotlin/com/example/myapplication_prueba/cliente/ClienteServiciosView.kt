package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.Service
import com.example.myapplication_prueba.admin.EmptyPlaceholder
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@Composable
fun ClienteServiciosView() {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            services = Greeting().getServices().filter { it.activo }
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
                    "Catálogo de Servicios",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color(0xFF0F172A)
                )

                if (services.isEmpty()) {
                    EmptyPlaceholder("No hay servicios disponibles por el momento", Icons.Default.DryCleaning)
                } else {
                    services.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { service ->
                                ClienteServiceCard(service, Modifier.weight(1f))
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
fun ClienteServiceCard(service: Service, modifier: Modifier) {
    Card(
        modifier = modifier.aspectRatio(0.8f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
                if (!service.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(service.imagenUrl!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.Center)) },
                        onFailure = { Icon(Icons.Default.Storefront, null, tint = Color(0xFF334155), modifier = Modifier.size(60.dp).align(Alignment.Center)) }
                    )
                } else {
                    Icon(Icons.Default.Storefront, null, tint = Color(0xFF334155), modifier = Modifier.size(60.dp).align(Alignment.Center))
                }
            }
            
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.9f)))
            ))
            
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Bottom) {
                Text(service.serviceCategory?.nombre?.uppercase() ?: "GENERAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Text(service.nombre, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Text("${service.duracion} min", color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("$${service.precio}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
