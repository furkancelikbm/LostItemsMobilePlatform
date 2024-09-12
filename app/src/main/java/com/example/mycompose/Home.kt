package com.example.mycompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter

@Composable
fun Home(navController: NavController) {
    // Sample data
    val photoItems = listOf(
        PhotoItem("https://fastly.picsum.photos/id/1082/200/300.jpg?hmac=AaFCHuEst4e0Oy553UCibOtysEKByBAl3XsTR8n4e1c", "Photo 1"),
        PhotoItem("https://fastly.picsum.photos/id/237/200/300.jpg?hmac=TmmQSbShHz9CdQm0NkEjx1Dyh_Y984R9LpNrpvH2D_U", "Photo 2"),
        PhotoItem("https://picsum.photos/seed/picsum/200/300", "furkiii1"),
        PhotoItem("https://picsum.photos/seed/picsum/200/300", "furkiii2"),
        PhotoItem("https://picsum.photos/seed/picsum/200/300", "furkiii4"),
        PhotoItem("https://picsum.photos/seed/picsum/200/300", "furkiii3"),
        // Add more items here
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photoItems) { photoItem ->
            PhotoItemView(photoItem = photoItem)
        }
    }
}

@Composable
fun PhotoItemView(photoItem: PhotoItem) {
    Card(
        shape = RoundedCornerShape(24.dp), // Oval corners
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Adjust the elevation as needed
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Ensure the card maintains an aspect ratio
            .padding(8.dp) // Padding around the card
    ) {
        Column {
            Image(
                painter = rememberImagePainter(photoItem.imageUrl),
                contentDescription = photoItem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)) // Oval corners for image
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = photoItem.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    Home(navController = rememberNavController())
}
