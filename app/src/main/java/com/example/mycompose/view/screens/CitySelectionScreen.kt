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
import com.google.firebase.database.*

@Composable
fun CitySelectionScreen(
    navController: NavController,
    stateCode: String,
    stateName:String,
    selectedLocation: MutableState<String>
) {
    var cityList by remember { mutableStateOf<List<String>>(emptyList()) }
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
            LoadingScreen() // You can replace this with your own loading screen composable
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(cityList) { city ->
                    Text(
                        text = city,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                // Set the selected city and pass it back to the Home screen
                                selectedLocation.value = city

                                // Log the selected city for debugging
                                Log.d("CitySelection", "City clicked: $city")

                                // Save selected state and city into savedStateHandle
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("selectedLocation", city)
                                    set("selectedState", stateName)
                                }

                                // Log the savedStateHandle update
                                Log.d("CitySelection", "Saved state handle updated with: $stateName")

                                // Pop back to the Home screen
                                navController.popBackStack("Home", inclusive = false)
                            },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun fetchCitiesFromFirebase(
    stateCode: String,
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance("https://mycompose-60672-default-rtdb.europe-west1.firebasedatabase.app/")
    val ref = database.getReference("cities").orderByChild("state_code").equalTo(stateCode)

    ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val cities = snapshot.children.mapNotNull { citySnapshot ->
                citySnapshot.child("name").getValue(String::class.java)
            }
            // Notify success with the list of cities
            onSuccess(cities)
        }

        override fun onCancelled(error: DatabaseError) {
            // Notify error if fetching fails
            onError(error.message)
        }
    })
}
