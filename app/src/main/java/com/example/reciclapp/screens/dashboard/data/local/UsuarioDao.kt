package com.example.reciclapp.screens.dashboard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios ORDER BY nombres ASC")
    fun obtenerUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE nombres LIKE :query OR correo LIKE :query ORDER BY nombres ASC")
    fun buscarUsuarios(query: String): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(vararg usuarios: UsuarioEntity)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios")
    suspend fun limpiarUsuarios()
}
