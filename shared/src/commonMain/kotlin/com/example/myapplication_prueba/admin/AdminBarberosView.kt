package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@Composable
fun AdminBarberosView(
    onNavigateToNew: () -> Unit,
    onNavigateToEdit: (Barber) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var barbers by remember { mutableStateOf<List<Barber>>(emptyList()) }
    var stats by remember { mutableStateOf(BarberStats(0, 0, 0)) }
    var isLoading by remember { mutableStateOf(true) }
    
    var selectedBarberForSchedule by remember { mutableStateOf<Barber?>(null) }
    var barberToDelete by remember { mutableStateOf<Barber?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            barbers = Greeting().getBarbers()
            stats = Greeting().getBarberStats()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Equipo de Trabajo", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color(0xFF0F172A))
                    Text("Gestión de profesionales", fontSize = 14.sp, color = Color.Gray)
                }
                Button(
                    onClick = onNavigateToNew,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo", fontWeight = FontWeight.Bold)
                }
            }

            // Stats
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(Modifier.weight(1f), "TOTAL", stats.totalBarbers.toString(), Icons.Default.Group, Color(0xFF64748B))
                StatItem(Modifier.weight(1f), "ACTIVOS", stats.activeBarbers.toString(), Icons.Default.CheckCircle, Color(0xFF10B981))
                StatItem(Modifier.weight(1f), "INACTIVOS", stats.offBarbers.toString(), Icons.Default.Cancel, Color(0xFFEF4444))
            }

            Divider(color = Color(0xFFE2E8F0))

            // Barbers List
            if (barbers.isEmpty() && !isLoading) {
                EmptyBarbersPlaceholder()
            } else {
                barbers.forEach { barber ->
                    BarberProfessionalCard(
                        barber = barber,
                        onManageSchedule = { selectedBarberForSchedule = it },
                        onEdit = onNavigateToEdit,
                        onDelete = { barberToDelete = it },
                        onToggleStatus = { active ->
                            coroutineScope.launch {
                                val res = Greeting().updateBarber(
                                    barber.copy(activo = active),
                                    imageBytes = null,
                                    password = null
                                )
                                if (res.success) refreshData()
                                else { toastMessage = res.message; toastType = ToastType.ERROR }
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (selectedBarberForSchedule != null) {
            ScheduleModal(
                barber = selectedBarberForSchedule!!,
                onDismiss = { selectedBarberForSchedule = null },
                onSave = { config ->
                    coroutineScope.launch {
                        val res = Greeting().updateBarberSchedule(selectedBarberForSchedule!!.id!!, config)
                        if (res.success) {
                            toastMessage = "Horario actualizado"
                            toastType = ToastType.SUCCESS
                            refreshData()
                        } else {
                            toastMessage = "Error al guardar"
                            toastType = ToastType.ERROR
                        }
                        selectedBarberForSchedule = null
                    }
                }
            )
        }

        if (barberToDelete != null) {
            ConfirmDialog(
                title = "DESPEDIR BARBERO",
                text = "¿Estás seguro de que deseas eliminar permanentemente a ${barberToDelete!!.nombreCompleto}? Esta acción no se puede deshacer.",
                confirmText = "Despedir",
                confirmColor = Color(0xFFEF4444),
                onConfirm = {
                    coroutineScope.launch {
                        barberToDelete?.id?.let { id ->
                            val res = Greeting().deleteBarber(id)
                            if (res.success) {
                                toastMessage = "Barbero eliminado"
                                toastType = ToastType.SUCCESS
                                refreshData()
                            } else {
                                toastMessage = res.message
                                toastType = ToastType.ERROR
                            }
                        }
                        barberToDelete = null
                    }
                },
                onDismiss = { barberToDelete = null }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFDC2626))
            }
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}

@Composable
fun StatItem(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Icon(icon, null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
        }
    }
}

@Composable
fun BarberProfessionalCard(
    barber: Barber, 
    onManageSchedule: (Barber) -> Unit, 
    onEdit: (Barber) -> Unit, 
    onDelete: (Barber) -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Profile Image / Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!barber.imagenUrl.isNullOrBlank()) {
                        KamelImage(
                            resource = asyncPainterResource(barber.imagenUrl!!),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) } },
                            onFailure = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(40.dp)) } }
                        )
                    } else {
                        Text(barber.nombreCompleto.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFCBD5E1))
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(barber.nombreCompleto, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))
                            StatusBadge(barber.activo)
                        }
                        
                        IconButton(
                            onClick = { onDelete(barber) },
                            modifier = Modifier.size(32.dp).background(Color(0xFFFEF2F2), CircleShape)
                        ) {
                            Icon(Icons.Default.PersonRemove, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = if(barber.specialties.isEmpty()) "Sin especialidades" else barber.specialties.joinToString(", "),
                        color = Color.Gray, fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ContactInfo(Icons.Default.Phone, barber.telefono ?: "--")
                        ContactInfo(Icons.Default.Email, barber.email ?: "--")
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (barber.activo) {
                    Button(
                        onClick = { onManageSchedule(barber) },
                        modifier = Modifier.weight(1.2f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Horario", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onEdit(barber) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                } else {
                    Button(
                        onClick = { onToggleStatus(true) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Activar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(active: Boolean) {
    Surface(
        color = if (active) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
        shape = CircleShape
    ) {
        Text(
            text = if (active) "ACTIVO" else "INACTIVO",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = if (active) Color(0xFF065F46) else Color(0xFF991B1B)
        )
    }
}

@Composable
fun ContactInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = Color(0xFFDC2626), modifier = Modifier.size(10.dp))
        Text(text, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleModal(barber: Barber, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var selectedSlots by remember { mutableStateOf(barber.scheduleConfiguration?.split(",")?.toSet() ?: emptySet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(selectedSlots.joinToString(",")) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                Text("Guardar Horario", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        },
        title = { Text("Horario de ${barber.nombreCompleto}", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                val days = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
                val hours = (10..20).map { "${it.toString().padStart(2, '0')}:00" }
                
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9))) {
                    Spacer(Modifier.width(50.dp))
                    days.forEach { day ->
                        Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                LazyColumn {
                    items(hours) { hour ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(hour, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                            (1..7).forEach { dayIndex ->
                                val day = if(dayIndex == 7) 0 else dayIndex // Sunday is 0 in backend instructions
                                val slot = "$day-$hour"
                                val isSelected = selectedSlots.contains(slot)
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Color(0xFFDC2626) else Color(0xFFF1F5F9))
                                        .clickable {
                                            selectedSlots = if (isSelected) selectedSlots - slot else selectedSlots + slot
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if(isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EmptyBarbersPlaceholder() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Group, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
        Text("No hay profesionales registrados", color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}
