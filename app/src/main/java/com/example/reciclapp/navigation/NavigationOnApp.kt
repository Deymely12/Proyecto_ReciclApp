package com.example.reciclapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.privacysandbox.ads.adservices.topics.Topic
import com.example.reciclapp.screens.home.Footer
import com.example.reciclapp.screens.home.Header

@Composable
fun MainLayout(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold (
        topBar = { Header(navController) },
        bottomBar = {Footer(navController)}

    ){innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            content()
        }

    }
}




