package com.example.myapplication_prueba.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminDashboard() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F6))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Citas Hoy",
                value = "12",
                subtitle = "Programadas",
                icon = Icons.Default.EventAvailable,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Ingresos",
                value = "$4,500",
                subtitle = "MXN Esperados",
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )
        }

        // Cierre de Caja (Special Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cierre del Día", color = Color.LightGray, fontSize = 14.sp)
                    Text("Realizar Corte de Caja", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* Navigate to cash register */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("IR", fontWeight = FontWeight.Black)
                }
            }
        }

        // Weekly Demand Chart (Simplified representation)
        Text("Demanda Semanal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val weeklyData = listOf(0.4f, 0.6f, 0.9f, 0.5f, 0.8f, 1.0f, 0.7f)
                weeklyData.forEach { heightFactor ->
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(heightFactor)
                            .background(Color(0xFFDC2626), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                }
            }
        }

        // Upcoming Tasks Table
        Text("Próximas Citas (Hoy)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                listOf(
                    AppointmentRowData("Juan P.", "Corte Moderno", "14:30", "Programada"),
                    AppointmentRowData("Maria G.", "Tinte Wolf", "15:00", "En Progreso"),
                    AppointmentRowData("Carlos R.", "Barba VIP", "16:00", "Programada")
                ).forEach { appointment ->
                    AppointmentRow(appointment)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.Gray, fontSize = 12.sp)
                Icon(icon, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
            }
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

data class AppointmentRowData(val name: String, val service: String, val time: String, val status: String)

@Composable
fun AppointmentRow(data: AppointmentRowData) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(data.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(data.service, color = Color.Gray, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(data.time, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                data.status,
                color = if (data.status == "En Progreso") Color(0xFFDC2626) else Color(0xFF28A745),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
