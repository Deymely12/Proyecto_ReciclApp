package com.example.reciclapp.screens.points

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reciclapp.model.Promotion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionDetailScreen(
    navController: NavController,
    promotionId: String
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var promotion by remember { mutableStateOf<Promotion?>(null) }
    var totalPoints by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isRedeeming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Cargar datos de la promo + puntos del usuario
    LaunchedEffect(promotionId) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            errorMessage = "No se pudo identificar al usuario."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null

            val promoSnap = db.collection("promotions")
                .document(promotionId)
                .get()
                .await()

            if (!promoSnap.exists()) {
                errorMessage = "La promoción no existe."
                isLoading = false
                return@LaunchedEffect
            }

            val data = promoSnap.data ?: emptyMap<String, Any>()
            promotion = Promotion(
                id = promoSnap.id,
                descripcion = data["descripcion"] as? String ?: "",
                porcentaje = (data["porcentaje"] as? Number)?.toInt() ?: 0,
                puntos = (data["puntos"] as? Number)?.toInt() ?: 0,
                cadena = data["cadena"] as? String ?: ""
            )

            val userSnap = db.collection("users")
                .document(uid)
                .get()
                .await()

            totalPoints = userSnap.getLong("totalPoints")?.toInt() ?: 0

        } catch (e: Exception) {
            errorMessage = "Error al cargar la promoción."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de promoción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                promotion == null -> {
                    Text("No se encontró la promoción.")
                }

                else -> {
                    val promo = promotion!!

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = promo.cadena,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = promo.descripcion,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Descuento: ${promo.porcentaje}%",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Puntos necesarios: ${promo.puntos}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Tus puntos actuales: $totalPoints",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (successMessage != null) {
                                Text(
                                    text = successMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (errorMessage != null && successMessage == null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (promo.puntos > totalPoints) {
                                    errorMessage = "No tienes puntos suficientes para canjear esta promoción."
                                    successMessage = null
                                    return@Button
                                }

                                scope.launch {
                                    isRedeeming = true
                                    errorMessage = null
                                    successMessage = null

                                    val uid = auth.currentUser?.uid
                                    if (uid == null) {
                                        errorMessage = "Usuario no identificado."
                                        isRedeeming = false
                                        return@launch
                                    }

                                    try {
                                        // Generar código aleatorio
                                        val code = UUID.randomUUID()
                                            .toString()
                                            .take(8)
                                            .uppercase()

                                        // Descontar puntos en Firebase
                                        val userRef = db.collection("users").document(uid)

                                        db.runTransaction { tx ->
                                            val snap = tx.get(userRef)
                                            val currentPoints = snap.getLong("totalPoints") ?: 0L

                                            if (currentPoints < promo.puntos) {
                                                throw Exception("Puntos insuficientes.")
                                            }

                                            tx.update(
                                                userRef,
                                                "totalPoints",
                                                currentPoints - promo.puntos
                                            )

                                            // OPCIONAL: guardar el canje en otra colección
                                           /* val redemptionRef = db.collection("redemptions").document()
                                            tx.set(
                                                redemptionRef,
                                                mapOf(
                                                    "userId" to uid,
                                                    "promotionId" to promo.id,
                                                    "code" to code,
                                                    "cadena" to promo.cadena,
                                                    "timestamp" to FieldValue.serverTimestamp()
                                                )
                                            )*/
                                        }.await()

                                        // Actualizar puntos en UI
                                        totalPoints -= promo.puntos

                                        successMessage =
                                            "Acércate a ${promo.cadena} a reclamar tu descuento con este código: $code"
                                        errorMessage = null

                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Error al canjear la promoción."
                                        successMessage = null
                                    } finally {
                                        isRedeeming = false
                                    }
                                }
                            },
                            enabled = !isRedeeming,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = if (isRedeeming) "Canjeando..." else "Canjear promoción"
                            )
                        }
                    }
                }
            }
        }
    }
}
