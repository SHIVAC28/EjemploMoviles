package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    
    val primaryRed = Color(0xFFEF4444)

    // Data States
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var promotions by remember { mutableStateOf<List<Promotion>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var barbers by remember { mutableStateOf<List<Barber>>(emptyList()) }
    
    // UI States
    var selectedBarberId by remember { mutableStateOf<Int?>(null) }
    var ghostName by remember { mutableStateOf("Cliente") }
    var paymentMethod by remember { mutableStateOf("EFECTIVO") }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var amountReceived by remember { mutableStateOf("") }
    
    var showServicesModal by remember { mutableStateOf(false) }
    var showProductsModal by remember { mutableStateOf(false) }
    var showPrintingModal by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }
    
    val total = cartItems.sumOf { it.price }
    val received = amountReceived.toDoubleOrNull() ?: 0.0
    val change = if (received > total) received - total else 0.0

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            services = Greeting().getServices()
            promotions = Greeting().getPromotions()
            products = Greeting().getProducts()
            barbers = Greeting().getBarbers()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                    .size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }

            // 1. SELECCIÓN DE CLIENTE
            LightTerminalSection(title = "1. SELECCIÓN DE CLIENTE") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LightTerminalTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "Buscar cliente en base de datos...",
                        leadingIcon = Icons.Default.Search
                    )
                    LightTerminalTextField(
                        value = ghostName,
                        onValueChange = { ghostName = it },
                        placeholder = "Ej. Cliente de paso...",
                        leadingIcon = Icons.Default.Badge
                    )
                }
            }

            // 2. ASIGNAR BARBERO
            LightTerminalSection(title = "2. ASIGNAR BARBERO") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    barbers.forEach { barber ->
                        BarberLightCard(
                            barber = barber,
                            isSelected = selectedBarberId == barber.id,
                            onClick = { selectedBarberId = barber.id }
                        )
                    }
                }
            }

            // 3. SERVICIOS
            LightTerminalSection(
                title = "3. SERVICIOS",
                action = {
                    TextButton(onClick = { showServicesModal = true }) {
                        Icon(Icons.Default.Add, null, tint = primaryRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AGREGAR SERVICIO", color = primaryRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            ) {
                if (cartItems.none { it.type != "product" }) {
                    LightEmptyBox(text = "NO HAY SERVICIOS AGREGADOS")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cartItems.filter { it.type != "product" }.forEach { item ->
                            CartItemLightRow(item) { cartItems = cartItems.filter { it != item } }
                        }
                    }
                }
            }

            // 4. PRODUCTOS (OPCIONAL)
            LightTerminalSection(
                title = "4. PRODUCTOS (OPCIONAL)",
                action = {
                    TextButton(onClick = { showProductsModal = true }) {
                        Icon(Icons.Default.Inventory2, null, tint = primaryRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AGREGAR PRODUCTO", color = primaryRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            ) {
                if (cartItems.any { it.type == "product" }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cartItems.filter { it.type == "product" }.forEach { item ->
                            CartItemLightRow(item) { cartItems = cartItems.filter { it != item } }
                        }
                    }
                }
            }

            // Terminal Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Payment Method
                    Column {
                        Text("MÉTODO DE PAGO", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LightPaymentBtn(
                                label = "Efectivo",
                                icon = Icons.Default.Payments,
                                isSelected = paymentMethod == "EFECTIVO",
                                onClick = { paymentMethod = "EFECTIVO" },
                                modifier = Modifier.weight(1f)
                            )
                            LightPaymentBtn(
                                label = "Tarjeta",
                                icon = Icons.Default.CreditCard,
                                isSelected = paymentMethod == "TARJETA",
                                onClick = { paymentMethod = "TARJETA" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Amounts
                    Column {
                        Text("MONTOS RÁPIDOS", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LightQuickAmountBtn("$100", Modifier.weight(1f)) { amountReceived = "100" }
                                LightQuickAmountBtn("$200", Modifier.weight(1f)) { amountReceived = "200" }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LightQuickAmountBtn("$500", Modifier.weight(1f)) { amountReceived = "500" }
                                LightQuickAmountBtn("$1000", Modifier.weight(1f), isSpecial = true) { amountReceived = "1000" }
                            }
                        }
                    }

                    // Final Terminal
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Text("TOTAL A PAGAR", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$${total.format(2)}", color = Color(0xFF0F172A), fontSize = 48.sp, fontWeight = FontWeight.Black)
                        
                        if (paymentMethod == "EFECTIVO") {
                            Spacer(Modifier.height(20.dp))
                            Text("RECIBIDO", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            LightTerminalTextField(
                                value = amountReceived,
                                onValueChange = { amountReceived = it },
                                placeholder = "0.00",
                                leadingIcon = Icons.Default.AttachMoney,
                                keyboardType = KeyboardType.Number
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, primaryRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CAMBIO", color = primaryRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("$${change.format(2)}", color = primaryRed, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (selectedBarberId != null && cartItems.isNotEmpty()) {
                                showPrintingModal = true
                                coroutineScope.launch {
                                    val res = Greeting().processGhostSale(
                                        GhostAppointmentRequest(
                                            barberId = selectedBarberId!!,
                                            paymentMethod = paymentMethod,
                                            cartItems = cartItems,
                                            amountReceived = received,
                                            ghostName = ghostName
                                        )
                                    )
                                    showPrintingModal = false
                                    if (res.success) {
                                        toastMessage = "Venta finalizada con éxito"
                                        toastType = ToastType.SUCCESS
                                        kotlinx.coroutines.delay(1000)
                                        onBack()
                                    } else {
                                        toastMessage = res.message
                                        toastType = ToastType.ERROR
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedBarberId != null && cartItems.isNotEmpty() && (paymentMethod != "EFECTIVO" || received >= total)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("FINALIZAR VENTA", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Modals
        if (showServicesModal || showProductsModal) {
            val title = if(showServicesModal) "AGREGAR SERVICIO" else "AGREGAR PRODUCTO"
            val itemsToShow = if(showServicesModal) {
                services.map { CartItem(it.id ?: 0, "service", it.nombre, it.precio ?: 0.0, it.duracion ?: 0) } +
                promotions.map { CartItem(it.id ?: 0, "promotion", it.nombre, it.precioPromocional, 30) }
            } else {
                products.map { CartItem(it.id ?: 0, "product", it.nombre, it.precio) }
            }

            LightItemsModal(
                title = title,
                items = itemsToShow,
                onDismiss = { showServicesModal = false; showProductsModal = false },
                onItemSelected = { 
                    cartItems = cartItems + it
                    showServicesModal = false
                    showProductsModal = false
                }
            )
        }

        if (showPrintingModal) {
            AlertDialog(onDismissRequest = {}, confirmButton = {}, containerColor = Color.White, title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Print, null, tint = primaryRed, modifier = Modifier.size(48.dp))
                    Text("PROCESANDO...", color = Color(0xFF0F172A))
                }
            })
        }

        toastMessage?.let {
            WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
        }
    }
}

@Composable
fun LightTerminalSection(title: String, action: @Composable (() -> Unit)? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(32.dp).background(Color(0xFFEF4444)))
                Spacer(Modifier.width(12.dp))
                Text(title, color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            action?.invoke()
        }
        content()
    }
}

@Composable
fun LightTerminalTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, leadingIcon: androidx.compose.ui.graphics.vector.ImageVector, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF94A3B8)) },
        leadingIcon = { Icon(leadingIcon, null, tint = Color(0xFF64748B)) },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFFEF4444),
            unfocusedBorderColor = Color(0xFFE2E8F0)
        )
    )
}

@Composable
fun BarberLightCard(barber: Barber, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(2.dp, if (isSelected) Color(0xFFEF4444) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(barber.nombreCompleto.take(1).uppercase(), color = Color(0xFFF1F5F9), fontSize = 64.sp, fontWeight = FontWeight.Black)
            Text(barber.nombreCompleto.uppercase(), color = if(isSelected) Color(0xFFEF4444) else Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LightEmptyBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory2, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(text, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CartItemLightRow(item: CartItem, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.type.uppercase(), color = Color(0xFF64748B), fontSize = 10.sp)
        }
        Text("$${item.price.format(2)}", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 18.sp)
        IconButton(onClick = onRemove) { Icon(Icons.Default.Close, null, tint = Color(0xFFCBD5E1)) }
    }
}

@Composable
fun LightPaymentBtn(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        color = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.1f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, if (isSelected) Color(0xFFEF4444) else Color(0xFFE2E8F0))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) Color(0xFFEF4444) else Color(0xFFCBD5E1))
            Text(label, color = if (isSelected) Color(0xFFEF4444) else Color(0xFF64748B), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LightQuickAmountBtn(label: String, modifier: Modifier = Modifier, isSpecial: Boolean = false, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSpecial) Color(0xFFEF4444) else Color(0xFFE2E8F0))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if(isSpecial) Color(0xFFEF4444) else Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
fun LightItemsModal(title: String, items: List<CartItem>, onDismiss: () -> Unit, onItemSelected: (CartItem) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        title = { Text(title, color = Color(0xFF0F172A), fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .clickable { onItemSelected(item) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.name, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        Text("$${item.price.format(2)}", color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    )
}
