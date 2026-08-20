package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch

@Composable
fun VentaFantasmaView(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // --- ESTADOS DE SELECCIÓN ---
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerName by remember { mutableStateOf<String?>(null) }
    var isOcasional by remember { mutableStateOf(false) }
    var ocasionalName by remember { mutableStateOf("") }
    
    var selectedBarberId by remember { mutableStateOf<Int?>(null) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var amountReceived by remember { mutableStateOf("") }

    // --- ESTADOS DE DATOS ---
    var customers by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var barbers by remember { mutableStateOf<List<Barber>>(emptyList()) }
    var availableServices by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var availableProducts by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(true) }
    var showServiceModal by remember { mutableStateOf(false) }
    var showProductModal by remember { mutableStateOf(false) }
    var customerSearch by remember { mutableStateOf("") }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    // Cálculos
    val totalPagar = cartItems.sumOf { it.price }
    val change = if (amountReceived.isNotEmpty() && amountReceived.toDoubleOrNull() != null) {
        amountReceived.toDouble() - totalPagar
    } else 0.0

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            customers = Greeting().getCustomers()
            barbers = Greeting().getBarbers()
            
            // Transformar servicios y productos a CartItem
            availableServices = Greeting().getServices().map { 
                CartItem(it.id ?: 0, "service", it.nombre, it.precio ?: 0.0) 
            }
            availableProducts = Greeting().getProducts().map { 
                CartItem(it.id ?: 0, "product", it.nombre, it.precio) 
            }
            isLoading = false
        }
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
                IconButton(onClick = onBack, modifier = Modifier.background(Color(0xFF121212), RoundedCornerShape(12.dp)).size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Venta Express", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
                    Text("Caja rápida y walk-ins", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // 1. SELECCIÓN DE CLIENTE
            LightTerminalSection("1. SELECCIÓN DE CLIENTE") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Switch de tipo de cliente
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isOcasional = false },
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isOcasional) Color.White else Color.Transparent,
                                contentColor = if (!isOcasional) Color.Black else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = if (!isOcasional) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                        ) { Text("Base de Datos", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        
                        Button(
                            onClick = { isOcasional = true },
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOcasional) Color.White else Color.Transparent,
                                contentColor = if (isOcasional) Color.Black else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = if (isOcasional) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                        ) { Text("Ocasional", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }

                    if (!isOcasional) {
                        OutlinedTextField(
                            value = customerSearch,
                            onValueChange = { customerSearch = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar cliente...", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        val filteredCustomers = customers.filter { 
                            it.nombre.contains(customerSearch, true) || it.telefono.contains(customerSearch)
                        }

                        if (customerSearch.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    filteredCustomers.forEach { customer ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { 
                                                selectedCustomerId = customer.id
                                                selectedCustomerName = "${customer.nombre} ${customer.apellido}"
                                                customerSearch = ""
                                            }.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(modifier = Modifier.size(32.dp).background(Color(0xFFDC2626).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                                Text(customer.nombre.take(1), color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                            }
                                            Text("${customer.nombre} ${customer.apellido}", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedCustomerId != null) {
                            Surface(color = Color(0xFF10B981).copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFF10B981))) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                                        Text(selectedCustomerName ?: "", fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = { selectedCustomerId = null; selectedCustomerName = null }) {
                                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    } else {
                        LightTerminalTextField(ocasionalName, { ocasionalName = it }, "Nombre si es ocasional", Icons.Default.Person)
                    }
                }
            }

            // 2. ASIGNAR BARBERO
            LightTerminalSection("2. ASIGNAR BARBERO") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(barbers) { barber ->
                        BarberLightCard(barber, selectedBarberId == barber.id) { selectedBarberId = barber.id }
                    }
                }
            }

            // 3. SERVICIOS
            LightTerminalSection("3. SERVICIOS", {
                TextButton(onClick = { showServiceModal = true }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text("AGREGAR SERVICIO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }) {
                val services = cartItems.filter { it.type == "service" }
                if (services.isEmpty()) LightEmptyBox("Sin servicios seleccionados")
                else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        services.forEach { item ->
                            CartItemLightRow(item) { cartItems = cartItems - item }
                        }
                    }
                }
            }

            // 4. PRODUCTOS (OPCIONAL)
            LightTerminalSection("4. PRODUCTOS (OPCIONAL)", {
                TextButton(onClick = { showProductModal = true }) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(16.dp))
                    Text("AGREGAR PRODUCTO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }) {
                val prods = cartItems.filter { it.type == "product" }
                if (prods.isEmpty()) LightEmptyBox("Sin productos adicionales")
                else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prods.forEach { item ->
                            CartItemLightRow(item) { cartItems = cartItems - item }
                        }
                    }
                }
            }

            // 5. PAGO Y FINALIZAR
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("MÉTODO DE PAGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LightPaymentBtn("Efectivo", Icons.Default.Payments, paymentMethod == "Efectivo", { paymentMethod = "Efectivo" }, Modifier.weight(1f))
                        LightPaymentBtn("Tarjeta", Icons.Default.CreditCard, paymentMethod == "Tarjeta", { paymentMethod = "Tarjeta" }, Modifier.weight(1f))
                    }

                    if (paymentMethod == "Efectivo") {
                        Spacer(Modifier.height(24.dp))
                        Text("MONTOS RÁPIDOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LightQuickAmountBtn("$100", Modifier.weight(1f), amountReceived == "100") { amountReceived = "100" }
                            LightQuickAmountBtn("$200", Modifier.weight(1f), amountReceived == "200") { amountReceived = "200" }
                            LightQuickAmountBtn("$500", Modifier.weight(1f), amountReceived == "500") { amountReceived = "500" }
                            LightQuickAmountBtn("$1000", Modifier.weight(1f), amountReceived == "1000") { amountReceived = "1000" }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)).padding(20.dp)) {
                        Column {
                            Text("TOTAL A PAGAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("$${totalPagar.format(2)}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            
                            if (paymentMethod == "Efectivo") {
                                Spacer(Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = amountReceived,
                                        onValueChange = { if(it.isEmpty() || it.toDoubleOrNull() != null) amountReceived = it },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("RECIBIDO") },
                                        prefix = { Text("$ ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("CAMBIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        Text("$${change.format(2)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    val canFinalize = selectedBarberId != null && cartItems.isNotEmpty() && 
                                      (if(isOcasional) ocasionalName.isNotBlank() else selectedCustomerId != null) &&
                                      (paymentMethod != "Efectivo" || (amountReceived.toDoubleOrNull() ?: 0.0) >= totalPagar)

                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val request = VentaExpressRequest(
                                    usuarioId = if (!isOcasional) selectedCustomerId else null,
                                    barberoId = selectedBarberId!!,
                                    serviciosNombres = cartItems.filter { it.type == "service" }.map { it.name },
                                    productos = cartItems.filter { it.type == "product" }.map { ProductoVenta(it.id, 1) }, // Simplificado a 1 por ahora
                                    totalPagar = totalPagar,
                                    metodoPago = paymentMethod,
                                    esOcasional = isOcasional,
                                    clienteNombre = if (isOcasional) ocasionalName else null
                                )
                                val res = Greeting().processVentaExpress(request)
                                isLoading = false
                                if (res.success) {
                                    toastMessage = "Venta finalizada con éxito"
                                    toastType = ToastType.SUCCESS
                                    kotlinx.coroutines.delay(1500)
                                    onBack()
                                } else {
                                    toastMessage = res.message
                                    toastType = ToastType.ERROR
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = canFinalize && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else {
                            Text("FINALIZAR VENTA", fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        // --- MODALES ---
        if (showServiceModal) {
            LightItemsModal("AGREGAR SERVICIO", availableServices, { showServiceModal = false }) { item ->
                cartItems = cartItems + item
                showServiceModal = false
            }
        }
        if (showProductModal) {
            LightItemsModal("AGREGAR PRODUCTO", availableProducts, { showProductModal = false }) { item ->
                cartItems = cartItems + item
                showProductModal = false
            }
        }

        if (isLoading && cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFDC2626))
            }
        }

        toastMessage?.let { WolfToast(it, toastType) { toastMessage = null } }
    }
}

@Composable
fun LightTerminalSection(title: String, action: @Composable (() -> Unit)? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color(0xFFDC2626)))
                Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A), letterSpacing = 1.sp)
            }
            action?.invoke()
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun LightTerminalTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color.Gray) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0))
    )
}

@Composable
fun BarberLightCard(barber: Barber, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(90.dp, 120.dp).clickable { onClick() },
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if(isSelected) Color(0xFFDC2626).copy(alpha = 0.1f) else Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                Text(barber.nombreCompleto.take(1).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = if(isSelected) Color(0xFFDC2626) else Color.LightGray)
            }
            Spacer(Modifier.height(8.dp))
            Text(barber.nombreCompleto, textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun LightEmptyBox(text: String) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Text(text, color = Color.LightGray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}

@Composable
fun CartItemLightRow(item: CartItem, onRemove: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFF1F5F9))) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.type.uppercase(), fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$${item.price.format(2)}", fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun LightPaymentBtn(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.height(56.dp).clickable { onClick() },
        color = if (isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFDC2626) else Color.Transparent)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) Color(0xFFDC2626) else Color.Gray, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color(0xFFDC2626) else Color.Gray)
        }
    }
}

@Composable
fun LightQuickAmountBtn(label: String, modifier: Modifier, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(44.dp).clickable { onClick() },
        color = if (isSelected) Color.White else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFDC2626) else Color(0xFFE2E8F0))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color(0xFFDC2626) else Color(0xFF0F172A))
        }
    }
}

@Composable
fun LightItemsModal(title: String, items: List<CartItem>, onDismiss: () -> Unit, onSelect: (CartItem) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Surface(modifier = Modifier.fillMaxWidth().clickable { onSelect(item) }, color = Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("$${item.price.format(2)}", color = Color(0xFFDC2626), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
