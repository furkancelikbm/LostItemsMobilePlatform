package com.example.mycompose.navigation

import AdDetailScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mycompose.viewmodel.LocationInputFieldViewModel
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import com.example.mycompose.view.screens.Home
import com.example.mycompose.view.screens.Login
import com.example.mycompose.view.screens.Profile
import com.example.mycompose.view.screens.ProfileApp
import com.example.mycompose.view.screens.Register
import com.example.mycompose.ui.CompleteProfileScreen
import com.example.mycompose.view.screens.CreateAdScreen
import com.example.mycompose.view.screens.MessageBoxScreen
import com.example.mycompose.view.screens.MessageScreen
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    startingScreen: String,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = startingScreen,
        modifier = modifier
    ) {
        composable(Screens.Profile.name) {
            Profile(navController = navController)
        }
        composable(Screens.Login.name) {
            Login(navController = navController)
        }
        composable(Screens.Register.name) {
            Register(navController = navController)
        }
        composable(Screens.Home.name) {
            Home(navController = navController)
        }
        composable(Screens.CompleteProfileScreen.name) {
            CompleteProfileScreen(navController = navController)
        }
        composable(Screens.ProfileApp.name) {
            ProfileApp(navController = navController)
        }
        composable(Screens.CreateAdScreen.name) {
            CreateAdScreen(navController = navController)
        }
        composable(Screens.MessageBoxScreen.name) {
            MessageBoxScreen(navController = navController,
                messageRepository = MessageRepository())
        }
        composable("adDetail/{adId}") {backStackEntry->
            val adId=backStackEntry.arguments?.getString("adId")?:""

            AdDetailScreen(
                navController = navController,
                adId = adId,
                adRepository = AdRepository(),
                profileRepository = ProfileRepository()
            )
        }
        composable("message/{adId}/{senderId}/{receiverId}") { backStackEntry ->
            val adId = backStackEntry.arguments?.getString("adId") ?: ""
            val senderId = backStackEntry.arguments?.getString("senderId") ?: ""
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""

            MessageScreen(
                navController = navController,
                adId = adId,
                senderId = senderId,
                receiverId = receiverId,
                messageRepository = MessageRepository(),
                profileRepository =ProfileRepository()
            )
        }




    }
}
