package com.example.mycompose.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun Profile(navController: NavController) {
    val context = LocalContext.current
    // Display progress bar if profile is loading

    Column(
        modifier = Modifier
            .fillMaxSize(), // Fill the entire screen
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        Text(text = "Bakim asamasinda")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen(navController: NavController = rememberNavController()) {
    Profile(navController = navController)
}


