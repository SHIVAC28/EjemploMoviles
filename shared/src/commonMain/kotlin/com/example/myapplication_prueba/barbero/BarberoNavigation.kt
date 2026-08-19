package com.example.myapplication_prueba.barbero

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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarberoNavigationWrapper(role: String, onLogout: () -> Unit, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Mi Agenda") }
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "Security"

    val primaryColor = Color(0xFFDC2626)

    if (currentSubScreen == "Security") {
        SeguridadView(email = "barbero@wolf.com", onBack = { currentSubScreen = null })
    } else if (showProfile) {
        // Reuse PerfilView (compatible with all roles if data exists)
        com.example.myapplication_prueba.cliente.PerfilView(onBack = { showProfile = false }, onNavigateToSecurity = { currentSubScreen = "Security" })
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
                                Text("PANEL BARBERO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val menuItems = listOf(
                            BarberMenuItem("Mi Agenda", Icons.Default.CalendarMonth),
                            BarberMenuItem("Citas Hoy", Icons.Default.Today),
                            BarberMenuItem("Clientes", Icons.Default.People)
                        )

                        menuItems.forEach { item ->
                            BarberNavigationItem(
                                item = item,
                                isSelected = selectedItem == item.title,
                                onClick = {
                                    selectedItem = item.title
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        
                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Logout, null, tint = Color.Gray)
                            Spacer(Modifier.width(12.dp))
                            Text("CERRAR SESIÓN", color = Color.Gray, fontWeight = FontWeight.Bold)
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
                                Text("¡A darle con todo, Maestro!", fontSize = 12.sp, color = Color.Gray)
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
                                        text = { Text("Seguridad") },
                                        onClick = { isProfileMenuExpanded = false; currentSubScreen = "Security" }
                                    )
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    when(selectedItem) {
                        "Mi Agenda", "Citas Hoy" -> BarberoAgendaView()
                        "Clientes" -> BarberoClientesView()
                        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Próximamente...") }
                    }
                }
            }
        }
    }
}

data class BarberMenuItem(val title: String, val icon: ImageVector)

@Composable
fun BarberNavigationItem(item: BarberMenuItem, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) Color(0xFFDC2626) else Color.Gray
    val bgColor = if (isSelected) Color(0xFFDC2626).copy(alpha = 0.05f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(item.title, color = contentColor, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, fontSize = 14.sp)
    }
}
