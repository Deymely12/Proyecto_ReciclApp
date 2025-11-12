package com.example.reciclapp.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.reciclapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(navController: NavHostController) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.reciclapplogo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ReciclApp",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        actions = {
            IconButton(
                onClick = { navController.navigate("profile") },
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
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}