package com.example.reciclapp.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
fun Header(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4CAF50))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.reciclapplogo),
            contentDescription = "Logo",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "ReciclApp",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .size(40.dp)
                .background(Color.Transparent, shape = CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.casa),
                contentDescription = "Home",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


    @Composable
    fun Footer(navController: NavHostController, modifier: Modifier = Modifier) {
        Card(modifier=modifier) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4CAF50))
                    .height(80.dp)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //
                IconButton(
                    onClick = { navController.navigate("dashboard") },
                    modifier = Modifier
                        .size(60.dp)
                        //.background(Color.Transparent, shape = RectangleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.barras),
                        contentDescription = "DashBoard",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                //
                IconButton(
                    onClick = { navController.navigate("points") },
                    modifier = Modifier
                        .size(60.dp)
                        //.background(Color.Transparent, shape = RectangleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.puntos),
                        contentDescription = "Points",
                        modifier = Modifier.fillMaxSize()
                    )

                }

                //
                IconButton(
                    onClick = { navController.navigate("camera") },
                    modifier = Modifier
                        .size(100.dp)
                        //.background(Color.White.copy(alpha = 0.3f), shape = CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camara),
                        contentDescription = "Camera",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Ícono 4: Ubicación (navega a "map")
                IconButton(
                    onClick = { navController.navigate("map") },
                    modifier = Modifier
                        .size(60.dp)
                        //.background(Color.Transparent, shape = RectangleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.alfilermaps),
                        contentDescription = "Maps",
                        modifier = Modifier.fillMaxSize()
                    )

                }

                // Ícono 5: Perfil (navega a "profile")
                IconButton(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier
                        .size(60.dp)
                        //.background(Color.Transparent, shape = RectangleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.informacionpersonal),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
