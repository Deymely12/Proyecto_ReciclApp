package com.example.reciclapp.screens.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reciclapp.model.User

@Composable
fun RankingScreen(navController: NavController,
    viewModel: RankingViewModel = viewModel()
) {
    val ranking by viewModel.ranking.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // BOTÓN ATRÁS
        TextButton (onClick = { navController.popBackStack() }) {
            Text("← Atrás")
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Buscar usuario")
                    },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search,
                    contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(ranking) { index, user ->
                RankingRow(position = index + 1, user = user)
                Divider()
            }
        }
    }
}

@Composable
fun RankingRow(position: Int, user: User) {

    val medal = when (position) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "$position."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$medal  ${user.firstname}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${user.totalPoints} pts",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

