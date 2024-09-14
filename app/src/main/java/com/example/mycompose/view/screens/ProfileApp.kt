package com.example.mycompose.view.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.viewmodel.ProfileAppViewModel
import com.example.mycompose.navigation.NavigationViewModel
import com.example.mycompose.navigation.Navigation
import com.example.mycompose.view.components.AppScaffold

@Composable
fun ProfileApp(navController: NavController) {
    val navController = rememberNavController()
    val navigationViewModel: NavigationViewModel = viewModel()
    val profileAppViewModel: ProfileAppViewModel = viewModel() // ProfileAppViewModel oluşturuldu
    val context = LocalContext.current

    // Kullanıcı profil verilerini yükleme
    LaunchedEffect(Unit) {
        profileAppViewModel.loadUserProfile(context)
    }

    if (profileAppViewModel.isLoading) {
        // Veri yüklenirken CircularProgressIndicator göster
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // AppScaffold ile navigation ve BottomNavigationBar yönetimi
        AppScaffold(
            startingScreen = profileAppViewModel.startingScreen,
            showBottomBar = profileAppViewModel.showBottomBar,
            navController = navController,
            navigationViewModel = navigationViewModel, // navigationViewModel parametresi geçildi
            content = { paddingValues -> // content parametresi geçildi
                Navigation(
                    navController = navController,
                    startingScreen = profileAppViewModel.startingScreen,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        )
    }
}
