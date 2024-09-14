package com.example.mycompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mycompose.view.screens.Home
import com.example.mycompose.view.screens.Login
import com.example.mycompose.view.screens.Profile
import com.example.mycompose.view.screens.ProfileApp
import com.example.mycompose.view.screens.Register
import com.example.mycompose.ui.CompleteProfileScreen

@Composable
fun Navigation(navController: NavHostController,startingScreen: String,modifier: Modifier){
    NavHost(
        navController = navController,
        startDestination = startingScreen,
        modifier=Modifier
    ) {

        composable(Screens.Profile.name) {
            Profile(navController = navController)
        }

        composable(Screens.Login.name){
            Login(navController = navController)
        }

        composable(Screens.Register.name){
            Register(navController = navController)
        }
        composable(Screens.Home.name){
            Home(navController=navController)
        }

        composable(Screens.CompleteProfileScreen.name){
            CompleteProfileScreen(navController=navController)
        }

        composable(Screens.ProfileApp.name){
            ProfileApp(navController=navController)
        }


    }
}
