package com.example.mycompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TransparentCircularProgressBar(isLoading: Boolean) {
    if (!isLoading) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)), // Semi-transparent background
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp), // Size of the progress indicator
            color = MaterialTheme.colorScheme.secondary, // Progress indicator color
            strokeWidth = 4.dp // Thickness of the progress indicator
        )
    }
}

@Preview
@Composable
fun SimpleCosmposablePreview() {
    TransparentCircularProgressBar(true)
}
