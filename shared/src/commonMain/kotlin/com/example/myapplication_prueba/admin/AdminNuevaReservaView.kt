package com.example.myapplication_prueba.admin

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNuevaReservaView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Form State
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    var ghostName by remember { mutableStateOf("") }
    var ghostPhone by remember { mutableStateOf("") }
    var isRegularCustomer by remember { mutableStateOf(true) }
    
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }
    
    var selectedDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var selectedTime by remember { mutableStateOf("") }
    var selectedBarberId by remember { mutableStateOf<Int?>(null) }
    var paymentMethod by remember { mutableStateOf("EFECTIVO") }
    
    // Data State
    var customers by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var promotions by remember { mutableStateOf<List<Promotion>>(emptyList()) }
    var availableTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableBarbers by remember { mutableStateOf<List<AvailableBarber>>(emptyList()) }
    
    var customerSearch by remember { mutableStateOf("") }
    var serviceSearch by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingBarbers by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            customers = Greeting().getCustomers()
            services = Greeting().getServices()
            promotions = Greeting().getPromotions()
            isLoading = false
        }
    }

    fun updateTimes() {
        if (selectedService == null && selectedPromotion == null) {
            availableTimes = emptyList()
            return
        }
        val duration = selectedService?.duracion ?: (if(selectedPromotion != null) 30 else 0)
        coroutineScope.launch {
            availableTimes = Greeting().getAvailableTimes(
                selectedDate, 
                duration
            )
            // Solo reseteamos si el tiempo seleccionado ya no es válido
            if (!availableTimes.contains(selectedTime)) {
                selectedTime = ""
                selectedBarberId = null
            }
        }
    }

    fun updateBarbers() {
        if (selectedTime.isBlank()) {
            availableBarbers = emptyList()
            return
        }
        val duration = selectedService?.duracion ?: (if(selectedPromotion != null) 30 else 0)
        coroutineScope.launch {
            isLoadingBarbers = true
            availableBarbers = Greeting().getAvailableBarbers(
                selectedDate,
                selectedTime,
                duration
            )
            isLoadingBarbers = false
            // Reset barber if not in new list
            if (availableBarbers.none { it.id == selectedBarberId }) {
                selectedBarberId = null
            }
        }
    }

    LaunchedEffect(Unit) { refreshData() }
    
    LaunchedEffect(selectedDate, selectedService, selectedPromotion) {
        updateTimes()
    }
    
    LaunchedEffect(selectedTime, selectedDate, selectedService, selectedPromotion) {
        updateBarbers()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Nueva Cita", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Reserva manual de administrador", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // 1. SELECCIÓN DE CLIENTE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. Cliente", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))
                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                val activeTabColor = Color(0xFFDC2626)
                                Button(
                                    onClick = { isRegularCustomer = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRegularCustomer) Color.White else Color.Transparent,
                                        contentColor = if (isRegularCustomer) Color.Black else Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("Registrado", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                Button(
                                    onClick = { isRegularCustomer = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isRegularCustomer) Color.White else Color.Transparent,
                                        contentColor = if (!isRegularCustomer) Color.Black else Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("Ocasional", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (isRegularCustomer) {
                        OutlinedTextField(
                            value = customerSearch,
                            onValueChange = { customerSearch = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar cliente...", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.heightIn(max = 160.dp)) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                customers.filter { it.nombre.contains(customerSearch, true) || it.telefono.contains(customerSearch) }.forEach { customer ->
                                    val isSelected = selectedCustomerId == customer.id
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().clickable { selectedCustomerId = customer.id },
                                        color = if (isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0))
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isSelected) Color(0xFFDC2626) else Color.LightGray), contentAlignment = Alignment.Center) {
                                                Text(customer.nombre.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text("${customer.nombre} ${customer.apellido}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(customer.telefono, fontSize = 11.sp, color = Color.Gray)
                                            }
                                            if (isSelected) {
                                                Spacer(Modifier.weight(1f))
                                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FormTextField(label = "Nombre del Cliente (Walk-in)", value = ghostName, onValueChange = { ghostName = it }, placeholder = "Ej. Juan Perez", isBlack = true)
                            FormTextField(label = "Teléfono de Contacto", value = ghostPhone, onValueChange = { 
                                if(it.length <= 10 && it.all { char -> char.isDigit() }) ghostPhone = it 
                            }, placeholder = "0000000000", keyboardType = KeyboardType.Phone, isBlack = true)
                        }
                    }
                }
            }

            // 2. SELECCIÓN DE SERVICIO / PROMO
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("2. Servicio / Promoción", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = serviceSearch,
                        onValueChange = { serviceSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar...", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Promos
                            promotions.filter { it.nombre.contains(serviceSearch, true) }.forEach { promo ->
                                val isSelected = selectedPromotion?.id == promo.id
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedPromotion = promo; selectedService = null; selectedTime = ""; selectedBarberId = null },
                                    color = if (isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFDC2626) else Color.Transparent)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(color = Color(0xFFDC2626), shape = CircleShape) {
                                                Text("PROMO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }
                                            Text(promo.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Text("$${promo.precioPromocional}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                            // Services
                            services.filter { it.nombre.contains(serviceSearch, true) }.forEach { service ->
                                val isSelected = selectedService?.id == service.id
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedService = service; selectedPromotion = null; selectedTime = ""; selectedBarberId = null },
                                    color = if (isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if(isSelected) 2.dp else 1.dp, if(isSelected) Color(0xFFDC2626) else Color.Transparent)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(service.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${service.duracion} MIN", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Text("$${service.precio}", fontWeight = FontWeight.Black, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. FECHA Y HORA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("3. Fecha y Hora", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                    Spacer(Modifier.height(20.dp))
                    
                    // Fecha
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(selectedDate, color = Color.White, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.EditCalendar, null, tint = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    
                    // Horas (10:00 - 20:00 cada 30 min)
                    if (selectedService != null || selectedPromotion != null) {
                        availableTimes.chunked(3).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { time ->
                                    val isSelected = selectedTime == time
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { selectedTime = time; selectedBarberId = null },
                                        color = if (isSelected) Color(0xFFDC2626) else Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if(isSelected) Color(0xFFDC2626) else Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Text(time, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, color = if(isSelected) Color.White else Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (availableTimes.isEmpty() && !isLoading) {
                            Text("No hay horarios disponibles", color = Color(0xFFEF4444), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        Text("Seleccione un servicio primero", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // 4. SELECCIÓN DE BARBERO (Solo aparece tras elegir la hora)
            if (selectedTime.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("4. Barbero Disponible", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            if (isLoadingBarbers) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFDC2626))
                        }
                        Spacer(Modifier.height(20.dp))
                        if (availableBarbers.isEmpty() && !isLoadingBarbers) {
                            Text("Sin barberos para este horario/duración.", color = Color(0xFFEF4444), fontSize = 12.sp)
                        } else {
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                availableBarbers.forEach { barber ->
                                    val isSelected = selectedBarberId == barber.id
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(80.dp).clickable { selectedBarberId = barber.id }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFFDC2626).copy(alpha = 0.1f) else Color(0xFFF1F5F9))
                                                .border(2.dp, if (isSelected) Color(0xFFDC2626) else Color.Transparent, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!barber.imagenUrl.isNullOrBlank()) {
                                                KamelImage(resource = asyncPainterResource(barber.imagenUrl!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            } else {
                                                Icon(Icons.Default.Person, null, tint = if(isSelected) Color(0xFFDC2626) else Color.LightGray)
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(barber.nombre, fontSize = 11.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium, color = if(isSelected) Color(0xFFDC2626) else Color.Black, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. PAGO Y CONFIRMACIÓN
            val canConfirm = (if(isRegularCustomer) selectedCustomerId != null else ghostName.isNotBlank()) &&
                             (selectedService != null || selectedPromotion != null) &&
                             selectedTime.isNotEmpty() && selectedBarberId != null

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("5. Confirmación", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentCard(Modifier.weight(1f), "EFECTIVO", Icons.Default.Payments, paymentMethod == "EFECTIVO") { paymentMethod = "EFECTIVO" }
                        PaymentCard(Modifier.weight(1f), "TARJETA", Icons.Default.CreditCard, paymentMethod == "TARJETA") { paymentMethod = "TARJETA" }
                    }
                    Spacer(Modifier.height(24.dp))
                    val totalPrice = selectedService?.precio ?: selectedPromotion?.precioPromocional ?: 0.0
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("TOTAL A COBRAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("$${totalPrice.format(2)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val duration = selectedService?.duracion ?: (if(selectedPromotion != null) 30 else 0)
                                val serviceName = selectedService?.nombre ?: selectedPromotion?.nombre ?: ""
                                
                                val res = Greeting().createAdminAppointment(
                                    AdminAppointmentRequest(
                                        usuarioId = if(isRegularCustomer) selectedCustomerId else null,
                                        esOcasional = !isRegularCustomer,
                                        clienteNombre = if(!isRegularCustomer) ghostName else null,
                                        clienteTelefono = if(!isRegularCustomer) ghostPhone else null,
                                        barberoId = selectedBarberId!!,
                                        servicioNombre = serviceName,
                                        fecha = selectedDate,
                                        horaInicio = selectedTime,
                                        duracion = duration,
                                        precio = totalPrice,
                                        metodoPago = paymentMethod
                                    )
                                )
                                isLoading = false
                                if (res.success) {
                                    toastMessage = "Cita confirmada"; toastType = ToastType.SUCCESS
                                    kotlinx.coroutines.delay(1500); onBack()
                                } else { toastMessage = res.message; toastType = ToastType.ERROR }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && canConfirm
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("CONFIRMAR Y AGENDAR", fontWeight = FontWeight.Black)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        if (showDatePicker) {
            com.example.myapplication_prueba.cliente.DatePickerModal(
                onDateSelected = { it?.let { selectedDate = com.example.myapplication_prueba.cliente.formatMillisToDate(it) } },
                onDismiss = { showDatePicker = false }
            )
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun PaymentCard(modifier: Modifier, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if(isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, if(isSelected) Color(0xFFDC2626) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if(isSelected) Color(0xFFDC2626) else Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = if(isSelected) Color(0xFFDC2626) else Color.Gray)
        }
    }
}
