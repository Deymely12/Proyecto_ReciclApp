package com.example.reciclapp.screens.noticias

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.reciclapp.model.Noticia
import androidx.compose.foundation.lazy.items
import com.example.reciclapp.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticiasFavoritasScreen(authViewModel: AuthViewModel) {

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

    val repo = remember { FirebaseFavoritosRepository() }

    var favoritos by remember { mutableStateOf<List<Noticia>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar los favoritos al entrar
    LaunchedEffect(Unit) {
        repo.obtenerFavoritos(userId) { lista ->
            favoritos = lista
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Noticias Favoritas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )


        if(cargando){
            CircularProgressIndicator()
        }
        else if (favoritos.isEmpty()){
            Text("No hay noticias favoritas ...")
        }else {
            LazyColumn {
                items(favoritos){favo->
                    FavoritoItem(
                        favo

                    )
                }
            }
        }
    }
}

@Composable
fun FavoritoItem(noticia: Noticia) {
    var mostrarDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                //.padding(bottom = 8.dp),  // espaciado inferior opcional
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (modifier = Modifier.weight(1f)){
                    Text(
                        text = noticia.titulo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = noticia.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Categoría: ${noticia.categoria}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(
                    onClick = { mostrarDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
        if (mostrarDialog) {
            DetalleNoticiaDialog(noticia = noticia, onDismiss = { mostrarDialog = false })
        }
    }
}

@Composable
fun DetalleNoticiaDialog(noticia: Noticia, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }

        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                //.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = noticia.titulo,
                    style = MaterialTheme.typography.titleLarge
                )
                //Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Categoria: ${noticia.categoria}",
                    modifier = Modifier.padding(top = 8.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Descripción: ${noticia.descripcion}",
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                //Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(noticia.imagenRecurso),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                //Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Fecha de Publicación: ${noticia.fecha}",
                    modifier = Modifier.padding(top = 8.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                //Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Por otro lado, ${noticia.descripcionAdicional}",
                    modifier = Modifier.padding(top = 8.dp))

            }
        }
    )
}