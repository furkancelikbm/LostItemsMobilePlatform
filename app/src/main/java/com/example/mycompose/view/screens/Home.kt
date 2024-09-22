package com.example.mycompose.view.screens

import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.PhotoItem
import com.example.mycompose.repository.AdRepository

@Composable
fun Home(navController: NavController, adRepository: AdRepository) {

    var photoItems by remember { mutableStateOf<List<AdModel>>(emptyList()) }

    // Fetch ads when the composable is first launched
    LaunchedEffect(Unit) {
        try {
            photoItems = adRepository.getAds()
            Log.d("Home", "Ads fetched: ${photoItems.size}")
        } catch (e: Exception) {
            Log.e("Home", "Error fetching ads: ${e.message}")
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photoItems) { ad ->
            val imageUrl = ad.imageUrls.firstOrNull() ?: ""
            val title = ad.title
            PhotoItemView(photoItem = PhotoItem(imageUrl, title))
        }
    }
}

@Composable
fun PhotoItemView(photoItem: PhotoItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Column {
            Image(
                painter = rememberImagePainter(photoItem.imageUrl),
                contentDescription = photoItem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
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
    Home(navController = rememberNavController(), adRepository = AdRepository())
}
