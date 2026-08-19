package com.example.myapplication_prueba.cliente

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

@Composable
fun ClienteNotificacionesView() {
    val notifications = listOf(
        NotificationItem("Wolf-Look", "¡Tu cita ha sido confirmada para mañana!", "10:00 AM", Icons.Default.CheckCircle, Color(0xFF10B981)),
        NotificationItem("Sistema", "¡Bienvenido a la nueva App de Barbería Wolf!", "Hace 2 horas", Icons.Default.Notifications, Color(0xFFDC2626)),
        NotificationItem("Promo", "Aprovecha 20% OFF en combos hoy.", "Ayer", Icons.Default.LocalOffer, Color(0xFFF59E0B))
    )

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F6F6)).padding(16.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notifications) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(item.color.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.message, fontSize = 12.sp, color = Color.Gray)
                            Text(item.time, fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

data class NotificationItem(val title: String, val message: String, val time: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
