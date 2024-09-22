package com.example.mycompose.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NavigationViewModel:ViewModel() {

    private val _selectedItemIndex= mutableStateOf(0)
    val selectedItemIndex: State<Int> = _selectedItemIndex

    fun onNavigationItemSelected(index:Int){
        _selectedItemIndex.value=index
    }
}