package com.example.reciclapp.screens.noticias

import com.example.reciclapp.model.Noticia
import com.google.firebase.database.FirebaseDatabase
import kotlin.jvm.java

class FirebaseFavoritosRepository {

    private val db = FirebaseDatabase.getInstance().getReference("favoritos")

    fun toggleFavorito(userId: String, noticia: Noticia, onResult: (Boolean) -> Unit) {
        val noticiaRef = db.child(userId).child(noticia.titulo)

        noticiaRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {

                noticiaRef.removeValue().addOnSuccessListener {
                    onResult(false)
                }
            } else {

                noticiaRef.setValue(noticia).addOnSuccessListener {
                    onResult(true)
                }
            }
        }
    }

    fun esFavorito(userId: String, titulo: String, onResult: (Boolean) -> Unit) {
        db.child(userId).child(titulo).get().addOnSuccessListener {
            onResult(it.exists())
        }
    }

    fun obtenerFavoritos(userId: String, onResult: (List<Noticia>) -> Unit) {
        db.child(userId).get().addOnSuccessListener { snapshot ->
            val lista = mutableListOf<Noticia>()
            if (snapshot.exists()) {
                for (noticiaSnap in snapshot.children) {

                    val tituloValue = noticiaSnap.child("titulo").getValue(String::class.java)
                    val titulo = if (tituloValue != null) tituloValue else ""

                    val descripcionValue = noticiaSnap.child("descripcion").getValue(String::class.java)
                    val descripcion = if (descripcionValue != null) descripcionValue else ""

                    val categoriaValue = noticiaSnap.child("categoria").getValue(String::class.java)
                    val categoria = if (categoriaValue != null) categoriaValue else ""

                    val imagenRecursoValue = noticiaSnap.child("imagenRecurso").getValue(Int::class.java)
                    val imagenRecurso = if (imagenRecursoValue != null) imagenRecursoValue else 0

                    val descripcionAdicionalValue = noticiaSnap.child("descripcionAdicional").getValue(String::class.java)
                    val descripcionAdicional = if (descripcionAdicionalValue != null) descripcionAdicionalValue else ""

                    val fechaValue = noticiaSnap.child("fecha").getValue(String::class.java)
                    val fecha = if (fechaValue != null) fechaValue else ""

                    val noticia = Noticia(
                        titulo = titulo,
                        descripcion = descripcion,
                        categoria = categoria,
                        imagenRecurso = imagenRecurso,
                        descripcionAdicional = descripcionAdicional,
                        fecha = fecha
                    )
                    lista.add(noticia)
                }
            }
            onResult(lista)
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }


}