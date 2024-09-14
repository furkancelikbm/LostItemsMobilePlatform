package com.example.mycompose.model

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val title:String,
    val selectedIcon:ImageVector,
    val unSelectedIcon:ImageVector
)
