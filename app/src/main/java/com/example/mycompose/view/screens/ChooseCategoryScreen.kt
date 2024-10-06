package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mycompose.viewmodel.CreateAdScreenViewModel

@Composable
fun ChooseCategoryScreen(
    navController: NavHostController,
    viewModel: CreateAdScreenViewModel = viewModel()
) {
    val categories = listOf("Electronics", "Furniture", "Vehicles", "Clothing") // Sample categories
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Choose a Category", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(categories) { category -> // Ensure 'category' is of String type
                Text(
                    text = category,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            viewModel.selectedCategory.value = category // Set selected category
                            Log.d("ChooseCategoryScreen", "Category selected: $category") // Log selected category
                            Log.d("ChooseCategoryScreen", "Category selected: ${viewModel.selectedCategory.value}") // Log selected category
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedCategory", category)
                            navController.popBackStack()
                        },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

