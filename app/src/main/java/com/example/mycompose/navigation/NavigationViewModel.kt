package com.example.mycompose.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

class NavigationViewModel:ViewModel() {

    private val _selectedItemIndex= mutableStateOf(0)
    val selectedItemIndex: State<Int> = _selectedItemIndex

    fun onNavigationItemSelected(index:Int){
        _selectedItemIndex.value=index
    }

    fun onHomeDoubleClick(navController: NavController){
        //Navigate to the Home screen again,forcing a refresh
        navController.navigate("Home"){
            popUpTo(navController.graph.startDestinationId){
                inclusive=true
            }
            launchSingleTop=true
        }
    }
}