package com.example.reciclapp.screens.map

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

// Data class interna solo para manejar la lista en esta pantalla.
private data class PendingMarker(
    val documentId: String,
    val name: String,
    val description: String
)

@Composable
fun RequestListScreen(navController: NavController) {
    val context = LocalContext.current
    var requests by remember { mutableStateOf<List<PendingMarker>>(emptyList()) }

    // Escucha en tiempo real la colección "marker" donde state sea false
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("marker")
            .whereEqualTo("state", false) // Muestra solo los que están pendientes
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    requests = snapshot.documents.mapNotNull { doc ->
                        PendingMarker(
                            documentId = doc.id,
                            name = doc.getString("name") ?: "Sin nombre",
                            description = doc.getString("description") ?: "Sin descripción"
                        )
                    }
                }
            }
        onDispose { listener.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Solicitudes Pendientes",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                RequestItem(
                    request = request,
                    onEnableClicked = { docId ->
                        // Actualiza el campo 'state' a 'true' en el documento correspondiente
                        FirebaseFirestore.getInstance().collection("marker").document(docId)
                            .update("state", true)
                            .addOnSuccessListener {
                                Toast.makeText(context, "${request.name} habilitado", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al habilitar", Toast.LENGTH_SHORT).show()
                            }
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Volver al Mapa")
        }
    }
}

@Composable
private fun RequestItem(request: PendingMarker, onEnableClicked: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.name, fontWeight = FontWeight.Bold)
                Text(request.description)
            }
            Button(onClick = { onEnableClicked(request.documentId) }) {
                Text("Habilitar")
            }
        }
    }
}

