package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.admin.ExpressCard
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaCitaView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFD32F2F)

    var step by remember { mutableIntStateOf(1) }
    
    // Selection State
    var selectedBarber by remember { mutableStateOf<Barber?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    // Data State
    var barbers by remember { mutableStateOf<List<Barber>>(emptyList()) }
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            barbers = Greeting().getBarbers()
            services = Greeting().getServices()
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
                StepCircle(1, "Barbero", step >= 1)
                HorizontalDivider(Modifier.weight(1f).padding(top = 16.dp), color = if(step > 1) primaryColor else Color.LightGray)
                StepCircle(2, "Servicio", step >= 2)
                HorizontalDivider(Modifier.weight(1f).padding(top = 16.dp), color = if(step > 2) primaryColor else Color.LightGray)
                StepCircle(3, "Fecha", step >= 3)
            }

            Spacer(Modifier.height(32.dp))

            when(step) {
                1 -> { // Selection Barbero
                    ExpressCard("Selecciona a tu Maestro", "Elige al profesional que te atenderá.") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            barbers.forEach { barber ->
                                BarberSelectCard(barber, selectedBarber?.id == barber.id) { selectedBarber = barber }
                            }
                        }
                    }
                }
                2 -> { // Selection Servicio
                    ExpressCard("¿Qué servicio prefieres?", "Define tu nuevo look.") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            services.forEach { service ->
                                ServiceSelectCard(service, selectedService?.id == service.id) { selectedService = service }
                            }
                        }
                    }
                }
                3 -> { // Date & Time
                    ExpressCard("Día y Hora", "Selecciona el momento ideal.") {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Simplified Date/Time selection for the exercise
                            OutlinedTextField(
                                value = selectedDate,
                                onValueChange = { selectedDate = it },
                                label = { Text("Fecha (AAAA-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = selectedTime,
                                onValueChange = { selectedTime = it },
                                label = { Text("Hora (HH:MM)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
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
                            // Enviar al servidor
                            coroutineScope.launch {
                                isLoading = true
                                val res = Greeting().createBooking(
                                    GhostAppointmentRequest(
                                        barberId = selectedBarber!!.id!!,
                                        paymentMethod = "App",
                                        cartItems = listOf(CartItem(selectedService!!.id!!, "service", selectedService!!.nombre, selectedService!!.precio ?: 0.0)),
                                        amountReceived = selectedService!!.precio ?: 0.0,
                                        ghostName = null // It's a logged user
                                    )
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
                        1 -> selectedBarber != null
                        2 -> selectedService != null
                        else -> selectedDate.isNotBlank() && selectedTime.isNotBlank()
                    }
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (step < 3) "SIGUIENTE" else "CONFIRMAR", fontWeight = FontWeight.Black)
                }
            }
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun StepCircle(num: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(32.dp).background(if(isActive) Color(0xFFD32F2F) else Color.LightGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(num.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isActive) Color.Black else Color.Gray)
    }
}

@Composable
fun BarberSelectCard(barber: Barber, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        color = if(isSelected) Color(0xFFD32F2F).copy(alpha = 0.05f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFD32F2F) else Color(0xFFE2E8F0))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color.LightGray, RoundedCornerShape(20.dp)))
            Spacer(Modifier.width(12.dp))
            Text(barber.nombreCompleto, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if(isSelected) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFD32F2F))
        }
    }
}

@Composable
fun ServiceSelectCard(service: Service, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        color = if(isSelected) Color(0xFFD32F2F).copy(alpha = 0.05f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFD32F2F) else Color(0xFFE2E8F0))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!service.descripcion.isNullOrBlank()) {
                    Text(service.descripcion, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text("${service.duracion} min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F).copy(alpha = 0.7f))
            }
            Spacer(Modifier.width(8.dp))
            Text("$${service.precio}", fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), fontSize = 18.sp)
        }
    }
}
