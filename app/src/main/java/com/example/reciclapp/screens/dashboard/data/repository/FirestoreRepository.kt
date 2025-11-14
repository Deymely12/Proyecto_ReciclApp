package com.example.reciclapp.screens.dashboard.data.repository

import com.example.reciclapp.screens.dashboard.data.models.Residuo
import com.example.reciclapp.screens.dashboard.data.models.TipoResiduo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.collections.get

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getResiduos(uid: String): Flow<List<Residuo>> = flow {
        val snapshot = db.collection("users")
            .document(uid)
            .collection("residuos")
            .get()
            .await()
        val residuos = snapshot.toObjects(Residuo::class.java)
        emit(residuos)
    }

    fun getTipos(): Flow<Map<String, TipoResiduo>> = flow {
        val snapshot = db.collection("Recycle bin")
            .get()
            .await()
        val map = snapshot.documents.associate { doc ->
            doc.id to doc.toObject(TipoResiduo::class.java)!!
        }
        emit(map)
    }

    fun getResiduosCount(uid: String): Flow<Map<String, Int>> = flow {
        val snapshot = db.collection("users")
            .document(uid)
            .collection("residuos")
            .get()
            .await()
        val countMap = snapshot.documents.groupingBy { it.getString("waste_type_detected") ?: "desconocido" }
            .eachCount()
        emit(countMap)
    }

    fun getPuntosPorDia(uid: String): Flow<Map<String, Int>> = flow {
        val residuosSnapshot = db.collection("users")
            .document(uid)
            .collection("residuos")
            .get()
            .await()
        val tiposSnapshot = db.collection("Recycle bin")
            .get()
            .await()
        val tiposMap = tiposSnapshot.documents.associate { it.id to it.toObject(TipoResiduo::class.java)!! }

        val puntosPorDia = residuosSnapshot.documents.groupBy { doc ->
            val ts = doc.getTimestamp("timestamp")!!
            "${ts.toDate().year + 1900}-${ts.toDate().month + 1}-${ts.toDate().date}"
        }.mapValues { entry ->
            entry.value.sumOf { doc ->
                val tipo = doc.getString("waste_type_detected")
                tiposMap[tipo]?.points_per_item ?: 0
            }
        }
        emit(puntosPorDia)
    }

    fun getImpacto(uid: String): Flow<Map<String, Double>> = flow {
        val residuosSnapshot = db.collection("users")
            .document(uid)
            .collection("residuos")
            .get()
            .await()
        val tiposSnapshot = db.collection("Recycle bin")
            .get()
            .await()
        val tiposMap = tiposSnapshot.documents.associate { it.id to it.toObject(TipoResiduo::class.java)!! }

        val impactoAcumulado = mutableMapOf<String, Double>()
        residuosSnapshot.documents.forEach { doc ->
            val tipo = doc.getString("waste_type_detected")
            val metrics = tiposMap[tipo]?.metrics_per_item ?: emptyMap()
            metrics.forEach { (key, value) ->
                impactoAcumulado[key] = (impactoAcumulado[key] ?: 0.0) + value
            }
        }
        emit(impactoAcumulado)
    }
}