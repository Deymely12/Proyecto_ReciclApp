package com.example.reciclapp.screens.dashboard.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.LinkedHashMap

class DashboardRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Conteo de usuarios por rol */
    suspend fun getUserCountByRole(): Map<String, Int> {
        val snapshot = db.collection("users").get().await()
        val countMap = mutableMapOf(
            "Admin" to 0,
            "Usuario" to 0
        )

        for (doc in snapshot.documents) {
            val rol = doc.getBoolean("rol") ?: false
            if (rol) countMap["Admin"] = countMap["Admin"]!! + 1
            else countMap["Usuario"] = countMap["Usuario"]!! + 1
        }

        return countMap
    }

    /** Crecimiento de usuarios por mes */
    suspend fun getMonthlyUserGrowth(): Map<String, Int> {
        val snapshot = db.collection("users").get().await()
        val calendar = Calendar.getInstance()
        val countMap = linkedMapOf<String, Int>() // Mantener orden

        // Inicializa los últimos 12 meses
        for (i in 11 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            val key = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}"
            countMap[key] = 0
        }

        for (doc in snapshot.documents) {
            val timestamp = doc.getTimestamp("registro")?.toDate() ?: continue
            val key = "${calendar.time.year + 1900}-${String.format("%02d", timestamp.month + 1)}"
            if (countMap.containsKey(key)) {
                countMap[key] = countMap[key]!! + 1
            }
        }

        return countMap
    }

    /** Conteo de residuos de todos los usuarios */
    suspend fun getResiduosCountAllUsers(): Map<String, Int> {
        val snapshotUsuarios = db.collection("users").get().await()
        val totalCount = mutableMapOf<String, Int>()

        for (usuarioDoc in snapshotUsuarios.documents) {
            val residuosSnapshot = db.collection("users")
                .document(usuarioDoc.id)
                .collection("residuos")
                .get()
                .await()

            val countMap = residuosSnapshot.documents
                .groupingBy { it.getString("waste_type_detected") ?: "Desconocido" }
                .eachCount()

            for ((tipo, cantidad) in countMap) {
                totalCount[tipo] = (totalCount[tipo] ?: 0) + cantidad
            }
        }

        return totalCount.toSortedMap() // opcional para orden
    }
}
