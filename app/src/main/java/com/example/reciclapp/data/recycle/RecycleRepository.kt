package com.example.reciclapp.data.recycle

import com.example.reciclapp.model.RecycleBin
import com.example.reciclapp.model.WasteType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val RECYCLE_BIN_COLLECTION = "Recycle bin"
private const val USERS_COLLECTION = "users"

class RecycleRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    suspend fun getRecycleBinForWaste(wasteType: WasteType): RecycleBin {
        val docId = wasteType.binDocId

        val snapshot = db.collection(RECYCLE_BIN_COLLECTION)
            .document(docId)
            .get()
            .await()

        val recycleBin = snapshot.toObject(RecycleBin::class.java)
            ?: throw IllegalStateException("RecycleBin no encontrado para $docId")

        return recycleBin.copy(id = snapshot.id)
    }

    suspend fun addPointsAndMetricsForCurrentUser(recycleBin: RecycleBin) {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No hay usuario logeado")

        val userDocRef = db.collection(USERS_COLLECTION)
            .document(currentUser.uid)

        db.runTransaction { tx ->
            val snapshot = tx.get(userDocRef)

            val currentPoints = snapshot.getLong("totalPoints") ?: 0L
            val currentCo2 = snapshot.getDouble("total_co2_avoided_kg") ?: 0.0
            val currentWaste = snapshot.getDouble("total_waste_avoided_kg") ?: 0.0

            val metrics = recycleBin.metrics_per_item

            val addedCo2 = (metrics.co2_avoided_kg ?: metrics.co2_equivalent_avoided_kg ?: 0.0)
            val addedWaste = (metrics.waste_avoided_kg ?: 0.0)

            val updates = mapOf(
                "totalPoints" to (currentPoints + recycleBin.points_per_item),
                "total_co2_avoided_kg" to (currentCo2 + addedCo2),
                "total_waste_avoided_kg" to (currentWaste + addedWaste)
            )

            tx.set(userDocRef, updates, SetOptions.merge())
        }.await()
    }
}
