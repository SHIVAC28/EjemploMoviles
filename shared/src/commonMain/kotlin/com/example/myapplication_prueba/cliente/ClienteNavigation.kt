package com.example.myapplication_prueba.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.myapplication_prueba.SeguridadView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteNavigationWrapper(role: String, onLogout: () -> Unit, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Inicio") }
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "Security", "NewBooking"
    
    val primaryColor = Color(0xFFD32F2F)

    if (currentSubScreen == "Security") {
        SeguridadView(email = "cliente@wolf.com", onBack = { currentSubScreen = null })
    } else if (currentSubScreen == "NewBooking") {
        NuevaCitaView(onBack = { currentSubScreen = null })
    } else if (showProfile) {
        PerfilView(onBack = { showProfile = false }, onNavigateToSecurity = { currentSubScreen = "Security" })
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
                        ) {
                            Box(modifier = Modifier.size(40.dp).background(primaryColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ContentCut, null, tint = Color.White)
                            }
                            Column {
                                Text("Wolf-Look", fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("CLIENTE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val menuItems = listOf(
                            ClienteMenuItem("Inicio", Icons.Default.Dashboard),
                            ClienteMenuItem("Mis Citas", Icons.Default.CalendarToday),
                            ClienteMenuItem("Notificaciones", Icons.Default.Notifications)
                        )

                        menuItems.forEach { item ->
                            ClienteNavigationItem(
                                item = item,
                                isSelected = selectedItem == item.title,
                                onClick = {
                                    selectedItem = item.title
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        Button(
                            onClick = { 
                                currentSubScreen = "NewBooking"
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
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
                            Column {
                                Text(selectedItem, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                Text("Wolf-Look Barbershop", fontSize = 12.sp, color = Color.Gray)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        },
                        actions = {
                            Box {
                                IconButton(onClick = { isProfileMenuExpanded = true }) {
                                    Icon(Icons.Default.AccountCircle, null, tint = primaryColor, modifier = Modifier.size(32.dp))
                                }
                                DropdownMenu(
                                    expanded = isProfileMenuExpanded,
                                    onDismissRequest = { isProfileMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Mi Perfil") },
                                        onClick = { isProfileMenuExpanded = false; showProfile = true }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cerrar Sesión") },
                                        onClick = { isProfileMenuExpanded = false; onLogout() }
                                    )
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when(selectedItem) {
                        "Inicio" -> ClienteDashboard()
                        "Mis Citas" -> ClienteDashboard()
                        "Notificaciones" -> ClienteNotificacionesView()
                        else -> content()
                    }
                }
            }
        }
    }
}

data class ClienteMenuItem(val title: String, val icon: ImageVector)

@Composable
fun ClienteNavigationItem(item: ClienteMenuItem, isSelected: Boolean, onClick: () -> Unit) {
    val primaryColor = Color(0xFFD32F2F)
    val backgroundColor = if (isSelected) primaryColor else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(item.title, color = contentColor, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}
