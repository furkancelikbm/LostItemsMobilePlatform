package com.example.mycompose.view.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.mycompose.navigation.NavigationViewModel

@Composable
fun AppScaffold(
    startingScreen: String,
    showBottomBar: Boolean = true,
    navController: NavController,
    navigationViewModel: NavigationViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController, viewModel = navigationViewModel)
            }
        },
        content = content
    )
}





