package com.example.reciclapp.screens.points

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.model.Promotion
import com.example.reciclapp.model.PromotionsViewMode
import com.example.reciclapp.model.getViewModeFlow
import com.example.reciclapp.model.saveViewMode
import com.example.reciclapp.R
import com.example.reciclapp.screens.ranking.RankingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsPromotionsScreen(
    navController: NavController,
    viewModel: PointsPromotionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // DataStore -> modo de vista (LIST / GRID)
    val viewModeFlow = remember { getViewModeFlow(context) }
    val viewMode by viewModeFlow.collectAsState(initial = PromotionsViewMode.LIST)

    //val scrollState = rememberScrollState() // de diego

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis puntos y promociones") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                //.verticalScroll(scrollState)//de diego
        ) {

            // ---- PUNTOS TOTALES ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // margen opcional
                //horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Puntos totales: ${uiState.totalPoints}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = {
                    navController.navigate("ranking")
                }) {
                    Text(text = "Ver Ranking")
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            //RankingScreen()

            //Divider()

            Spacer(modifier = Modifier.height(12.dp))

            // ---- ENCABEZADO PROMOCIONES + TOGGLE DE VISTA ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Promociones disponibles",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    IconButton(
                        onClick = {
                            scope.launch { saveViewMode(context, PromotionsViewMode.LIST) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Ver en lista",
                            tint = if (viewMode == PromotionsViewMode.LIST)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch { saveViewMode(context, PromotionsViewMode.GRID) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Ver en cuadros",
                            tint = if (viewMode == PromotionsViewMode.GRID)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---- CONTENIDO PRINCIPAL ----
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                uiState.promotions.isEmpty() -> {
                    Text("No hay promociones disponibles por ahora.")
                }

                else -> {
                    when (viewMode) {
                        PromotionsViewMode.LIST -> PromotionsList(uiState.promotions)
                        PromotionsViewMode.GRID -> PromotionsGrid(uiState.promotions)
                    }
                }
            }
        }
    }
}
@Composable
fun PromotionsList(promotions: List<Promotion>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(promotions) { promo ->
            PromotionRow(promo)
        }
    }
}

@Composable
fun PromotionsGrid(promotions: List<Promotion>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(promotions) { promo ->
            PromotionCard(promo)
        }
    }
}

// Item tipo "lista"
@Composable
fun PromotionRow(promotion: Promotion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = promotion.descripcion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = promotion.cadena,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "-${promotion.porcentaje}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${promotion.puntos} pts",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Item tipo "cuadro / card"
@Composable
fun PromotionCard(promotion: Promotion) {
    val logoRes = logoForChain(promotion.cadena)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Logo de la cadena
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Logo ${promotion.cadena}",
                    modifier = Modifier
                        .size(40.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = promotion.cadena,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = promotion.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "-${promotion.porcentaje}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${promotion.puntos} pts",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@DrawableRes
fun logoForChain(cadena: String): Int {
    return when (cadena.trim().lowercase()) {
        "tambo" -> R.drawable.tambo
        "plazavea", "plaza vea" -> R.drawable.plazavea
        "oxxo" -> R.drawable.oxxo
        "cineplanet" -> R.drawable.cineplanet
        "metro" -> R.drawable.metro_logo
        "h&m", "h & m", "hm" -> R.drawable.hm_logo
        else -> R.drawable.educacionambiental02
    }
}

