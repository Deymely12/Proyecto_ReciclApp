package com.example.reciclapp.data.recycle

import com.example.reciclapp.model.RecycleBin
import com.example.reciclapp.model.RecycleMetrics
import com.example.reciclapp.model.WasteType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val RECYCLE_BIN_COLLECTION = "Recycle bin"
private const val USERS_COLLECTION = "users"
private const val RESIDUOS_SUBCOLLECTION = "residuos"

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

        // Guardamos también el id del documento
        return recycleBin.copy(id = snapshot.id)
    }

    /**
     * Registra un evento de reciclaje para el usuario actual:
     * 1) Actualiza totales en el documento del usuario (totalPoints, etc.)
     * 2) Crea un documento en users/{uid}/residuos con el detalle del residuo.
     *
     * @param recycleBin       Doc de la colección "Recycle bin" (con metrics_per_item).
     * @param wasteTypeDocId   Id del tipo de residuo ("plastico", "vidrio", etc.).
     * @param imageUrl         URL de la imagen escaneada (si la tienes, si no puede ser null).
     */
    suspend fun registerRecycleForCurrentUser(
        recycleBin: RecycleBin,
        wasteTypeDocId: String,
        imageUrl: String? = null
    ) {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No hay usuario logeado")

        val userDocRef = db.collection(USERS_COLLECTION)
            .document(currentUser.uid)

        // subcolección "residuos"
        val residuosCollection = userDocRef.collection(RESIDUOS_SUBCOLLECTION)

        val metrics = recycleBin.metrics_per_item

        // Mapa solo con métricas no nulas
        val metricsMap = mutableMapOf<String, Any>()
        metrics.co2_avoided_kg?.let { metricsMap["co2_avoided_kg"] = it }
        metrics.co2_equivalent_avoided_kg?.let { metricsMap["co2_equivalent_avoided_kg"] = it }
        metrics.waste_avoided_kg?.let { metricsMap["waste_avoided_kg"] = it }
        metrics.energy_saved_kwh?.let { metricsMap["energy_saved_kwh"] = it }
        metrics.oil_saved_liters?.let { metricsMap["oil_saved_liters"] = it }
        metrics.trees_saved_factor?.let { metricsMap["trees_saved_factor"] = it }
        metrics.water_saved_liters?.let { metricsMap["water_saved_liters"] = it }
        metrics.sand_saved_kg?.let { metricsMap["sand_saved_kg"] = it }
        metrics.bauxite_saved_kg?.let { metricsMap["bauxite_saved_kg"] = it }
        metrics.compost_produced_kg?.let { metricsMap["compost_produced_kg"] = it }
        metrics.contamination_prevented?.let { metricsMap["contamination_prevented"] = it }

        // Datos del documento en la subcolección "residuos"
        val residuoData = hashMapOf(
            "timestamp" to FieldValue.serverTimestamp(), // Firestore Timestamp
            "waste_type_detected" to wasteTypeDocId,
            "image_url" to (imageUrl ?: ""),
            "points_awarded" to recycleBin.points_per_item,
            "metrics_calculated" to metricsMap
        )

        db.runTransaction { tx ->
            val snapshot = tx.get(userDocRef)

            // Totales actuales (si no existen, asumimos 0)
            val currentPoints = snapshot.getLong("totalPoints") ?: 0L

            val userUpdates = mapOf(
                "totalPoints" to (currentPoints + recycleBin.points_per_item),
            )

            // 1) Actualizamos totales del usuario
            tx.set(userDocRef, userUpdates, SetOptions.merge())

            // 2) Creamos el documento en la subcolección "residuos"
            val newResiduoRef = residuosCollection.document()
            tx.set(newResiduoRef, residuoData)
        }.await()
    }
}
