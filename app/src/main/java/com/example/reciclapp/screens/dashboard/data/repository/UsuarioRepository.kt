package com.example.reciclapp.screens.dashboard.data.repository

import android.content.Context
import com.example.reciclapp.screens.dashboard.data.local.DatabaseProvider
import com.example.reciclapp.screens.dashboard.data.local.UsuarioEntity
import com.example.reciclapp.screens.dashboard.data.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para manejar usuarios localmente con Room
 * y sincronizar cambios de rol a Firebase.
 */
class UsuarioRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val usuarioDao = DatabaseProvider.getDatabase(context).usuarioDao()

    /** Obtiene todos los usuarios desde Room */
    fun getAllUsuarios(): Flow<List<UsuarioEntity>> {
        return usuarioDao.obtenerUsuarios()
    }

    /** Busca usuarios por nombre o correo desde Room */
    fun searchUsuarios(query: String): Flow<List<UsuarioEntity>> {
        return usuarioDao.buscarUsuarios("%$query%")
    }

    /** Inserta o reemplaza usuarios en Room */
    suspend fun insertUsuarios(usuarios: List<UsuarioEntity>) {
        usuarioDao.insertarUsuarios(*usuarios.toTypedArray())
    }

    /** Limpia la tabla de usuarios en Room */
    suspend fun deleteAll() {
        usuarioDao.limpiarUsuarios()
    }

    /** Actualiza un usuario en Room y Firebase */
    suspend fun updateUsuario(usuario: UsuarioEntity) {
        // Actualiza en Room
        usuarioDao.actualizarUsuario(usuario)

        // Actualiza en Firebase
        db.collection("users").document(usuario.uid)
            .update("rol", usuario.rol)
            .await()
    }

    suspend fun fetchUsuariosFromFirebase(): List<UsuarioEntity> {
        val snapshot = db.collection("users").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val uid = doc.id
            val firstname = doc.getString("firstname") ?: ""
            val lastname = doc.getString("lastname") ?: ""
            val correo = doc.getString("correo") ?: ""
            val rol = doc.getBoolean("rol") ?: false

            UsuarioEntity(
                uid = uid,
                nombres = firstname,
                apellidos = lastname,
                correo = correo,
                rol = rol
            )
        }
    }

    suspend fun getUsuario(uid: String): UsuarioEntity? {
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(UsuarioEntity::class.java)?.copy(uid = doc.id)
    }

    suspend fun getResiduosCountAll(): Map<String, Int> {
        val snapshot = db.collectionGroup("residuos").get().await()
        return snapshot.documents.groupingBy { it.getString("waste_type_detected") ?: "desconocido" }
            .eachCount()
    }

    suspend fun getAllUsuariosOnce(): List<Usuario> {
        val snapshot = db.collection("users").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Usuario::class.java)
        }
    }

    suspend fun getResiduosCount(uid: String): Map<String, Int> {
        val snapshot = db.collection("users").document(uid)
            .collection("residuos").get().await()
        return snapshot.documents.groupingBy { it.getString("waste_type_detected") ?: "desconocido" }
            .eachCount()
    }


}
