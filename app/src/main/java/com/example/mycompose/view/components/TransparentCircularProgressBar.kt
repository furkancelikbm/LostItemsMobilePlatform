package com.example.mycompose.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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

import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun TransparentCircularProgressBar(isLoading: Boolean) {
    if (!isLoading) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) // Yarı saydam arka plan
            .pointerInput(Unit) {
                // Tüm dokunma olaylarını engelle
                detectTapGestures {}
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp), // Yükleme göstergesinin boyutu
            color = MaterialTheme.colorScheme.secondary, // Yükleme göstergesinin rengi
            strokeWidth = 4.dp // Çizgi kalınlığı
        )
    }
}


@Preview
@Composable
fun SimpleCosmposablePreview() {
    TransparentCircularProgressBar(true)
}
