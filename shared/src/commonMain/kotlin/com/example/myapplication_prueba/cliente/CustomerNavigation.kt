package com.example.myapplication_prueba.cliente

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
fun CustomerNavigationWrapper(role: String, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Inicio") }
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    
    val primaryColor = Color(0xFFD32F2F)

    if (showProfile) {
        ProfileView(onBack = { showProfile = false })
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
                            Column {
                                Text("Wolf-Look", fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("CLIENTE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Items del Menú
                        val menuItems = listOf(
                            CustomerMenuItem("Inicio", Icons.Default.Dashboard),
                            CustomerMenuItem("Servicios", Icons.Default.Storefront),
                            CustomerMenuItem("Mis Citas", Icons.Default.ContentCut),
                            CustomerMenuItem("Productos", Icons.Default.LocalOffer)
                        )

                        menuItems.forEach { item ->
                            CustomerNavigationItem(
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
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
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
                                Text("Wolf-Look Barbershop", fontSize = 12.sp, color = Color.Gray)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Ubicacion */ }) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Ubicacion", tint = Color.Gray)
                            }
                            
                            Box {
                                IconButton(onClick = { isProfileMenuExpanded = true }) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = primaryColor)
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
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    DropdownMenuItem(
                                        text = { Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) },
                                        onClick = {
                                            isProfileMenuExpanded = false
                                            // Aquí deberías llamar a la lógica de logout (App.kt screenState = "LOGIN")
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
                    if (selectedItem == "Inicio") {
                        CustomerDashboard()
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Modulo de $selectedItem - Próximamente", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

data class CustomerMenuItem(val title: String, val icon: ImageVector)

@Composable
fun CustomerNavigationItem(item: CustomerMenuItem, isSelected: Boolean, onClick: () -> Unit) {
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
