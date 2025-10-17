package com.example.reciclapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.reciclapp.R
import androidx.compose.ui.graphics.Color

@Composable
fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    if (photoUrl.isNullOrEmpty()) {
        Image(
            painter = painterResource(id = R.drawable.ic_perfil),
            contentDescription = "Profile picture",
            modifier = modifier
                .size(100.dp)
                .clip(CircleShape),
     //           .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(photoUrl),
            contentDescription = "Profile picture",
            modifier = modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
