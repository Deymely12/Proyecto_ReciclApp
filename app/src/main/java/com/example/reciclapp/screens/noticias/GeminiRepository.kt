package com.example.reciclapp.screens.noticias

import android.util.Log
import androidx.annotation.DrawableRes
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.reciclapp.model.Noticia
import com.example.reciclapp.R

class GeminiRepository {


private val model = Firebase.vertexAI.generativeModel("gemini-2.0-flash")
/*
suspend fun getNoticias(): List<Noticia> {

    val prompt = """
    Genera 8 noticias breves relacionadas con la sostenibilidad y el medio ambiente.

    Cada noticia debe pertenecer exclusivamente a UNA de las siguientes categorías:
    1. Reciclaje
    2. Tecnología verde
    3. Educación ambiental
    4. Comunidad sostenible

    Reglas:
    - Cada categoría debe estar representada al menos una vez.
    - No repitas títulos ni temas idénticos.
    - No combines categorías en una misma noticia (mantén el enfoque en una sola).
    - Evita mencionar explícitamente las otras categorías en una noticia.
    - Usa un tono positivo, educativo y natural.
    - Incluye al menos una palabra clave relacionada con la categoría en el título o descripción:
      - Reciclaje → plástico, residuos, botellas, separación, reutilización.
      - Tecnología verde → energía solar, innovación, ecológica, sostenible, eficiencia.
      - Educación ambiental → escuela, conciencia, estudiantes, enseñanza, talleres.
      - Comunidad sostenible → vecindario, colaboración, huertos, desarrollo local, sostenibilidad.
    - Agrega el campo "fecha" en formato "yyyy-MM-dd" representando una fecha de publicación ficticia (puede ser reciente).
    - Devuelve las noticias ordenadas desde la fecha más reciente a la más antigua.
    - Devuelve la respuesta en formato JSON estricto con los siguientes campos:
      - "titulo" (máx. 10 palabras)
      - "descripcion" (máx. 150 caracteres)
      - "descripcionAdicional" (máx. 300 caracteres)
      - "fecha" (formato: "yyyy-MM-dd")

    Ejemplo del formato esperado:
    [
      {
        "titulo": "Campaña nacional de reciclaje en Lima",
        "descripcion": "Los vecinos separan residuos plásticos y de papel para su reutilización.",
        "descripcionAdicional": "Texto adicional con más contexto o impacto de la noticia (máx. 300 caracteres)",
        "fecha": "2025-10-10"
      },
      {
        "title": "Paneles solares portátiles impulsan tecnología verde",
        "description": "Emprendedores crean dispositivos ecológicos para zonas rurales sin electricidad.",
        "descripcionAdicional": "Texto adicional con más contexto o impacto de la noticia (máx. 300 caracteres)",
        "fecha": "2025-10-08"
      }
    ]
    """.trimIndent()

    try {
        val respuestaIA = model.generateContent(prompt)

        val textoRespuesta=respuestaIA.text
        if (textoRespuesta==null){
            return emptyList()
        }

        Log.d("GeminiDebug", "Respuesta cruda: $textoRespuesta")

        val jsonLimpio = textoRespuesta
            .replace("```json", "")
            .replace("```", "")
            .trim()

        Log.d("GeminiDebug", "Respuesta cruda limpia: $jsonLimpio")

        val gson=Gson()

        val jsonNoticias = jsonLimpio.substringAfter("[").substringBeforeLast("]").let {
            "[$it]"
        }

        val tipoListaNoticias = object : TypeToken<List<Noticia>>() {}.type

        var noticiasParseadas: List<Noticia> = emptyList()

        try {

            noticiasParseadas=gson.fromJson<List<Noticia>>(jsonNoticias, tipoListaNoticias)

        } catch (e: Exception) {
            Log.w("GeminiDebug", "Fallo parse directo, intentando parsear como String: ${e.message}")

            val innerJson = gson.fromJson(jsonLimpio, String::class.java)
            Log.d("GeminiDebug", "JSON final extraído: $innerJson")

            noticiasParseadas=gson.fromJson(innerJson, tipoListaNoticias)
        }

        val noticiasConCategorias = asignarCategorias(noticiasParseadas)
        val noticiasFinales=asignarImagenes(noticiasConCategorias)

        return noticiasFinales

    } catch (e: Exception) {
        Log.e("GeminiError", "Error generando tips", e)

        return emptyList()
    }

}

private fun asignarCategorias(noticias: List<Noticia>): List<Noticia> {
    return noticias.map { noticia ->
        val categoria = when {
            noticia.titulo.contains("tecnología", ignoreCase = true) ||
                    noticia.descripcion.contains("tecnología", ignoreCase = true) ||
                    noticia.descripcion.contains("innovación", ignoreCase = true) ||
                    noticia.descripcion.contains("ecológica", ignoreCase = true) -> "Tecnología verde"

            noticia.titulo.contains("educación", ignoreCase = true) ||
                    noticia.descripcion.contains("educación", ignoreCase = true)||
                    noticia.descripcion.contains("estudiantes", ignoreCase = true)||
                    noticia.descripcion.contains("enseñanza", ignoreCase = true)||
                    noticia.descripcion.contains("conciencia", ignoreCase = true) -> "Educación ambiental"

            noticia.titulo.contains("comunidad", ignoreCase = true) ||
                    noticia.descripcion.contains("comunidad", ignoreCase = true)||
                    noticia.descripcion.contains("vecindario", ignoreCase = true)||
                    noticia.descripcion.contains("colaboración", ignoreCase = true) ||
                    noticia.descripcion.contains("desarrollo local", ignoreCase = true)||
                    noticia.descripcion.contains("sostenibilidad", ignoreCase = true)-> "Comunidad sostenible"

            else -> "Reciclaje"
        }

        noticia.copy(categoria = categoria)
    }
}

private fun asignarImagenes(noticias: List<Noticia>): List<Noticia> {
    val reciclajeImages = listOf(
        R.drawable.reciclaje01,
        R.drawable.reciclaje02
    )
    val tecnologiaImages = listOf(
        R.drawable.tecnologiaverde01,
        R.drawable.tecnologiaverde02
    )
    val educacionImages = listOf(
        R.drawable.educacionambiental01,
        R.drawable.educacionambiental02
    )
    val comunidadImages = listOf(
        R.drawable.comunidadsostenible01,
        R.drawable.comunidadsostenible02
    )

    return noticias.map { noticia ->
        val imageRes = when {
            noticia.categoria.contains("reciclaje", ignoreCase = true) -> reciclajeImages.random()
            noticia.categoria.contains("tecnología", ignoreCase = true) ->  tecnologiaImages.random()
            noticia.categoria.contains("educación", ignoreCase = true) -> educacionImages.random()
            else -> comunidadImages.random() // una imagen genérica
        }

        noticia.copy(imagenRecurso = imageRes)
    }
}

<<<<<<< HEAD
*/




suspend fun getNoticias(): List<Noticia> {
    val noticiasPrueba = listOf(
        Noticia(
            "Huertos vecinales florecen: Un impulso a la sostenibilidad local",
            "Residentes transforman terrenos baldíos en productivos huertos comunitarios.",
            "Reciclaje",
            R.drawable.reciclaje01,
            "La iniciativa promueve el consumo de alimentos frescos y reduce la huella de carbono al evitar el transporte de productos desde largas distancias. Además, fortalece los lazos entre los vecinos, creando un espacio de convivencia y aprendizaje compartido sobre prácticas agrícolas sostenibles.",
            "2024-01-26"

        ),
        Noticia(
            "Escuela implementa talleres de conciencia ambiental para niños",
            "Estudiantes aprenden sobre la importancia de proteger el planeta a través de actividades lúdicas.",
            "Reciclaje",
            R.drawable.reciclaje02,
            "Los talleres incluyen juegos interactivos, experimentos científicos sencillos y salidas de campo para observar la naturaleza de cerca. El objetivo es fomentar una cultura de respeto por el medio ambiente desde temprana edad, impulsando a los niños a convertirse en agentes de cambio en sus hogares y comunidades.",
            "2024-01-20"
        ),
        Noticia(
            "Nueva planta de reciclaje reduce residuos plásticos en la ciudad",
            "Instalación moderna aumenta la capacidad de reutilización de botellas y otros materiales.",
            "Tecnología Verde",
            R.drawable.tecnologiaverde01,
            "La planta utiliza tecnología de punta para procesar una mayor cantidad de plástico y transformarlo en nuevos productos, contribuyendo significativamente a la reducción de la contaminación y al aprovechamiento de recursos. Se espera que la iniciativa impulse la economía circular en la región.",
            "2024-01-15"
        ),
        Noticia(
            "Comunidad sostenible impulsa el desarrollo local con energía solar",
            "Instalación moderna aumenta la capacidad de reutilización de botellas y otros materiales.",
            "Tecnología Verde",
            R.drawable.tecnologiaverde02,
            "La planta utiliza tecnología de punta para procesar una mayor cantidad de plástico y transformarlo en nuevos productos, contribuyendo significativamente a la reducción de la contaminación y al aprovechamiento de recursos. Se espera que la iniciativa impulse la economía circular en la región.",
            "2024-01-15"
        ),
        Noticia(
            "Innovación en la separación de residuos: Contenedores inteligentes",
            "Instalación moderna aumenta la capacidad de reutilización de botellas y otros materiales.",
            "Comunidad Sostenible",
            R.drawable.comunidadsostenible01,
            "La planta utiliza tecnología de punta para procesar una mayor cantidad de plástico y transformarlo en nuevos productos, contribuyendo significativamente a la reducción de la contaminación y al aprovechamiento de recursos. Se espera que la iniciativa impulse la economía circular en la región.",
            "2024-01-15"
        ),
        Noticia(
            "Nuevo programa de enseñanza ambiental llega a las escuelas rurales",
            "Instalación moderna aumenta la capacidad de reutilización de botellas y otros materiales.",
            "Comunidad Sostenible",
            R.drawable.comunidadsostenible02,
            "La planta utiliza tecnología de punta para procesar una mayor cantidad de plástico y transformarlo en nuevos productos, contribuyendo significativamente a la reducción de la contaminación y al aprovechamiento de recursos. Se espera que la iniciativa impulse la economía circular en la región.",
            "2024-01-15"
        ),
        Noticia(
            "Vecindario se une para la sostenibilidad: Proyecto de compostaje comunitario",
            "Instalación moderna aumenta la capacidad de reutilización de botellas y otros materiales.",
            "Educación Ambiental",
            R.drawable.educacionambiental02,
            "La planta utiliza tecnología de punta para procesar una mayor cantidad de plástico y transformarlo en nuevos productos, contribuyendo significativamente a la reducción de la contaminación y al aprovechamiento de recursos. Se espera que la iniciativa impulse la economía circular en la región.",
            "2024-01-15"
        ),
    )

    return noticiasPrueba

}



}


