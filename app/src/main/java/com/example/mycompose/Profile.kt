package com.example.mycompose

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.widgets.ProfileImage
import com.google.firebase.auth.FirebaseAuth

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
        Text(text = "napiyon lan")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen(navController: NavController = rememberNavController()) {
    Profile(navController = navController)
}


