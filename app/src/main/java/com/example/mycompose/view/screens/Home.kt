package com.example.mycompose.view.screens

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mycompose.model.PhotoItem
import com.example.mycompose.viewmodel.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*

@Composable
fun Home(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val photoItems = viewModel.photoItems.collectAsState(initial = emptyList())
    val isRefreshing = viewModel.isRefreshing.collectAsState(initial = false)


    var searchQuery by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Observe savedStateHandle for selected state and city
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    savedStateHandle?.getLiveData<String>("selectedState")?.observeForever { state ->
        viewModel.selectedState.value = state
    }
    savedStateHandle?.getLiveData<String>("selectedLocation")?.observeForever { city ->
        viewModel.selectedLocation.value = city
    }


    // Filter the photo items based on search query, location, and date
    val filteredItems = photoItems.value.filter {
        it.title.contains(searchQuery, ignoreCase = true) &&
                (viewModel.selectedLocation.value == "All Locations" || it.location == viewModel.selectedLocation.value) &&
                (selectedDate.isEmpty() || it.adDate == selectedDate)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { /* Handle Filter Icon Click */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            )
        }

        // Filter Options Row (Location and Date)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Dropdown
            Box {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("LocationSelectionScreen")
                        }
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                {
                    Text(
                        text = "${viewModel.selectedState.value},${viewModel.selectedLocation.value}"

                    )
                }
            }

            // Date Picker
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    selectedDate = String.format("%02d/%02d/%02d", dayOfMonth, month + 1, year % 100)
                    Log.d("HomeScreen", "Selected date: $selectedDate") // Log the selected date
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )

            Text(
                text = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                modifier = Modifier
                    .clickable { datePickerDialog.show() }
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // SwipeRefresh with LazyVerticalGrid for displaying photos
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing.value),
            onRefresh = { viewModel.refreshPhotos() }) {

            Spacer(modifier = Modifier.height(1.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { ad ->
                    val imageUrl = ad.imageUrls.firstOrNull() ?: ""
                    val title = ad.title
                    PhotoItemView(photoItem = PhotoItem(imageUrl, title = title, id = ad.id)) {
                        navController.navigate("adDetail/${ad.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoItemView(photoItem: PhotoItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column {
            Image(
                painter = rememberImagePainter(photoItem.imageUrl),
                contentDescription = photoItem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = photoItem.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    Home(navController = rememberNavController())
}
