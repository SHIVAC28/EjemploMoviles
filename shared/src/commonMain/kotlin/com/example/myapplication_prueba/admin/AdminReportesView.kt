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
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportesView() {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // States
    var reportStats by remember { mutableStateOf(ReportStats(0, 0.0)) }
    var filteredApps by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var barbers by remember { mutableStateOf<List<Barber>>(emptyList()) }
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Filters
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(today.toString()) }
    var endDate by remember { mutableStateOf(today.toString()) }
    var selectedBarberId by remember { mutableStateOf<Int?>(null) }
    var selectedServiceId by remember { mutableStateOf<Int?>(null) }

    // Modals
    var selectedAppForDetails by remember { mutableStateOf<Appointment?>(null) }
    var saleDetails by remember { mutableStateOf<List<SaleDetail>>(emptyList()) }
    var showSoldProductsModal by remember { mutableStateOf(false) }
    var soldProducts by remember { mutableStateOf<List<SoldProduct>>(emptyList()) }

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            reportStats = Greeting().getReportStats(startDate, endDate, selectedBarberId, selectedServiceId)
            filteredApps = Greeting().getFilteredAppointments(startDate, endDate, selectedBarberId, selectedServiceId)
            barbers = Greeting().getBarbers()
            services = Greeting().getServices()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { 
                        coroutineScope.launch {
                            soldProducts = Greeting().getSoldProducts(startDate, endDate)
                            showSoldProductsModal = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Productos")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { /* Cash Register */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cierre")
                }
            }

            // Stats Grid (Bento)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Total Appointments
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("TOTAL CITAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(reportStats.totalApps.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    // Revenue
                    Card(modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color(0xFFDC2626).copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("INGRESOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("$${reportStats.totalIncome.format(0)}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                        }
                    }
                }

                // Top Barber Card
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WorkspacePremium, null, tint = Color(0xFFDC2626), modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BARBERO TOP", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                            Text(reportStats.topBarberName ?: "Sin datos", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("${reportStats.topBarberCount} citas realizadas", color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Advanced Filters
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("FILTROS AVANZADOS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Inicio") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fin") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }

                    Button(
                        onClick = { loadData() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("APLICAR FILTROS")
                    }
                }
            }

            // Detailed List
            Column {
                Text("LISTADO DETALLADO", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F172A))
                Spacer(Modifier.height(12.dp))

                if (filteredApps.isEmpty() && !isLoading) {
                    EmptyPlaceholder("No hay resultados", Icons.Default.ReceiptLong)
                } else {
                    filteredApps.forEach { app ->
                        ReportAppointmentCard(
                            app = app,
                            onViewDetails = { 
                                coroutineScope.launch {
                                    saleDetails = Greeting().getSaleDetails(app.id)
                                    selectedAppForDetails = it
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Modals
        if (selectedAppForDetails != null) {
            ReportDetailsModal(
                app = selectedAppForDetails!!,
                details = saleDetails,
                onDismiss = { selectedAppForDetails = null }
            )
        }

        if (showSoldProductsModal) {
            SoldProductsModal(
                products = soldProducts,
                onDismiss = { showSoldProductsModal = false }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
fun ReportAppointmentCard(app: Appointment, onViewDetails: (Appointment) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onViewDetails(app) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                Text(app.customer?.nombre?.take(1)?.uppercase() ?: "?", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.customer?.nombre ?: "Cliente", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${app.date} • ${app.startTime.take(5)}", fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${app.totalPrice}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFDC2626))
                StatusBadge(app.status == "Completada")
            }
        }
    }
}

@Composable
fun ReportDetailsModal(app: Appointment, details: List<SaleDetail>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text("DETALLE DEL SERVICIO", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailChip("Cliente", app.customer?.nombre ?: "--", Modifier.weight(1f))
                    DetailChip("Fecha", app.date, Modifier.weight(1f))
                }
                Text("ARTÍCULOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Column(modifier = Modifier.background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(8.dp)) {
                    details.forEach { 
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(it.price, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("TOTAL", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("$${app.totalPrice}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFFDC2626))
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SoldProductsModal(products: List<SoldProduct>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text("PRODUCTOS VENDIDOS", fontWeight = FontWeight.Black) },
        text = {
            if (products.isEmpty()) {
                Text("No hay productos vendidos en este rango.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products) { sp ->
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${sp.quantity} un. • ${sp.date}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text("$${sp.price}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
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
fun DetailChip(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(8.dp)) {
        Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun Double.format(digits: Int): String {
    val s = this.toString()
    if (!s.contains(".")) return s
    val parts = s.split(".")
    val decimal = parts[1].padEnd(digits, '0').take(digits)
    return if (digits > 0) "${parts[0]}.$decimal" else parts[0]
}
