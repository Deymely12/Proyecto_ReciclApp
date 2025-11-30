package com.example.reciclapp.data.marker

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.example.reciclapp.model.Marker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseMarkerRepository(
    private val firestore: FirebaseFirestore
) : MarkerRepository {

    override fun getActiveMarkers(): Flow<List<Marker>> = callbackFlow {
        val registration = firestore.collection("marker")
            .whereEqualTo("state", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val markers = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val name = data["name"] as? String ?: return@mapNotNull null
                    val gp = data["coordinates"] as? GeoPoint ?: return@mapNotNull null
                    val desc = data["description"] as? String
                    val state = data["state"] as? Boolean ?: true

                    Marker(
                        id = doc.id,
                        name = name,
                        latitude = gp.latitude,
                        longitude = gp.longitude,
                        description = desc,
                        isEnabled = state
                    )
                }.orEmpty()

                trySend(markers)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun registerMarker(marker: Marker) {
        val data = hashMapOf(
            "name" to marker.name,
            "description" to marker.description,
            "coordinates" to GeoPoint(marker.latitude, marker.longitude),
            "state" to marker.isEnabled
        )

        firestore.collection("marker")
            .add(data)
            .await()
    }
}