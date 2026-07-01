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
import kotlinx.coroutines.launch

@Composable
fun AdminNavigationWrapper(role: String, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Dashboard") }

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
                        MenuItem("Barberos", Icons.Default.Face),
                        MenuItem("Inventario", Icons.Default.Inventory2),
                        MenuItem("Clientes", Icons.Default.Group),
                        MenuItem("Reportes", Icons.Default.Analytics),
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
                        onClick = { /* Nueva Reserva */ },
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
                        IconButton(onClick = { /* Settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                        }
                        // Profile Avatar Placeholder
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .background(Color(0xFFDC2626).copy(alpha = 0.1f), RoundedCornerShape(percent = 50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFFDC2626))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F6F6))
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (selectedItem == "Dashboard") {
                    AdminDashboard()
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Modulo de $selectedItem - Próximamente", color = Color.Gray)
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
