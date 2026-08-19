package com.example.myapplication_prueba.barbero

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.admin.ExpressCard
import kotlinx.coroutines.launch

@Composable
fun BarberoClientesView() {
    val coroutineScope = rememberCoroutineScope()
    var customers by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            customers = Greeting().getCustomers()
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F6F6)).padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFDC2626))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(customers) { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                                Text(customer.nombre.take(1).uppercase(), fontWeight = FontWeight.Black, color = Color.Gray)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${customer.nombre} ${customer.apellido}", fontWeight = FontWeight.Bold)
                                Text(customer.telefono, fontSize = 12.sp, color = Color.Gray)
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { /* Call or Message */ }) {
                                Icon(Icons.Default.Phone, null, tint = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}
