package com.example.reciclapp.screens.noticias

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reciclapp.model.Noticia
import com.example.reciclapp.R
import com.example.reciclapp.viewmodel.NoticiasViewModel
import androidx.navigation.NavHostController
import com.example.reciclapp.screens.noticias.FirebaseFavoritosRepository
import com.example.reciclapp.viewmodel.AuthState
import com.example.reciclapp.viewmodel.AuthViewModel
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.text.equals
import kotlin.text.replace
import kotlin.text.trimIndent
import kotlin.toString

@Composable
//fun NoticiasScreen(viewModel: NoticiasViewModel, userId:String) {

fun NoticiasScreen(viewModel: NoticiasViewModel, authViewModel: AuthViewModel) {

     val user=authViewModel.obtenerUsuarioActual()

    val userEmailconPunto : String
    if(user!=null){
        if(user.email!=null){
            userEmailconPunto=user.email.toString()
        }else{
            userEmailconPunto="null"
        }
    }else{
        userEmailconPunto="null"
    }


    val userId=emailSeguro(userEmailconPunto)
    //val userEmailconPunto = user?.email.toString()
    //val userId = user?.uid.toString()

    val noticias by viewModel.noticias
    val loading by viewModel.loading

    //Estado para el filtro por categoría
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    val categorias = listOf(
        "Todas",
        "Reciclaje",
        "Tecnología verde",
        "Educación ambiental",
        "Comunidad sostenible"
    )
    var expanded by remember { mutableStateOf(false) }
    //==============

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Noticias Ambientales",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    viewModel.actualizarNoticias()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator(
                //color = MaterialTheme.colorScheme.onPrimary,
                //modifier = Modifier.size(20.dp)
            )
            Text("Actualizando...")
        } else {

            if (noticias.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $categoriaSeleccionada")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                onClick = {
                                    categoriaSeleccionada = categoria
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        categoria,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            )
                        }
                    }
                }
            }

            val noticiasFiltradas: List<Noticia>

            if (categoriaSeleccionada == "Todas")
                noticiasFiltradas = noticias
            else
                noticiasFiltradas = noticias.filter { noticiaFiltrada ->
                    noticiaFiltrada.categoria.equals(categoriaSeleccionada, ignoreCase = true)
                }

            LazyColumn {
                items(noticiasFiltradas) { noticiaFil ->
                    NoticiasItem(noticiaFil, userId)
                }
            }
        }
    }
}

@Composable
fun NoticiasItem(noticia: Noticia, userId: String){
    var mostrarInformacionAdicional by remember { mutableStateOf(false) }
    var esFavorito by remember { mutableStateOf(false) }
    val repoFavoritos = remember { FirebaseFavoritosRepository() }

    LaunchedEffect(Unit) {
        repoFavoritos.esFavorito(userId, noticia.titulo) {
            esFavorito = it
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = noticia.categoria,
                    style = MaterialTheme.typography.labelSmall
                )

                Row {
                    // Botón de favoritos
                    IconButton(
                        onClick = {
                            repoFavoritos.toggleFavorito(userId, noticia) { nuevoEstado ->
                                esFavorito = nuevoEstado
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) Color.Red else Color.Gray
                        )
                    }

                    //Botón de Compartir
                    val contexto = LocalContext.current
                    IconButton(
                        onClick = {
                            compartirNoticia(contexto, noticia)
                        }

                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = noticia.titulo,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    //.width(500.dp)
                    .height(200.dp)
                    .padding(top = 8.dp),

                painter = painterResource(noticia.imagenRecurso),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            //Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            )
            {
                Text(
                    text = "Fecha de publicación ${noticia.fecha}",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = noticia.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )


            //Mostrar Informacion Adicional
            Text(
                text = if (mostrarInformacionAdicional) "Ver menos" else "Ver más",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
                        mostrarInformacionAdicional = !mostrarInformacionAdicional
                    },
                //color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            //if(mostrarInformacionAdicional){
            AnimatedVisibility(
                visible = mostrarInformacionAdicional,  // <--- aquí está el condicional
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = noticia.descripcionAdicional,
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                }
            }
        }
    }
}


fun compartirNoticia(contexto: Context, noticia: Noticia){
    val mensaje = """
        Categoría: ${noticia.categoria}
        Título de la noticia: ${noticia.titulo}
        Descripción: ${noticia.descripcion}\n${noticia.descripcionAdicional}
    """.trimIndent()

    val enviarIntent = Intent(Intent.ACTION_SEND).apply {
        //Intent.setAction = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,mensaje)
            type ="text/plain" //Recien agregado
        //)
        //Intent.setType = "text/plain"
    }
    val compartirIntent = Intent.createChooser(enviarIntent, "Compartir noticia")
    contexto.startActivity(compartirIntent)

}

fun emailSeguro(email: String): String {
    return email.replace(".", "_")
}