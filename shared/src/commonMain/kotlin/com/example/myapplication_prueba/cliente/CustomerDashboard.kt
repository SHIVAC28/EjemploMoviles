package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerDashboard() {
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFD32F2F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F6))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "BIENVENIDO DE NUEVO",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tu Próximo Estilo está a un clic.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Agenda tu cita rápidamente y asegura tu espacio con los mejores barberos.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Navegar a Book */ },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AGENDAR CITA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Box(modifier = Modifier.width(4.dp).height(24.dp).background(primaryColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Últimos Servicios", fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text("Basado en tu historial reciente", color = Color.Gray, fontSize = 12.sp)
            }
            Text(
                "VER HISTORIAL",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Services List
        val services = listOf(
            ServiceData("Corte Moderno", "28 Jun 2026 a las 14:30", "500", "Programada"),
            ServiceData("Barba VIP", "20 Jun 2026 a las 10:00", "350", "Completada")
        )

        if (services.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Text("Aún no tienes servicios agendados.", color = Color.Gray)
                }
            }
        } else {
            services.forEach { service ->
                ServiceCard(service)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class ServiceData(val title: String, val dateTime: String, val price: String, val status: String)

@Composable
fun ServiceCard(data: ServiceData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFD32F2F).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color(0xFFD32F2F))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text(data.dateTime, color = Color.Gray, fontSize = 12.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("$${data.price}", fontWeight = FontWeight.Black, fontSize = 20.sp)
                val statusColor = when(data.status) {
                    "Programada" -> Color(0xFFB45309) // Amber
                    "Completada" -> Color(0xFF15803D) // Green
                    else -> Color(0xFFB91C1C) // Red
                }
                val statusBg = statusColor.copy(alpha = 0.1f)
                
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(percent = 50),
                    border = null // Can add border if needed
                ) {
                    Text(
                        text = data.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}
