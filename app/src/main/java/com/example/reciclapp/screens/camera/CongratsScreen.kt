package com.example.reciclapp.screens.camera

// Solo UI en Compose (sin main ni Activity)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun CongratsCard(navController: NavHostController) {

    LaunchedEffect(Unit) {
        delay(3000) // 3 segundos
        navController.navigate("noticias") {
            // opcional: sacamos ResultScreen del back stack
            popUpTo("ResultScreen") { inclusive = true }
            launchSingleTop = true
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Título
                Text(
                    text = "🎉 ¡Felicitaciones!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Círculo azul con trofeo
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF0066B3),
                                    Color(0xFF102A67)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Trofeo",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(96.dp)
                    )
                }

                // Texto de puntos y mensaje
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Has ganado 25 puntos por\n" +
                                "reciclar 1 botella(s)\n" +
                                "plástica(s).",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Tu acción ayuda a construir\nun planeta más limpio!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
