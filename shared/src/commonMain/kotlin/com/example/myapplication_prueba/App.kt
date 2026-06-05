package com.example.myapplication_prueba

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose

    .resources.painterResource

import myapplication_prueba.shared.generated.resources.Res
import myapplication_prueba.shared.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }

        // Creamos una variable silenciosa para guardar lo de Railway
        var respuestaRailway by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Sistemas OP")
            }
            AnimatedVisibility(showContent) {

                // Reemplazamos la línea roja por esto. Hace la llamada a Railway en secreto.
                LaunchedEffect(showContent) {
                    if (showContent) {
                        respuestaRailway = Greeting().greet()
                        // Esto imprimirá la respuesta de tu servidor en la consola de Android Studio
                        println("Backend dice: $respuestaRailway")
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)

                    // TU TEXTO ORIGINAL INTACTO COMO LO QUERÍAS:
                    Text("¡Bienvenido a nuestro primer ejemplo!")
                }
            }
        }
    }
}