package com.example.reciclapp.model


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ---- MODELO DE PROMOCIÓN ----
data class Promotion(
    val id: String = "",
    val descripcion: String = "",
    val porcentaje: Int = 0,
    val puntos: Int = 0, // Puntos que cuesta el descuento
    val cadena: String = "" // Tambo, PlazaVea, etc.
)

// ---- MODO DE VISTA (LISTA / GRID) ----
enum class PromotionsViewMode {
    LIST,
    GRID
}

// ---- DATASTORE ----
// Context.dataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private val VIEW_MODE_KEY = stringPreferencesKey("promotions_view_mode")

// Leer el modo guardado
fun getViewModeFlow(context: Context): Flow<PromotionsViewMode> {
    return context.dataStore.data.map { prefs ->
        when (prefs[VIEW_MODE_KEY] ?: PromotionsViewMode.LIST.name) {
            PromotionsViewMode.GRID.name -> PromotionsViewMode.GRID
            else -> PromotionsViewMode.LIST
        }
    }
}

// Guardar el modo
suspend fun saveViewMode(context: Context, mode: PromotionsViewMode) {
    context.dataStore.edit { prefs ->
        prefs[VIEW_MODE_KEY] = mode.name
    }
}