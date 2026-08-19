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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun AdminClientesView(
    onNavigateToNew: () -> Unit,
    onNavigateToEdit: (Cliente) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var customers by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var stats by remember { mutableStateOf(ClienteStats(0, 0, 0)) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedCustomerForView by remember { mutableStateOf<Cliente?>(null) }
    var customerToToggle by remember { mutableStateOf<Cliente?>(null) }
    var customerToDelete by remember { mutableStateOf<Cliente?>(null) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            customers = Greeting().getCustomers()
            stats = Greeting().getCustomerStats()
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
            // Stats Bento Style
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomerHeroStat(
                        modifier = Modifier.weight(2f),
                        label = "Clientes Activos",
                        value = stats.activos.toString(),
                        color = Color(0xFFDC2626)
                    )
                    CustomerMiniStat(
                        modifier = Modifier.weight(1f),
                        label = "Global",
                        value = stats.totalGlobal.toString(),
                        accentColor = Color(0xFF10B981)
                    )
                }
                CustomerMiniStat(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Inactivos",
                    value = stats.inactivos.toString(),
                    accentColor = Color(0xFF64748B)
                )
            }

            // Search & Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("DIRECTORIO DE CLIENTES", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F172A))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por nombre o número...", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0))
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onNavigateToNew,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("NUEVO", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { /* Print */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Icon(Icons.Default.Print, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("IMPRIMIR", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // List
            val filtered = customers.filter { 
                it.nombre.contains(searchQuery, ignoreCase = true) || 
                it.apellido.contains(searchQuery, ignoreCase = true) ||
                it.telefono.contains(searchQuery)
            }

            if (filtered.isEmpty() && !isLoading) {
                EmptyPlaceholder("No se encontraron clientes", Icons.Default.Group)
            } else {
                filtered.forEach { customer ->
                    CustomerCard(
                        customer = customer,
                        onView = { selectedCustomerForView = it },
                        onEdit = onNavigateToEdit,
                        onUpdateToToggle = { customerToToggle = it },
                        onUpdateToDelete = { customerToDelete = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Modals
        if (selectedCustomerForView != null) {
            CustomerDetailsModal(
                customer = selectedCustomerForView!!,
                onDismiss = { selectedCustomerForView = null }
            )
        }

        if (customerToToggle != null) {
            ConfirmDialog(
                title = "Inhabilitar Cliente",
                text = "¿Estás a punto de inhabilitar a ${customerToToggle!!.nombre}? No podrá realizar nuevas reservas.",
                confirmText = "Inhabilitar",
                confirmColor = Color(0xFFF59E0B),
                onConfirm = {
                    coroutineScope.launch {
                        val res = Greeting().toggleCustomerStatus(customerToToggle!!.id!!)
                        if (res.success) refreshData()
                        else { toastMessage = res.message; toastType = ToastType.ERROR }
                        customerToToggle = null
                    }
                },
                onDismiss = { customerToToggle = null }
            )
        }

        if (customerToDelete != null) {
            ConfirmDialog(
                title = "ELIMINAR CLIENTE",
                text = "¿Deseas ELIMINAR permanentemente a ${customerToDelete!!.nombre}? Esta acción no se puede deshacer.",
                confirmText = "Eliminar",
                confirmColor = Color(0xFFEF4444),
                onConfirm = {
                    coroutineScope.launch {
                        val res = Greeting().deleteCustomer(customerToDelete!!.id!!)
                        if (res.success) refreshData()
                        else { toastMessage = res.message; toastType = ToastType.ERROR }
                        customerToDelete = null
                    }
                },
                onDismiss = { customerToDelete = null }
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
fun CustomerHeroStat(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(6.dp).height(40.dp).background(color, RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp)).align(Alignment.CenterStart))
            Column(modifier = Modifier.padding(24.dp)) {
                Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                Text(value, fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text("Registros activos", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.Diversity3, null, modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).offset(20.dp, 20.dp).alpha(0.05f))
        }
    }
}

@Composable
fun CustomerMiniStat(modifier: Modifier, label: String, value: String, accentColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Box(modifier = Modifier.size(30.dp, 6.dp).background(accentColor, CircleShape).offset(y = (-10).dp))
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: Cliente, 
    onView: (Cliente) -> Unit, 
    onEdit: (Cliente) -> Unit, 
    onUpdateToToggle: (Cliente) -> Unit, 
    onUpdateToDelete: (Cliente) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Avatar / Foto de Perfil
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!customer.imagenUrl.isNullOrBlank()) {
                            KamelImage(
                                resource = asyncPainterResource(customer.imagenUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                onLoading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(16.dp)) } },
                                onFailure = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color(0xFFDC2626)) } }
                            )
                        } else {
                            Text(
                                text = customer.nombre.take(1).uppercase(),
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Column {
                        Text("#WL-${customer.id?.toString()?.padStart(4, '0')}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        Text("${customer.nombre} ${customer.apellido}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Surface(
                    color = if(customer.estado == "active") Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if(customer.estado == "active") "ACTIVO" else "INACTIVO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 8.sp, fontWeight = FontWeight.Black, color = if(customer.estado == "active") Color(0xFF065F46) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("EMAIL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(customer.correo, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("REGISTRO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(customer.fechaRegistro ?: "--", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEdit(customer) }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("EDITAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { onView(customer) }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PERFIL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { 
                        if (customer.estado == "active") onUpdateToToggle(customer) 
                        else onUpdateToDelete(customer)
                    },
                    modifier = Modifier.size(44.dp).background(if(customer.estado == "active") Color(0xFFFFF7ED) else Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        if(customer.estado == "active") Icons.Default.Warning else Icons.Default.Delete,
                        null,
                        tint = if(customer.estado == "active") Color(0xFFD97706) else Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDetailsModal(customer: Cliente, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))) {
                Text("Cerrar", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(56.dp).background(Color(0xFFDC2626).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(customer.nombre.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                }
                Column {
                    Text("${customer.nombre} ${customer.apellido}".uppercase(), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    StatusBadge(customer.estado == "active")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                DetailItem("Contacto", customer.correo, customer.telefono, Icons.Default.ContactMail)
                DetailItem("Fechas", "Registro: ${customer.fechaRegistro ?: "--"}", "Nacimiento: ${customer.fecha_cumpleanos ?: "--"}", Icons.Default.CalendarMonth)
                
                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        Text("DIRECCIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    Text(customer.direccion ?: "No registrada", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155), modifier = Modifier.padding(top = 4.dp))
                }

                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.EditNote, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        Text("NOTAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    Text(customer.notas ?: "Sin notas adicionales", fontSize = 12.sp, fontWeight = FontWeight.Medium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun DetailItem(title: String, line1: String, line2: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp).offset(y = 2.dp))
        Column {
            Text(title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(line1, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(line2, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
