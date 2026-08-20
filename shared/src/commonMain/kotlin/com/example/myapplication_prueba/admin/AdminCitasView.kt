package com.example.myapplication_prueba.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.Appointment
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import com.example.myapplication_prueba.format
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@Composable
fun AdminCitasView(onNavigateToPOS: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFDC2626)
    
    var selectedDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var dailyAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var pendingToday by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val dateStr = selectedDate.toString() 
                dailyAppointments = Greeting().getDailyAppointments(dateStr)
                dailyAppointments.forEach { app ->
                    println("DEBUG: Cita ID ${app.id} tiene estado: ${app.status}")
                }
                pendingToday = Greeting().getPendingAppointmentsToday()
            } catch (e: Exception) {
                println("DEBUG: Error al refrescar citas: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedDate) { refreshData() }

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
            // 1. Calendar View
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedDate.month.name} ${selectedDate.year}".uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        weekDays.forEach { day ->
                            val isSelected = day == selectedDate
                            val isToday = day == Clock.System.todayIn(TimeZone.currentSystemDefault())
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.8f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) primaryColor else if (isToday) primaryColor.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { selectedDate = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.dayOfMonth.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = if (isSelected) Color.White else if (isToday) primaryColor else Color.Black
                                    )
                                    if (isToday) {
                                        Text("HOY", fontSize = 8.sp, fontWeight = FontWeight.Black, color = if(isSelected) Color.White else primaryColor)
                                    }
                                }
                            }
                        }
                    }

                    if (dailyAppointments.isEmpty() && !isLoading) {
                        Text(
                            "No hay citas para este día",
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        dailyAppointments.forEach { app ->
                            val clientName = app.customer?.nombre ?: app.clienteNombre ?: "Walk-in"
                            AppointmentItem(app, clientName, isSelected = selectedAppointment?.id == app.id) {
                                selectedAppointment = app
                            }
                        }
                    }
                }
            }

            // 2. Quick Action Button (Ocasional)
            Button(
                onClick = onNavigateToPOS,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.PointOfSale, null)
                    Text("SERVICIO OCASIONAL", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            // 3. Detail Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                if (selectedAppointment == null) {
                    Column(
                        modifier = Modifier.padding(40.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TouchApp, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Ninguna cita seleccionada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Haz clic en una cita para ver detalles", fontSize = 12.sp, color = Color.LightGray)
                    }
                } else {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("DETALLE DE LA CITA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                            StatusBadgeSmall(selectedAppointment!!.status)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.size(50.dp).background(Color(0xFFF1F5F9), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray)
                            }
                            Column {
                                val clientName = selectedAppointment!!.customer?.nombre ?: selectedAppointment!!.clienteNombre ?: "Cliente Ocasional"
                                Text(clientName, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                val serviceText = selectedAppointment!!.serviceName ?: selectedAppointment!!.service?.nombre ?: "Servicio"
                                Text(selectedAppointment!!.startTime.take(5) + " - " + serviceText, color = Color.Gray, fontSize = 14.sp)
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        DetailRow(Icons.Default.Face, "Barbero", selectedAppointment!!.barber?.nombreCompleto ?: "Asignado")
                        DetailRow(Icons.Default.Payments, "Total a Cobrar", "$${selectedAppointment!!.totalPrice.format(2)} (${selectedAppointment!!.paymentMethod ?: "N/A"})")

                        if (selectedAppointment!!.status == "Programada") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            println("DEBUG: Intentando completar cita ID: ${selectedAppointment!!.id}")
                                            val res = Greeting().updateCitaEstado(selectedAppointment!!.id, "Completada")
                                            println("DEBUG: Respuesta completar recibida: ${res.success} - ${res.message}")
                                            
                                            if (res.success) {
                                                toastMessage = "Cita completada"; toastType = ToastType.SUCCESS
                                                selectedAppointment = null
                                                // Esperamos un momento para que el servidor termine de persistir
                                                kotlinx.coroutines.delay(500)
                                                refreshData()
                                            } else {
                                                toastMessage = res.message; toastType = ToastType.ERROR
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("COMPLETAR", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                
                                OutlinedButton(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("CANCELAR", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp) }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { showRescheduleDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("REPROGRAMAR", fontWeight = FontWeight.Bold) }
                        } else {
                            // Si ya está completada o cancelada, mostramos un aviso y bloqueamos acciones
                            val finalColor = if(selectedAppointment!!.status == "Completada") Color(0xFF10B981) else Color(0xFFEF4444)
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                color = finalColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, finalColor.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        if(selectedAppointment!!.status == "Completada") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        null,
                                        tint = finalColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "CITA ${selectedAppointment!!.status.uppercase()}",
                                        fontWeight = FontWeight.Black,
                                        color = finalColor,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Today Pending List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("PENDIENTES PARA HOY", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(16.dp))
                    
                    if (pendingToday.isEmpty()) {
                        Text("No hay tareas pendientes", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    } else {
                        pendingToday.forEach { app ->
                            val clientName = app.customer?.nombre ?: app.clienteNombre ?: "Cliente"
                            val serviceText = app.serviceName ?: app.service?.nombre ?: "Servicio"
                            Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(primaryColor, CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${app.startTime.take(5)} • $serviceText", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Dialogs
        if (showCancelDialog && selectedAppointment != null) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("¿Cancelar Cita?", fontWeight = FontWeight.Black) },
                text = { Text("Esta acción liberará el espacio en la agenda. No se puede deshacer.") },
                confirmButton = {
                    Button(onClick = {
                        coroutineScope.launch {
                            println("DEBUG: Intentando cancelar cita ID: ${selectedAppointment!!.id}")
                            val res = Greeting().updateCitaEstado(selectedAppointment!!.id, "Cancelada")
                            println("DEBUG: Respuesta cancelar recibida: ${res.success} - ${res.message}")
                            
                            if (res.success) {
                                toastMessage = "Cita cancelada"; toastType = ToastType.SUCCESS
                                selectedAppointment = null
                                // Pequeña pausa de seguridad antes del refresco
                                kotlinx.coroutines.delay(500)
                                refreshData()
                            } else {
                                toastMessage = res.message; toastType = ToastType.ERROR
                            }
                        }
                        showCancelDialog = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("Sí, Cancelar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("Volver", color = Color.Gray) } },
                shape = RoundedCornerShape(24.dp), containerColor = Color.White
            )
        }
        
        if (showRescheduleDialog && selectedAppointment != null) {
            RescheduleDialog(
                appointment = selectedAppointment!!,
                onDismiss = { showRescheduleDialog = false },
                onConfirm = { date, time ->
                    coroutineScope.launch {
                        val res = Greeting().rescheduleAppointment(selectedAppointment!!.id, date, time)
                        if (res.success) {
                            toastMessage = "Cita reprogramada"; toastType = ToastType.SUCCESS
                            selectedAppointment = null; refreshData()
                        } else { toastMessage = res.message; toastType = ToastType.ERROR }
                    }
                    showRescheduleDialog = false
                }
            )
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun AppointmentItem(app: Appointment, clientName: String, isSelected: Boolean, onClick: () -> Unit) {
    val statusColor = when(app.status) {
        "Completada" -> Color(0xFF10B981)
        "Cancelada" -> Color(0xFFEF4444)
        else -> Color(0xFFDC2626)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        color = if(isSelected) statusColor.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(16.dp),
        border = if(isSelected) BorderStroke(2.dp, statusColor) else BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.startTime.take(5), fontWeight = FontWeight.Black, fontSize = 16.sp, color = statusColor)
                Text(clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
            }
            StatusBadgeSmall(app.status)
        }
    }
}

@Composable
fun StatusBadgeSmall(status: String) {
    val color = when(status) {
        "Completada" -> Color(0xFF10B981)
        "Cancelada" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
        Column {
            Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RescheduleDialog(appointment: Appointment, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var newDate by remember { mutableStateOf(appointment.date) }
    var newTime by remember { mutableStateOf("") }
    var availableTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(newDate) {
        coroutineScope.launch {
            availableTimes = Greeting().getAvailableTimes(newDate, appointment.duracion)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reprogramar Cita", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Selector de Fecha
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(newDate, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray)
                    }
                }

                Text("HORARIOS DISPONIBLES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                
                // Grid de Horas (Cuadritos)
                Box(modifier = Modifier.heightIn(max = 200.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableTimes.chunked(3).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { time ->
                                    val isSelected = newTime == time
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { newTime = time },
                                        color = if (isSelected) Color(0xFFDC2626) else Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if(isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0))
                                    ) {
                                        Text(time, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, color = if(isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                        if (availableTimes.isEmpty()) {
                            Text("No hay horarios para esta fecha", color = Color.Red, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newDate, newTime) }, enabled = newTime.isNotEmpty()) {
                Text("Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )

    if (showDatePicker) {
        com.example.myapplication_prueba.cliente.DatePickerModal(
            onDateSelected = { it?.let { newDate = com.example.myapplication_prueba.cliente.formatMillisToDate(it) } },
            onDismiss = { showDatePicker = false }
        )
    }
}
