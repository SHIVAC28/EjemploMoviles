package com.example.myapplication_prueba.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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

@Composable
fun AdminServiciosView(
    onNavigateToNewService: () -> Unit,
    onNavigateToNewPromotion: () -> Unit,
    onNavigateToEditService: (Service) -> Unit,
    onNavigateToEditPromotion: (Promotion) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var promotions by remember { mutableStateOf<List<Promotion>>(emptyList()) }
    var stats by remember { mutableStateOf(ServiceStats(0, 0)) }
    var isLoading by remember { mutableStateOf(true) }
    
    var isServicesExpanded by remember { mutableStateOf(false) }
    var promoToToggle by remember { mutableStateOf<Promotion?>(null) }
    var promoToDelete by remember { mutableStateOf<Promotion?>(null) }
    var serviceToDelete by remember { mutableStateOf<Service?>(null) }
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            services = Greeting().getServices()
            promotions = Greeting().getPromotions()
            stats = Greeting().getServiceStats()
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Stats & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Crear Servicio", Icons.Default.AddCircle, true, onClick = onNavigateToNewService)
                    ActionButton("Crear Promoción", Icons.Default.Campaign, false, onClick = onNavigateToNewPromotion)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SimpleStat("SERVICIOS", stats.totalServices.toString(), Color(0xFF0F172A))
                    SimpleStat("PROMOS", stats.totalPromotions.toString(), Color(0xFFDC2626))
                }
            }

            // Section 1: Servicios Individuales
            Column {
                SectionHeader(
                    title = "Servicios Individuales",
                    actionText = if (services.size > 3) (if (isServicesExpanded) "Ver Menos" else "Ver Todos") else null,
                    onActionClick = { isServicesExpanded = !isServicesExpanded }
                )
                
                val displayServices = if (isServicesExpanded) services else services.take(3)
                
                if (services.isEmpty() && !isLoading) {
                    EmptyPlaceholder("No hay servicios registrados", Icons.Default.Inventory2)
                } else {
                    // Usamos una cuadrícula manual o Column de filas para móvil
                    displayServices.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { service ->
                                ServiceGridCard(
                                    service = service, 
                                    modifier = Modifier.weight(1f),
                                    onEdit = { onNavigateToEditService(service) },
                                    onDelete = { serviceToDelete = service }
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // Section 2: Lanzamientos Programados
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val upcoming = promotions.filter { 
                try { LocalDate.parse(it.fechaInicio) > today } catch(e: Exception) { false } 
            }.sortedBy { it.fechaInicio }

            if (upcoming.isNotEmpty()) {
                UpcomingBar(upcoming)
            }

            // Section 3: Promociones
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Promociones y Combos", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF0F172A))
                    Surface(color = Color(0xFFDC2626).copy(alpha = 0.1f), shape = CircleShape) {
                        Text("${stats.totalPromotions} ACTIVAS", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (promotions.isEmpty() && !isLoading) {
                    EmptyPlaceholder("No hay promociones activas", Icons.Default.Campaign)
                } else {
                    promotions.forEach { promo ->
                        PromotionLargeCard(
                            promo = promo,
                            onEdit = onNavigateToEditPromotion,
                            onToggle = { promoToToggle = it },
                            onDelete = { promoToDelete = it }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Modals
        if (promoToToggle != null) {
            val isActivating = !promoToToggle!!.activo
            ConfirmDialog(
                title = if(isActivating) "¿Prender Promoción?" else "¿Apagar Promoción?",
                text = "Esta acción cambiará la visibilidad de \"${promoToToggle!!.nombre}\" para los clientes.",
                confirmText = if(isActivating) "Prender" else "Apagar",
                confirmColor = if(isActivating) Color(0xFF10B981) else Color(0xFF64748B),
                onConfirm = {
                    coroutineScope.launch {
                        promoToToggle?.id?.let { id ->
                            val res = Greeting().togglePromotionStatus(id)
                            if (res.success) refreshData()
                            else { toastMessage = res.message; toastType = ToastType.ERROR }
                        }
                        promoToToggle = null
                    }
                },
                onDismiss = { promoToToggle = null }
            )
        }

        if (promoToDelete != null) {
            ConfirmDialog(
                title = "¿Eliminar Permanentemente?",
                text = "Esta acción borrará \"${promoToDelete!!.nombre}\" para siempre del sistema. No hay vuelta atrás.",
                confirmText = "Eliminar",
                confirmColor = Color(0xFFEF4444),
                onConfirm = {
                    coroutineScope.launch {
                        promoToDelete?.id?.let { id ->
                            val res = Greeting().deletePromotion(id)
                            if (res.success) refreshData()
                            else { toastMessage = res.message; toastType = ToastType.ERROR }
                        }
                        promoToDelete = null
                    }
                },
                onDismiss = { promoToDelete = null }
            )
        }

        if (serviceToDelete != null) {
            ConfirmDialog(
                title = "¿Eliminar Servicio?",
                text = "Esta acción eliminará permanentemente el servicio \"${serviceToDelete!!.nombre}\".",
                confirmText = "Eliminar",
                confirmColor = Color(0xFFEF4444),
                onConfirm = {
                    coroutineScope.launch {
                        serviceToDelete?.id?.let { id ->
                            val res = Greeting().deleteService(id)
                            if (res.success) refreshData()
                            else { toastMessage = res.message; toastType = ToastType.ERROR }
                        }
                        serviceToDelete = null
                    }
                },
                onDismiss = { serviceToDelete = null }
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
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, outline: Boolean, onClick: () -> Unit) {
    if (outline) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Icon(icon, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun SimpleStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun SectionHeader(title: String, actionText: String?, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF0F172A))
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(Color(0xFFDC2626), CircleShape))
        }
        if (actionText != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Icon(if (actionText.contains("Todos")) Icons.Default.ExpandMore else Icons.Default.ExpandLess, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ServiceGridCard(service: Service, modifier: Modifier, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(0.8f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image/Placeholder
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
                if (!service.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(service.imagenUrl!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.Center)) },
                        onFailure = { Icon(Icons.Default.Storefront, null, tint = Color(0xFF334155), modifier = Modifier.size(60.dp).align(Alignment.Center)) }
                    )
                } else {
                    Icon(Icons.Default.Storefront, null, tint = Color(0xFF334155), modifier = Modifier.size(60.dp).align(Alignment.Center))
                }
            }
            
            // Gradient Overlay
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.9f)))
            ))
            
            // Content
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Bottom) {
                Text(service.serviceCategory?.nombre?.uppercase() ?: "GENERAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Text(service.nombre, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Text("${service.duracion} min", color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$${service.precio}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp).background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingBar(upcoming: List<Promotion>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.RocketLaunch, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                Text("LANZAMIENTOS PROGRAMADOS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            }
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(upcoming) { promo ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFDC2626).copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Event, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(promo.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text("Inicia ${promo.fechaInicio}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionLargeCard(promo: Promotion, onEdit: (Promotion) -> Unit, onToggle: (Promotion) -> Unit, onDelete: (Promotion) -> Unit) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isScheduled = try { LocalDate.parse(promo.fechaInicio) > today } catch(e: Exception) { false }
    val isExpired = try { LocalDate.parse(promo.fechaFinal) < today } catch(e: Exception) { false }
    
    val indicatorColor = if (!promo.activo) Color.Gray else (if (isScheduled) Color(0xFFF59E0B) else if (isExpired) Color(0xFFEF4444) else Color(0xFFDC2626))

    Card(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image/Placeholder
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
                if (!promo.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(promo.imagenUrl!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.Center)) },
                        onFailure = { Icon(Icons.Default.Campaign, null, tint = Color(0xFF1E293B), modifier = Modifier.size(100.dp).align(Alignment.Center)) }
                    )
                } else {
                    Icon(Icons.Default.Campaign, null, tint = Color(0xFF1E293B), modifier = Modifier.size(100.dp).align(Alignment.Center))
                }
            }
            
            // Blur & Gradient
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xFF0F172A).copy(alpha = 0.95f), Color(0xFF0F172A).copy(alpha = 0.6f)))
            ))
            
            // Left Accent
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(indicatorColor))
            
            // Content
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(promo.nombre, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, lineHeight = 28.sp)
                        val servicesText = if (promo.nombreServicios.isNotEmpty()) promo.nombreServicios.joinToString(", ") else "Exclusivo Wolf-Look"
                        Text(servicesText, color = Color.LightGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (promo.descripcion != null && promo.descripcion.isNotBlank()) {
                            Text(promo.descripcion, color = Color.Gray, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                    
                    // Status Badge
                    if (isScheduled || isExpired) {
                        Surface(
                            color = indicatorColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if(isScheduled) "PROGRAMADA" else "CADUCADA",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("$${promo.precioPromocional}", color = if(promo.activo && !isExpired) Color(0xFFEF4444) else Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    if ((promo.precioOriginal ?: 0.0) > promo.precioPromocional) {
                        Text("$${promo.precioOriginal}", color = Color.Gray, textDecoration = TextDecoration.LineThrough, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onEdit(promo) }, modifier = Modifier.height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)), shape = RoundedCornerShape(10.dp)) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("EDITAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    IconButton(
                        onClick = { onToggle(promo) },
                        modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(if(promo.activo) Icons.Default.PowerSettingsNew else Icons.Default.Power, null, tint = if(promo.activo) Color.White else Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                    
                    IconButton(
                        onClick = { onDelete(promo) },
                        modifier = Modifier.size(40.dp).background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            // Date Badge
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    "${promo.fechaInicio} - ${promo.fechaFinal}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
            }
        }
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirmText: String, confirmColor: Color, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = confirmColor)) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EmptyPlaceholder(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
        Text(text, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}
