package com.example.reciclapp.screens.puntoReciclaje

import android.content.Context
import android.location.Geocoder
import androidx.compose.ui.platform.LocalContext
import com.example.reciclapp.model.Coment
import com.example.reciclapp.model.Marker
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.collections.map
import kotlin.to

class MarkerRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getAllMarkers(): Flow<List<Marker>> = callbackFlow {

        val listener = db.collection("marker")
            .whereEqualTo("state", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val markers = snapshot?.documents?.map { doc ->
                    val data = doc.data ?: emptyMap<String, Any>()

                    Marker(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        isEnabled = data["state"] as? Boolean ?: false,
                        latitude = (data["coordinates"] as? GeoPoint)?.latitude ?: 0.0,
                        longitude = (data["coordinates"] as? GeoPoint)?.longitude ?: 0.0,
                        photo = data["photo"] as? String ?: "",
                        direccion = data["direccion"] as? String ?: ""
                    )
                } ?: emptyList()

                trySend(markers)
            }

        awaitClose { listener.remove() }
    }

    fun getMarkerById(markerId: String): Flow<Marker> = callbackFlow {
        val listener = db.collection("marker")
            .document(markerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val data = snapshot?.data ?: emptyMap<String, Any>()

                val marker = Marker(
                    id = snapshot?.id ?: "",
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    isEnabled = data["state"] as? Boolean ?: false,
                    latitude = (data["coordinates"] as? GeoPoint)?.latitude ?: 0.0,
                    longitude = (data["coordinates"] as? GeoPoint)?.longitude ?: 0.0
                )

                trySend(marker)
            }

        awaitClose { listener.remove() }
    }

    fun getComments(markerId: String): Flow<List<Coment>> = callbackFlow {
        val listener = db.collection("marker")
            .document(markerId)
            .collection("comments")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }


               // CoroutineScope(Dispatchers.IO).launch {
                launch {
                    val comments = snapshot?.documents?.map { doc ->
                        val data = doc.data ?: emptyMap<String, Any>()

                        val userId = data["userId"] as? String ?: ""

                        // LLAMANDO A LA FUNCIÓN SEPARADA
                        val (name, lastName) = getUserInfo(userId)

                        Coment(
                            id = doc.id,
                            userId = userId,
                            texto = data["texto"] as? String ?: "",
                            fecha = data["fecha"] as? Timestamp,
                            userName = name,
                            userLastName = lastName

                        )
                    } ?: emptyList()
                    trySend(comments)
                }
            }

        awaitClose { listener.remove() }
    }

    private suspend fun getUserInfo(userId: String): Pair<String, String> {
        return try {
            val userSnap = db.collection("users")
                .document(userId)
                .get()
                .await()

            val name = userSnap.getString("firstname") ?: ""
            val lastName = userSnap.getString("lastname") ?: ""

            Pair(name, lastName)

        } catch (e: Exception) {
            Pair("", "")
        }
    }

    suspend fun addComment(markerId: String, userId: String, texto: String) {
        val comment = mapOf(
            "userId" to userId,
            "texto" to texto,
            "fecha" to Timestamp.now()
        )

        db.collection("marker")
            .document(markerId)
            .collection("comments")
            .add(comment)
            .await()
    }
}
