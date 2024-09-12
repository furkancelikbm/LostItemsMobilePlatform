package com.example.mycompose.NavigationBar

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val title:String,
    val selectedIcon:ImageVector,
    val unSelectedIcon:ImageVector
)
