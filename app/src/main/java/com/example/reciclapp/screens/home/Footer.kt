package com.example.reciclapp.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.reciclapp.R

@Composable
fun Footer(navController: NavHostController) {
    val greenBackground = Color(0xFF4CAF50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(greenBackground)
            .height(80.dp)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { navController.navigate("dashboard") },
            modifier = Modifier
                .size(60.dp)
                .background(Color.Transparent, shape = RectangleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.barras),
                contentDescription = "Dashboard",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = { navController.navigate("points") },
            modifier = Modifier
                .size(60.dp)
                .background(Color.Transparent, shape = RectangleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.puntos),
                contentDescription = "Points",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = { navController.navigate("noticias") },
            modifier = Modifier
                .size(100.dp)
                .background(Color.White.copy(alpha = 0.3f), shape = CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.camara),
                contentDescription = "Camera",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = { navController.navigate("map") },
            modifier = Modifier
                .size(60.dp)
                .background(Color.Transparent, shape = RectangleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.alfilermaps),
                contentDescription = "Maps",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = { navController.navigate("profile") },
            modifier = Modifier
                .size(60.dp)
                .background(Color.Transparent, shape = RectangleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.informacionpersonal),
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
