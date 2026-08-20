package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.admin.ExpressCard
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaCitaView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFDC2626)

    var step by remember { mutableIntStateOf(1) }
    
    // Selection State
    var selectedBarber by remember { mutableStateOf<Barber?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    // Data State
    var barbers by remember { mutableStateOf<List<Barber>>(emptyList()) }
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var promotions by remember { mutableStateOf<List<Promotion>>(emptyList()) }
    var availableTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            barbers = Greeting().getBarbers()
            services = Greeting().getServices()
            promotions = Greeting().getPromotions()
        }
    }

    // Actualizar horarios disponibles cuando cambia fecha o barbero o servicio
    LaunchedEffect(selectedDate, selectedBarber, selectedService, selectedPromotion) {
        if (selectedDate.isNotEmpty() && selectedBarber != null) {
            coroutineScope.launch {
                availableTimes = Greeting().getBarberAvailability(selectedBarber!!.id!!, selectedDate)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Gray) }
                Text("AGENDAR CITA", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Step Indicator
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StepCircle(1, "Servicio", step >= 1)
                HorizontalDivider(Modifier.weight(1f).padding(top = 16.dp), color = if(step > 1) primaryColor else Color.LightGray)
                StepCircle(2, "Barbero", step >= 2)
                HorizontalDivider(Modifier.weight(1f).padding(top = 16.dp), color = if(step > 2) primaryColor else Color.LightGray)
                StepCircle(3, "Fecha", step >= 3)
            }

            Spacer(Modifier.height(32.dp))

            when(step) {
                1 -> { // Selection Servicio o Promo
                    ExpressCard("¿Qué servicio prefieres?", "Define tu nuevo look.") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Promos
                            promotions.filter { it.activo }.forEach { promo ->
                                val isSelected = selectedPromotion?.id == promo.id
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { 
                                        selectedPromotion = promo
                                        selectedService = null
                                    },
                                    color = if(isSelected) primaryColor.copy(alpha = 0.05f) else Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) primaryColor else Color(0xFFE2E8F0))
                                ) {
                                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(Modifier.weight(1f)) {
                                            Surface(color = primaryColor, shape = CircleShape) {
                                                Text("PROMO", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }
                                            Text(promo.nombre, fontWeight = FontWeight.Bold)
                                            Text(promo.nombreServicios.joinToString(", "), fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text("$${promo.precioPromocional}", fontWeight = FontWeight.Black, color = primaryColor)
                                    }
                                }
                            }
                            // Individual Services
                            services.filter { it.activo }.forEach { service ->
                                ServiceSelectCard(service, selectedService?.id == service.id) { 
                                    selectedService = service 
                                    selectedPromotion = null
                                }
                            }
                        }
                    }
                }
                2 -> { // Selection Barbero
                    ExpressCard("Selecciona a tu Maestro", "Elige al profesional que te atenderá.") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            barbers.forEach { barber ->
                                BarberSelectCard(barber, selectedBarber?.id == barber.id) { selectedBarber = barber }
                            }
                        }
                    }
                }
                3 -> { // Date & Time
                    ExpressCard("Día y Hora", "Selecciona el momento ideal.") {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Fecha con Calendario
                            Text("FECHA DE CITA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(selectedDate.ifBlank { "Seleccionar Fecha" }, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray)
                                }
                            }

                            if (selectedDate.isNotEmpty()) {
                                Text("HORARIOS DISPONIBLES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                if (availableTimes.isEmpty()) {
                                    Text("No hay horarios libres para este barbero hoy.", color = primaryColor, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                } else {
                                    availableTimes.chunked(3).forEach { row ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            row.forEach { time ->
                                                val isSelected = selectedTime == time
                                                Surface(
                                                    modifier = Modifier.weight(1f).clickable { selectedTime = time },
                                                    color = if (isSelected) primaryColor else Color.White,
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, if(isSelected) primaryColor else Color(0xFFE2E8F0))
                                                ) {
                                                    Text(time, modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, color = if(isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Navigation
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }, Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp)) {
                        Text("ANTERIOR", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
                Button(
                    onClick = {
                        if (step < 3) step++
                        else {
                            coroutineScope.launch {
                                isLoading = true
                                val res = Greeting().createClientBooking(
                                    barberId = selectedBarber!!.id!!,
                                    serviceId = selectedService?.id,
                                    promotionId = selectedPromotion?.id,
                                    date = selectedDate,
                                    time = selectedTime
                                )
                                isLoading = false
                                if (res.success) {
                                    toastMessage = "Cita agendada con éxito"
                                    toastType = ToastType.SUCCESS
                                    kotlinx.coroutines.delay(1500)
                                    onBack()
                                } else {
                                    toastMessage = res.message
                                    toastType = ToastType.ERROR
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = when(step) {
                        1 -> selectedService != null || selectedPromotion != null
                        2 -> selectedBarber != null
                        else -> selectedDate.isNotBlank() && selectedTime.isNotBlank()
                    }
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (step < 3) "SIGUIENTE" else "CONFIRMAR", fontWeight = FontWeight.Black)
                }
            }
        }

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { it?.let { selectedDate = formatMillisToDate(it) } },
                onDismiss = { showDatePicker = false }
            )
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun BarberSelectCard(barber: Barber, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        color = if(isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!barber.imagenUrl.isNullOrBlank()) {
                    KamelImage(resource = asyncPainterResource(barber.imagenUrl!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else {
                    Text(barber.nombreCompleto.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(barber.nombreCompleto, fontWeight = FontWeight.Bold)
                Text(barber.specialties.joinToString(", "), fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            if(isSelected) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFDC2626))
        }
    }
}

@Composable
fun ServiceSelectCard(service: Service, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        color = if(isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!service.descripcion.isNullOrBlank()) {
                    Text(service.descripcion, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text("${service.duracion} min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626).copy(alpha = 0.7f))
            }
            Spacer(Modifier.width(8.dp))
            Text("$${service.precio}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626), fontSize = 18.sp)
        }
    }
}

@Composable
fun StepCircle(num: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(32.dp).background(if(isActive) Color(0xFFDC2626) else Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isActive) Color.Black else Color.Gray)
    }
}
