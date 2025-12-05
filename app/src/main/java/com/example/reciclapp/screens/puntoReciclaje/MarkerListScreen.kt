package com.example.reciclapp.screens.puntoReciclaje

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.reciclapp.model.Coment
import kotlin.collections.forEach
import kotlin.text.isNotBlank
import kotlin.toString

@Composable
fun MarkerListScreen(navController: NavController,viewModel: MarkerViewModel = viewModel()) {

    // BOTÓN ATRÁS
    TextButton (onClick = { navController.popBackStack() }) {
        Text("← Atrás")
    }

    val markers by viewModel.markerList.collectAsState()
    val commentsMap by viewModel.comments.collectAsState()


    LazyColumn(modifier = Modifier.fillMaxSize()) {

        items(markers) { uiState ->

            val marker = uiState.marker
            val isExpanded = uiState.expanded
            val comments = commentsMap[marker.id] ?: emptyList()

            Card (
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable { viewModel.toggleExpand(marker.id) },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column (modifier = Modifier.padding(12.dp)) {

                    // ---------- Título ----------
                    Text(marker.name, style = MaterialTheme.typography.titleMedium)
                    Text("Ubicacdo en ${marker.direccion}", style = MaterialTheme.typography.bodySmall)
                    Image(
                        modifier=Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp),

                        painter = rememberAsyncImagePainter(marker.photo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop

                    )

                    // ---------- Expandible ----------
                    AnimatedVisibility(visible = isExpanded) {

                        Column {

                            Spacer(Modifier.height(8.dp))
                            Text(marker.description.toString(), style = MaterialTheme.typography.bodyMedium)
                            //Text(marker.latitude.toString())
                            //Text(marker.longitude.toString())
                            //Text(marker.id.toString())

                            Spacer(Modifier.height(12.dp))
                            Text("Comentarios", style = MaterialTheme.typography.titleSmall)

                            comments.forEach { comment ->
                                CommentItem(comment)
                            }

                            CommentInput(onSend = { text ->
                                viewModel.addComment(
                                    marker.id,
//                                    userId = "TEMP_USER",
                                    userId="dWxwJ57JwnUR068QOXXiFA1PIwj2",
                                    texto = text
                                )
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Coment) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            //Text("Usuario: ${comment.userId}", style = MaterialTheme.typography.labelSmall)
            Text(
                "${comment.userName} ${comment.userLastName} comentó:",
                style = MaterialTheme.typography.labelMedium)

            Text(comment.texto, style = MaterialTheme.typography.bodyMedium)
            //style = MaterialTheme.typography.bodyMedium
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    comment.fecha?.toDate()?.toString() ?: "",
                    style = MaterialTheme.typography.labelSmall
                )
            }

        }
    }
}


@Composable
fun CommentInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row (
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un comentario...") }
        )

        IconButton (
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }
        ) {
            Icon(Icons.Default.Send, contentDescription = "Enviar")
        }
    }
}
