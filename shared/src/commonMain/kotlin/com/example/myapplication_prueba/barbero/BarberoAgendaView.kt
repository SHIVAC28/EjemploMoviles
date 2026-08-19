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
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun BarberoAgendaView() {
    val coroutineScope = rememberCoroutineScope()
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            appointments = Greeting().getAppointments(today) // Backend filters by logged barber
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F6F6)).padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFDC2626))
        } else if (appointments.isEmpty()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Text("No tienes citas para hoy", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(appointments) { appt ->
                    BarberAppointmentCard(
                        appt = appt,
                        onAction = { newStatus ->
                            coroutineScope.launch {
                                val res = Greeting().updateAppointmentStatus(appt.id, newStatus)
                                if (res.success) {
                                    toastMessage = "Estado actualizado a $newStatus"
                                    toastType = ToastType.SUCCESS
                                    refreshData()
                                } else {
                                    toastMessage = res.message
                                    toastType = ToastType.ERROR
                                }
                            }
                        }
                    )
                }
            }
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun BarberAppointmentCard(appt: Appointment, onAction: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CLIENTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("${appt.customer?.nombre} ${appt.customer?.apellido}", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Surface(
                    color = when(appt.status.lowercase()) {
                        "pending" -> Color(0xFFFEF3C7)
                        "confirmed" -> Color(0xFFDBEAFE)
                        "completed" -> Color(0xFFDCFCE7)
                        else -> Color(0xFFF1F5F9)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        appt.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.Black,
                        color = when(appt.status.lowercase()) {
                            "pending" -> Color(0xFF92400E)
                            "confirmed" -> Color(0xFF1E40AF)
                            "completed" -> Color(0xFF166534)
                            else -> Color.Gray
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("SERVICIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(appt.service?.nombre ?: "Varios", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("HORARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(appt.startTime.take(5), fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("PRECIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$${appt.totalPrice}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                }
            }

            if (appt.status.lowercase() == "pending" || appt.status.lowercase() == "confirmed") {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (appt.status.lowercase() == "pending") {
                        Button(
                            onClick = { onAction("confirmed") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("ACEPTAR", fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    }
                    if (appt.status.lowercase() == "confirmed") {
                        Button(
                            onClick = { onAction("completed") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("COMPLETAR", fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    }
                    OutlinedButton(
                        onClick = { onAction("cancelled") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("RECHAZAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red) }
                }
            }
        }
    }
}
