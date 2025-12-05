package com.example.reciclapp.screens.dashboard.viewmodeldashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.screens.dashboard.data.models.CrecimientoUsuario
import com.example.reciclapp.screens.dashboard.data.models.ResiduosData
import com.example.reciclapp.screens.dashboard.data.models.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AdminDashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Cantidad de usuarios: Pair<adminCount, userCount>
    private val _userRoles = MutableStateFlow<Pair<Int, Int>>(0 to 0)
    val userRoles: StateFlow<Pair<Int, Int>> = _userRoles

    // Crecimiento mensual
    private val _monthlyGrowth = MutableStateFlow<List<CrecimientoUsuario>>(emptyList())
    val monthlyGrowth: StateFlow<List<CrecimientoUsuario>> = _monthlyGrowth

    // Residuos
    private val _residuosData = MutableStateFlow<List<ResiduosData>>(emptyList())
    val residuosData: StateFlow<List<ResiduosData>> = _residuosData

    // Filtro de residuos (día, semana, mes)
    private val _residuosFilter = MutableStateFlow("Mes")
    val residuosFilter: StateFlow<String> = _residuosFilter

    fun setResiduosFilter(filtro: String) {
        _residuosFilter.value = filtro
        loadResiduos()
    }

    init {
        loadUserRoles()
        loadMonthlyGrowth()
        loadResiduos()
    }

    private fun loadUserRoles() {
        viewModelScope.launch {
            val usuarios = fetchUsuariosFromFirebase()
            val adminCount = usuarios.count { it.rol }
            val userCount = usuarios.size - adminCount
            _userRoles.value = adminCount to userCount
        }
    }

    private fun loadMonthlyGrowth() {
        viewModelScope.launch {
            val usuarios = fetchUsuariosFromFirebase()

            // Agrupa por mes usando Calendar
            val grouped: Map<Int, List<Usuario>> = usuarios.groupBy { user ->
                val cal = Calendar.getInstance()
                cal.time = user.registro.toDate()
                cal.get(Calendar.MONTH) + 1
            }

            val monthlyList = grouped.map { (mes: Int, list: List<Usuario>) ->
                CrecimientoUsuario(mes.toString(), list.size)
            }.sortedBy { it.mes.toInt() }

            _monthlyGrowth.value = monthlyList
        }
    }

    private fun loadResiduos() {
        viewModelScope.launch {
            val allResiduos = mutableListOf<ResiduosData>()
            val usuarios = fetchUsuariosFromFirebase()
            usuarios.forEach { user ->
                if (user.uid.isNotBlank()) {
                    val userResiduos = getResiduosCount(user.uid)
                    allResiduos.addAll(userResiduos)
                }
            }
            _residuosData.value = allResiduos
        }
    }


    // Trae todos los usuarios desde Firebase y asigna uid
    private suspend fun fetchUsuariosFromFirebase(): List<Usuario> {
        val snapshot = db.collection("users").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Usuario::class.java)?.copy(
                uid = doc.id,
                registro = doc.getTimestamp("registro") ?: Timestamp.now()
            )
        }
    }

    // Cuenta residuos de un usuario
    private suspend fun getResiduosCount(uid: String): List<ResiduosData> {
        val snapshot = db.collection("users")
            .document(uid)
            .collection("residuos")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val tipo = doc.getString("waste_type_detected") ?: "desconocido"
            val cantidad = doc.getLong("cantidad")?.toInt() ?: 1
            val fecha = doc.getTimestamp("timestamp") ?: com.google.firebase.Timestamp.now()
            ResiduosData(tipo, cantidad, fecha)
        }
    }

}
