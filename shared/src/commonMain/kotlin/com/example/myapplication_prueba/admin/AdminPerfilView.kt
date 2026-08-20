package com.example.myapplication_prueba.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication_prueba.AdminUser
import com.example.myapplication_prueba.AdminUserRequest
import com.example.myapplication_prueba.Greeting
import com.example.myapplication_prueba.PerfilAdmin
import com.example.myapplication_prueba.cuenta.ToastType
import com.example.myapplication_prueba.cuenta.WolfToast
import kotlinx.coroutines.launch

@Composable
fun AdminPerfilView(onBack: () -> Unit, onNavigateToSecurity: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFDC2626)

    var currentAdmin by remember { mutableStateOf<PerfilAdmin?>(null) }
    var adminList by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var showAddAdminModal by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastType by remember { mutableStateOf(ToastType.SUCCESS) }

    // Fetch initial data
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                val profile = Greeting().getAdminProfile()
                val list = Greeting().getAdministrators()
                
                if (profile != null) {
                    currentAdmin = profile
                } else {
                    toastMessage = "No se pudieron cargar tus datos de administrador"
                    toastType = ToastType.ERROR
                }
                adminList = list
            } catch (e: Exception) {
                toastMessage = "Error de conexión: ${e.message}"
                toastType = ToastType.ERROR
            } finally {
                isLoading = false
            }
        }
    }

    if (isEditing) {
        // Mostramos directamente la vista de edición si el estado isEditing es true
        AdminEditarPerfilView(
            perfil = currentAdmin,
            onBack = { 
                isEditing = false
                // Al volver, refrescamos AMBAS cosas: el perfil y la lista total
                coroutineScope.launch { 
                    isLoading = true
                    val profile = Greeting().getAdminProfile()
                    val list = Greeting().getAdministrators()
                    currentAdmin = profile
                    adminList = list
                    isLoading = false
                }
            }
        )
    } else {
        // Todo el contenido original dentro de un else para que sea mutuamente excluyente
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
            if (showAddAdminModal) {
                AddAdminModal(
                    onDismiss = { showAddAdminModal = false },
                    onAdminAdded = {
                        showAddAdminModal = false
                        toastMessage = "Administrador agregado correctamente"
                        toastType = ToastType.SUCCESS
                        // Refresh list
                        coroutineScope.launch { adminList = Greeting().getAdministrators() }
                    },
                    onError = { msg ->
                        toastMessage = msg
                        toastType = ToastType.ERROR
                    }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                            .size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }

                    Text(
                        text = "Configuración de Administrador",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF121212)
                    )

                    // Layout
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        CuentaActivaCard(currentAdmin, onEditClick = { isEditing = true })

                        AdministradoresCard(
                            adminList = adminList,
                            currentAdminEmail = currentAdmin?.email ?: "",
                            onAddClick = { showAddAdminModal = true }
                        )
                    }
                }
            }

            toastMessage?.let {
                WolfToast(message = it, type = toastType, onDismiss = { toastMessage = null })
            }
        }
    }
}

@Composable
fun CuentaActivaCard(admin: PerfilAdmin?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = Color(0xFFDC2626))
                Text("CUENTA ACTIVA", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminDataField("Nombre Completo", "${admin?.nombres ?: "Admin"} ${admin?.apellidos ?: ""}")
                AdminDataField("Correo Electrónico", admin?.email ?: "admin@wolf-look.com")
                AdminDataField("Rol de Acceso", admin?.rol ?: "Master Admin", enabled = false)
                
                HorizontalDivider(color = Color(0xFFF1F5F9))
                
                // Editar Perfil Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditClick() }, // Usamos el callback que viene del padre
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                            Text("Editar Mi Perfil", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDataField(label: String, value: String, enabled: Boolean = true) {
    Column {
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFDC2626),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                disabledContainerColor = Color(0xFFF1F5F9).copy(alpha = 0.5f)
            ),
            enabled = enabled
        )
    }
}

@Composable
fun AdministradoresCard(adminList: List<AdminUser>, currentAdminEmail: String, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFDC2626))
                    Text("ADMINISTRADORES", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    Text("Nuevo", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                adminList.forEach { admin ->
                    AdminListItem(admin, admin.email == currentAdminEmail)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "* Los administradores pueden agregar/eliminar servicios, ver inventario y modificar citas.",
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminListItem(admin: AdminUser, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F6F6), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (admin.nombres.isNotEmpty()) admin.nombres.take(1).uppercase() else "A",
                    fontWeight = FontWeight.Black,
                    color = Color.Gray
                )
            }
            Column {
                Text("${admin.nombres} ${admin.apellidos}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(admin.email, fontSize = 12.sp, color = Color.Gray)
            }
        }
        if (isMe) {
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "TÚ",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF15803D)
                )
            }
        }
    }
}

@Composable
fun AddAdminModal(onDismiss: () -> Unit, onAdminAdded: () -> Unit, onError: (String) -> Unit) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Administrador", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModalField("Nombres", nombres, { nombres = it }, Modifier.weight(1f))
                    ModalField("Apellidos", apellidos, { apellidos = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModalField("Teléfono", phone, { if (it.length <= 10) phone = it }, Modifier.weight(1f), KeyboardType.Phone)
                    ModalField("Correo", email, { email = it }, Modifier.weight(1f), KeyboardType.Email)
                }
                
                Column {
                    Text("CONTRASEÑA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        }
                    )
                }

                Column {
                    Text("CONFIRMAR CONTRASEÑA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password == confirmPassword && nombres.isNotEmpty() && email.isNotEmpty()) {
                        isLoading = true
                        coroutineScope.launch {
                            val result = Greeting().addAdministrator(
                                AdminUserRequest(nombres, apellidos, email, phone, password)
                            )
                            isLoading = false
                            if (result.success) {
                                onAdminAdded()
                            } else {
                                onError(result.message)
                            }
                        }
                    } else {
                        onError("Las contraseñas no coinciden o faltan campos")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Crear Admin", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun ModalField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(modifier = modifier) {
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}
