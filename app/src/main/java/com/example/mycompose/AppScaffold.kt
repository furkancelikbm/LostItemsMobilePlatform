package com.example.mycompose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.NavigationBar.BottomNavigationBar
import com.example.mycompose.NavigationBar.NavigationViewModel
import com.example.mycompose.navigation.Navigation

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





