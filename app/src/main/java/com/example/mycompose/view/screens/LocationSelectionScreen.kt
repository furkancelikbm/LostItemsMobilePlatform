package com.example.mycompose.view.screens

import android.util.Log
import android.view.View
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
import com.example.mycompose.model.LocationStates

@Composable
fun LocationSelectionScreen(
    navController: NavController) {

    val stateList = remember { mutableStateOf<List<LocationStates>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") } // State for the search text

    // Fetch data from Firebase
    LaunchedEffect(Unit) {
        fetchDataFromFirebase(stateList, isLoading)
    }

    // Screen Content
    Column {
        // Search Bar
        Row(
            modifier=Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick ={navController.popBackStack()} ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack ,
                    contentDescription = "Back" )
            }
            SearchBar(
                searchText = searchText,
                onSearchTextChanged = { searchText = it }
            )
        }



        // Show Loading or Filtered and Sorted List
        if (isLoading.value) {
            LoadingScreen()
        } else {
            val filteredAndSortedList = stateList.value
                .sortedBy { it.name } // Sort alphabetically
                .filter { it.name.contains(searchText, ignoreCase = true) } // Filter by search text

            LocationList(stateList = filteredAndSortedList, navController = navController)
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
private fun fetchDataFromFirebase(stateList: MutableState<List<LocationStates>>, isLoading: MutableState<Boolean>) {
    val database = FirebaseDatabase.getInstance("https://mycompose-60672-default-rtdb.europe-west1.firebasedatabase.app/")
    val ref = database.getReference("states")
    Log.d("FirebaseDebug", "Firebase reference initialized: $ref")

    try {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Data snapshot received: ${snapshot.childrenCount} items")
                val states = snapshot.children.mapNotNull { stateSnapshot ->
                    try {
                        // Log data type for debugging
                        val name = stateSnapshot.child("name").getValue(Any::class.java)
                        val fipsCode = stateSnapshot.child("fips_code").getValue(Any::class.java)
                        val id = stateSnapshot.child("id").getValue(Any::class.java)
                        val latitude = stateSnapshot.child("latitude").getValue(Any::class.java)
                        val longitude = stateSnapshot.child("longitude").getValue(Any::class.java)

                        // Ensure all fields are cast correctly
                        val nameString = name?.toString()
                        val fipsCodeString = fipsCode?.toString()
                        val idString = id?.toString()
                        val latitudeDouble = latitude?.toString()?.toDoubleOrNull()
                        val longitudeDouble = longitude?.toString()?.toDoubleOrNull()

                        // Only create LocationStates if all fields are valid
                        if (nameString != null && fipsCodeString != null && idString != null &&
                            latitudeDouble != null && longitudeDouble != null) {
                            LocationStates(
                                name = nameString,
                                fips_code = fipsCodeString,
                                id = idString,
                                latitude = latitudeDouble,
                                longitude = longitudeDouble
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error processing snapshot: ${e.message}")
                        null
                    }
                }

                stateList.value = states
                isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
                isLoading.value = false
            }
        })
    } catch (e: Exception) {
        Log.e("FirebaseError", "Exception: ${e.message}")
        isLoading.value = false
    }
}