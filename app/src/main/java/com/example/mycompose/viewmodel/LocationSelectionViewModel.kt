package com.example.mycompose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mycompose.model.LocationCities
import com.example.mycompose.model.LocationStates
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationSelectionViewModel : ViewModel() {

    private val _stateList = MutableStateFlow<List<LocationStates>>(emptyList())
    val stateList: StateFlow<List<LocationStates>> = _stateList

    private val _citiesList=MutableStateFlow<List<LocationCities>>(emptyList())
    val citiesList:StateFlow<List<LocationCities>> =_citiesList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        val database =
            FirebaseDatabase.getInstance("https://mycompose-60672-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = database.getReference("states")
        Log.d("FirebaseDebug", "Firebase reference initialized: $ref")

        try {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(
                        "FirebaseDebug",
                        "Data snapshot received: ${snapshot.childrenCount} items"
                    )
                    val states = snapshot.children.mapNotNull { stateSnapshot ->
                        try {
                            // Log data type for debugging
                            val name = stateSnapshot.child("name").getValue(Any::class.java)
                            val fipsCode =
                                stateSnapshot.child("fips_code").getValue(Any::class.java)
                            val id = stateSnapshot.child("id").getValue(Any::class.java)
                            val latitude = stateSnapshot.child("latitude").getValue(Any::class.java)
                            val longitude =
                                stateSnapshot.child("longitude").getValue(Any::class.java)

                            // Ensure all fields are cast correctly
                            val nameString = name?.toString()
                            val fipsCodeString = fipsCode?.toString()
                            val idString = id?.toString()
                            val latitudeDouble = latitude?.toString()?.toDoubleOrNull()
                            val longitudeDouble = longitude?.toString()?.toDoubleOrNull()

                            // Only create LocationStates if all fields are valid
                            if (nameString != null && fipsCodeString != null && idString != null &&
                                latitudeDouble != null && longitudeDouble != null
                            ) {
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

                    _stateList.value = states
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                    _isLoading.value = false
                }
            })
        } catch (e: Exception) {
            Log.e("FirebaseError", "Exception: ${e.message}")
            _isLoading.value = false
        }
    }
}

fun fetchCitiesFromFirebase(
    stateCode: String,
    onSuccess: (List<LocationCities>) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance("https://mycompose-60672-default-rtdb.europe-west1.firebasedatabase.app/")
    val ref = database.getReference("cities").orderByChild("state_code").equalTo(stateCode)

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val cities = snapshot.children.mapNotNull { citySnapshot ->
                try {
                    val name = citySnapshot.child("name").getValue(String::class.java)
                    val latitude = citySnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = citySnapshot.child("longitude").getValue(Double::class.java)
                    val id = citySnapshot.child("id").getValue(Any::class.java) // Use Any to handle different types
                    val stateCode = citySnapshot.child("state_code").getValue(String::class.java)

                    // Convert 'id' field to string if it's a Long or any other type
                    val idString = when (id) {
                        is Long -> id.toString()  // If the ID is a Long, convert it to String
                        is String -> id           // If it's already a String, use it directly
                        else -> null              // If it's neither, return null
                    }

                    // Create a LocationCities object only if all fields are non-null
                    if (name != null && latitude != null && longitude != null && idString != null && stateCode != null) {
                        LocationCities(
                            name = name,
                            latitude = latitude,
                            longitude = longitude,
                            id = idString,   // Use the converted ID
                            state_code = stateCode
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseError", "Error parsing city data: ${e.message}")
                    null
                }
            }

            // Notify the caller with the fetched cities
            onSuccess(cities)
        }

        override fun onCancelled(error: DatabaseError) {
            // Log the error and notify the caller
            Log.e("FirebaseError", "Error fetching cities: ${error.message}")
            onError(error.message)
        }
    })

}
