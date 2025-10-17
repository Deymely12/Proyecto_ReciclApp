package com.example.reciclapp.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButtonDefaults.elevation
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.reciclapp.R

@Composable
fun Header(navController: NavHostController,modifier: Modifier = Modifier,) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //     .background(Color(0xFF4CAF50))
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
                //      color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { navController.navigate("profile") }, // lleva al home/perfil
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
}

