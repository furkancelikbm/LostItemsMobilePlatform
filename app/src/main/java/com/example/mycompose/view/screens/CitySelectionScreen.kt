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
import com.example.mycompose.model.LocationCities
import com.example.mycompose.viewmodel.fetchCitiesFromFirebase
import com.google.firebase.database.*

@Composable
fun CitySelectionScreen(
    navController: NavController,
    stateCode: String,
    stateName: String,
    selectedLocation: MutableState<String>
) {
    var cityList by remember { mutableStateOf<List<LocationCities>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch cities for the given state code
    LaunchedEffect(stateCode) {
        fetchCitiesFromFirebase(stateCode, { cities ->
            cityList = cities
            isLoading = false
        }, { error ->
            // Handle errors
            Log.e("FirebaseError", "Error fetching cities: $error")
            isLoading = false
        })
    }

    // Screen Content
    Column(modifier = Modifier.fillMaxSize()) {
        // Back Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        // Display loading screen or city list
        if (isLoading) {
            LoadingScreen() // Replace with your custom loading indicator
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(cityList) { city ->
                    CityItem(
                        city = city,
                        onCityClick = {
                            selectedLocation.value = city.name

                            // Log the selected city for debugging
                            Log.d("CitySelection", "City clicked: ${city.name}")

                            // Save selected state, city, latitude, and longitude into savedStateHandle
                            navController.previousBackStackEntry?.savedStateHandle?.apply {
                                set("selectedLocation", city.name)
                                set("selectedState", stateName)
                                set("latitude", city.latitude)
                                set("longitude", city.longitude)
                            }


                            // Log the savedStateHandle update
                            Log.d("CitySelection", "Saved state handle updated with: $stateName")
                            Log.d("CitySelection", "longi lati cityselection ${city.latitude} ${city.longitude}")


                            // Pop back to the Home screen
                            navController.popBackStack("Home", inclusive = false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CityItem(city: LocationCities, onCityClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCityClick() }
            .padding(8.dp)
    ) {
        Text(
            text = city.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Longitude: ${city.longitude}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Latitude: ${city.latitude}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

