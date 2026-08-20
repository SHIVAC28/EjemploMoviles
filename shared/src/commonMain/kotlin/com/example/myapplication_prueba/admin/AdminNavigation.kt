package com.example.myapplication_prueba.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.myapplication_prueba.*
import com.example.myapplication_prueba.admin.*
import kotlinx.coroutines.launch

@Composable
fun AdminNavigationWrapper(role: String, onLogout: () -> Unit, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Dashboard") }
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    
    // Sub-screens state
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "NewService", "NewPromotion", "NewProduct", "NewBarber", "Security"
    var editingItem by remember { mutableStateOf<Any?>(null) }

    if (currentSubScreen == "Security") {
        SeguridadView(email = "admin@wolf.com", onBack = { currentSubScreen = null })
    } else if (showProfile) {
        AdminPerfilView(onBack = { showProfile = false }, onNavigateToSecurity = { currentSubScreen = "Security" })
    } else if (currentSubScreen == "NewService") {
        NuevoServicioView(onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "NewPromotion") {
        NuevaPromocionView(onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "NewProduct") {
        NuevoProductoView(onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "NewBarber") {
        NuevoBarberoView(onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "NewCustomer") {
        AdminNuevoClienteView(onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "NewAppointment") {
        AdminNuevaReservaView(onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "EditProduct" && editingItem is Product) {
        NuevoProductoView(product = editingItem as Product, onBack = { currentSubScreen = null; editingItem = null })
    } else if (currentSubScreen == "EditService" && editingItem is Service) {
        AdminEditarServicioView(service = editingItem as Service, onBack = { currentSubScreen = null; editingItem = null })
    } else if (currentSubScreen == "EditPromotion" && editingItem is Promotion) {
        AdminEditarPromocionView(promotion = editingItem as Promotion, onBack = { currentSubScreen = null; editingItem = null })
    } else if (currentSubScreen == "EditBarber" && editingItem is Barber) {
        AdminEditarBarberoView(barber = editingItem as Barber, onBack = { currentSubScreen = null; editingItem = null })
    } else if (currentSubScreen == "EditCustomer" && editingItem is Cliente) {
        AdminEditarClienteView(customer = editingItem as Cliente, onBack = { currentSubScreen = null; editingItem = null })
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header del Menú
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFDC2626), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color.White)
                            }
                            Column {
                                Text("Wolf-Look", fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("PANEL DE ADMIN", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Items del Menú
                        val menuItems = listOf(
                            MenuItem("Dashboard", Icons.Default.Dashboard),
                            MenuItem("Citas", Icons.Default.CalendarToday),
                            MenuItem("Venta Express", Icons.Default.PointOfSale), // Nueva opción
                            MenuItem("Barberos", Icons.Default.Face),
                            MenuItem("Inventario", Icons.Default.Inventory2),
                            MenuItem("Clientes", Icons.Default.Group),
                            MenuItem("Servicios", Icons.Default.DryCleaning)
                        )

                        menuItems.forEach { item ->
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
                        
                        // Botón Nueva Reserva
                        Button(
                            onClick = { 
                                scope.launch { drawerState.close() }
                                currentSubScreen = "NewAppointment"
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("NUEVA RESERVA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    // Solo mostrar TopBar si NO estamos en una vista de pantalla completa (como Perfil)
                    // Las vistas modulares como Dashboard y Citas se renderizan dentro del Scaffold
                    TopAppBar(
                        title = {
                            Column {
                                Text(selectedItem, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                Text("Bienvenido de nuevo, Admin", fontSize = 12.sp, color = Color.Gray)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            if (selectedItem == "Citas") {
                                IconButton(onClick = { /* Refrescar citas si se desea */ }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Gray)
                                }
                            }
                            
                            Box {
                                IconButton(onClick = { isProfileMenuExpanded = true }) {
                                    Icon(
                                        Icons.Default.AccountCircle, 
                                        contentDescription = "Profile", 
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = isProfileMenuExpanded,
                                    onDismissRequest = { isProfileMenuExpanded = false },
                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                        onClick = {
                                            isProfileMenuExpanded = false
                                            showProfile = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Seguridad", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                                        onClick = {
                                            isProfileMenuExpanded = false
                                            currentSubScreen = "Security"
                                        }
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    DropdownMenuItem(
                                        text = { Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) },
                                        onClick = {
                                            isProfileMenuExpanded = false
                                            onLogout()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F6F6))
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedItem) {
                        "Dashboard" -> AdminDashboard()
                        "Citas" -> AdminCitasView(onNavigateToPOS = { selectedItem = "Venta Express" })
                        "Venta Express" -> VentaFantasmaView(onBack = { selectedItem = "Citas" })
                        "Barberos" -> AdminBarberosView(
                            onNavigateToNew = { currentSubScreen = "NewBarber" },
                            onNavigateToEdit = { barber ->
                                editingItem = barber
                                currentSubScreen = "EditBarber"
                            }
                        )
                        "Servicios" -> AdminServiciosView(
                            onNavigateToNewService = { currentSubScreen = "NewService" },
                            onNavigateToNewPromotion = { currentSubScreen = "NewPromotion" },
                            onNavigateToEditService = { service ->
                                editingItem = service
                                currentSubScreen = "EditService"
                            },
                            onNavigateToEditPromotion = { promo ->
                                editingItem = promo
                                currentSubScreen = "EditPromotion"
                            }
                        )
                        "Inventario" -> AdminInventarioView(
                            onNavigateToNewProduct = { currentSubScreen = "NewProduct" },
                            onNavigateToEditProduct = { product ->
                                editingItem = product
                                currentSubScreen = "EditProduct"
                            }
                        )
                        "Clientes" -> AdminClientesView(
                            onNavigateToNew = { currentSubScreen = "NewCustomer" },
                            onNavigateToEdit = { customer ->
                                editingItem = customer
                                currentSubScreen = "EditCustomer"
                            }
                        )
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Modulo de $selectedItem - Próximamente", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector)

@Composable
fun NavigationItem(item: MenuItem, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFDC2626) else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(item.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
        Text(
            text = item.title,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
