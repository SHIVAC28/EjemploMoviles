package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.admin.MenuItem
import com.example.myapplication_prueba.admin.NavigationItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteNavigationWrapper(role: String, onLogout: () -> Unit, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Mis Citas") }
    
    // Sub-screens
    var showProfile by remember { mutableStateOf(false) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "NewBooking"

    if (showProfile) {
        PerfilView(onBack = { showProfile = false }, onNavigateToSecurity = {})
    } else if (currentSubScreen == "NewBooking") {
        NuevaCitaView(onBack = { currentSubScreen = null })
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color(0xFFDC2626), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ContentCut, null, tint = Color.White)
                            }
                            Column {
                                Text("Wolf-Look", fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("ÁREA DE CLIENTE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Menú
                        val items = listOf(
                            MenuItem("Mis Citas", Icons.Default.CalendarToday),
                            MenuItem("Servicios", Icons.Default.DryCleaning),
                            MenuItem("Productos", Icons.Default.Inventory2)
                        )

                        items.forEach { item ->
                            NavigationItem(
                                item = item,
                                isSelected = selectedItem == item.title,
                                onClick = {
                                    selectedItem = item.title
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { 
                                scope.launch { drawerState.close() }
                                currentSubScreen = "NewBooking"
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("NUEVA RESERVA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(selectedItem, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showProfile = true }) {
                                Icon(Icons.Default.AccountCircle, null, tint = Color(0xFFDC2626), modifier = Modifier.size(32.dp))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F6F6))
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (selectedItem) {
                        "Mis Citas" -> MisCitasView(onNavigateToNew = { currentSubScreen = "NewBooking" })
                        "Servicios" -> ClienteServiciosView()
                        "Productos" -> ClienteProductosView()
                        else -> content()
                    }
                }
            }
        }
    }
}

@Composable
fun MisCitasView(onNavigateToNew: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var appointments by remember { mutableStateOf<List<com.example.myapplication_prueba.Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            appointments = Greeting().getClientAppointments()
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F6F6)).padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFDC2626))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("TU PRÓXIMO ESTILO", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Está a un clic.", fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToNew,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("AGENDAR AHORA", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("HISTORIAL DE CITAS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Gray)

                if (appointments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Aún no tienes citas agendadas", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        appointments.forEach { app ->
                            com.example.myapplication_prueba.cliente.ServicioCard(
                                com.example.myapplication_prueba.cliente.ServicioData(
                                    title = app.serviceName ?: app.service?.nombre ?: "Servicio",
                                    dateTime = "${app.date} - ${app.startTime}",
                                    price = app.totalPrice.toString(),
                                    status = app.status
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
