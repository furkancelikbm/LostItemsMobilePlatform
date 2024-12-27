package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mycompose.viewmodel.LocationSelectionViewModel
import com.example.mycompose.model.LocationStates
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LocationSelectionScreen(navController: NavController, viewModel: LocationSelectionViewModel = viewModel()) {
    val stateList by viewModel.stateList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Column {
        // Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            SearchBar(searchText = searchText, onSearchTextChanged = { searchText = it })
        }

        // Display Loading or List
        if (isLoading) {
            LoadingScreen()
        } else {
            val filteredAndSortedList = stateList
                .filter { it.name.contains(searchText, ignoreCase = true) }
                .sortedBy { it.name }

            if (filteredAndSortedList.isEmpty()) {
                Text(
                    text = "No locations found",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LocationList(stateList = filteredAndSortedList, navController = navController)
            }
        }
    }
}

@Composable
fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        label = { Text("Search Location") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        singleLine = true
    )
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Yükleniyor...")
    }
}

@Composable
fun LocationList(stateList: List<LocationStates>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(stateList) { state ->
            StateItem(state = state, navController = navController)
        }
    }
}

@Composable
fun StateItem(state: LocationStates, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("citySelection/${state.fips_code}/${state.name}")
            },
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
