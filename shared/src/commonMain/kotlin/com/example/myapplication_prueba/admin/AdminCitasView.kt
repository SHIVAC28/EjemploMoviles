package com.example.myapplication_prueba.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.Appointment
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
fun AdminCitasView(onNavigateToPOS: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFDC2626)
    
    var selectedDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Fetch appointments when date changes
    LaunchedEffect(selectedDate) {
        isLoading = true
        appointments = Greeting().getAppointments(selectedDate.toString())
        isLoading = false
    }

    val startOfWeek = selectedDate.minus(selectedDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val weekDays = (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    // Header con Navegación
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedDate.month.name} ${selectedDate.year}".uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedDate = selectedDate.minus(7, DateTimeUnit.DAY) }) {
                                Icon(Icons.Default.ChevronLeft, null)
                            }
                            TextButton(onClick = { selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()) }) {
                                Text("HOY", fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            IconButton(onClick = { selectedDate = selectedDate.plus(7, DateTimeUnit.DAY) }) {
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }

                    // Grid Semanal (Labels)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        weekDays.forEach { day ->
                            Text(
                                text = day.dayOfWeek.name.take(3),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Grid Semanal (Días)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                        weekDays.forEach { day ->
                            val isToday = day == Clock.System.todayIn(TimeZone.currentSystemDefault())
                            val isSelected = day == selectedDate
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.8f)
                                    .padding(2.dp)
                                    .background(
                                        color = if (isSelected) primaryColor else if (isToday) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedDate = day }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = if (isSelected) Color.White else if (isToday) primaryColor else Color.DarkGray
                                )
                                if (isToday) {
                                    Text("HOY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if(isSelected) Color.White else primaryColor)
                                }
                            }
                        }
                    }

                    // Citas del día seleccionado (Vista Compacta dentro del calendario)
                    val daily = appointments.filter { it.date == selectedDate.toString() }
                    if (daily.isNotEmpty()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            daily.forEach { app ->
                                CompactAppointmentRow(app) { selectedAppointment = it }
                            }
                        }
                    } else {
                        Text(
                            "No hay citas para este día",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Servicio Ocasional Button
            Button(
                onClick = onNavigateToPOS,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PointOfSale, null)
                    Text("SERVICIO OCASIONAL", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            // Detail Panel
            DetailPanelMobile(
                selectedAppointment = selectedAppointment,
                onCancelClick = { showCancelDialog = true },
                onCompleteClick = { /* Logic Complete */ }
            )

            // Today Pending List
            TodayPendingCard(appointments.filter { it.date == Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() })

            Spacer(modifier = Modifier.height(80.dp)) // Espacio para el menú inferior o margen
        }

        if (showCancelDialog && selectedAppointment != null) {
            CancelConfirmationDialog(
                onConfirm = {
                    coroutineScope.launch {
                        val res = Greeting().cancelAppointment(selectedAppointment!!.id)
                        if (res.success) {
                            toastMessage = "Cita cancelada"
                            toastType = ToastType.SUCCESS
                            appointments = Greeting().getAppointments(selectedDate.toString())
                            selectedAppointment = null
                        } else {
                            toastMessage = res.message
                            toastType = ToastType.ERROR
                        }
                    }
                    showCancelDialog = false
                },
                onDismiss = { showCancelDialog = false }
            )
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}

@Composable
fun CancelConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cancelar Cita?", fontWeight = FontWeight.Black) },
        text = { Text("Esta acción liberará el espacio en la agenda. No se puede deshacer.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Sí, Cancelar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Volver", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun CompactAppointmentRow(app: Appointment, onClick: (Appointment) -> Unit) {
    val color = when (app.status) {
        "Completada" -> Color(0xFF10B981)
        "Cancelada" -> Color(0xFFEF4444)
        else -> Color(0xFFDC2626)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(app) },
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(app.startTime.take(5), fontWeight = FontWeight.Black, color = color, fontSize = 14.sp)
                Text(app.customer?.nombre ?: "Sin nombre", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(
                app.status.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
fun DetailPanelMobile(selectedAppointment: Appointment?, onCancelClick: () -> Unit, onCompleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (selectedAppointment == null) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.TouchApp, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("Ninguna cita seleccionada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Haz clic en una cita para ver detalles", fontSize = 12.sp, color = Color.LightGray)
            }
        } else {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("DETALLE DE RESERVACIÓN", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(56.dp).background(Color(0xFFF1F5F9), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    }
                    Column {
                        Text(selectedAppointment.customer?.nombre ?: "Cliente", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(selectedAppointment.customer?.telefono ?: "Sin teléfono", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                DetailRowMobile(Icons.Default.Event, "Fecha y Hora", "${selectedAppointment.date} • ${selectedAppointment.startTime.take(5)}")
                DetailRowMobile(Icons.Default.ContentCut, "Servicio", selectedAppointment.service?.nombre ?: "Servicio")
                DetailRowMobile(Icons.Default.Face, "Barbero Asignado", selectedAppointment.barber?.nombreCompleto ?: "Staff")

                if (selectedAppointment.status != "Cancelada" && selectedAppointment.status != "Completada") {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onCompleteClick,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("TERMINAR", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = if(selectedAppointment.status == "Completada") Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            selectedAppointment.status.uppercase(),
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black,
                            color = if(selectedAppointment.status == "Completada") Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowMobile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
        Column {
            Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TodayPendingCard(todayAppointments: List<Appointment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("PENDIENTES PARA HOY", fontWeight = FontWeight.Black, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            
            if (todayAppointments.isEmpty()) {
                Text("No hay tareas pendientes", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            } else {
                todayAppointments.take(4).forEach { app ->
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(if(app.status == "En Progreso") Color(0xFFFFBF00) else Color(0xFF10B981), CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(app.customer?.nombre ?: "Cliente", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${app.startTime.take(5)} • ${app.service?.nombre}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
