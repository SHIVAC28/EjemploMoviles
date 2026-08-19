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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import kotlinx.coroutines.launch

@Composable
fun AdminDashboard() {
    val coroutineScope = rememberCoroutineScope()
    var stats by remember { mutableStateOf(ReportStats(0, 0.0)) }
    var barberStats by remember { mutableStateOf(BarberStats(0, 0, 0)) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Simplified fetch
            stats = Greeting().getReportStats("2024-01-01", "2024-12-31", null, null)
            barberStats = Greeting().getBarberStats()
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F6F6)).padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFDC2626))
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Resumen General", fontWeight = FontWeight.Black, fontSize = 20.sp)
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Ingresos", "$${stats.totalIncome}", Icons.Default.Payments, Color(0xFF10B981), Modifier.weight(1f))
                    StatCard("Citas", "${stats.totalApps}", Icons.Default.Event, Color(0xFFDC2626), Modifier.weight(1f))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Equipo", "${barberStats.totalBarbers}", Icons.Default.Face, Color(0xFF6366F1), Modifier.weight(1f))
                    StatCard("Activos", "${barberStats.activeBarbers}", Icons.Default.CheckCircle, Color(0xFFF59E0B), Modifier.weight(1f))
                }

                ExpressCard("Rendimiento", "Métricas del mes actual") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(progress = { 0.7f }, modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp)), color = Color(0xFFDC2626))
                        Text("Meta de ventas: 70%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}
