package com.example.mycompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.LocationStates
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationSelectionViewModel : ViewModel() {

    private val _stateList = MutableStateFlow<List<LocationStates>>(emptyList())
    val stateList: StateFlow<List<LocationStates>> = _stateList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        val database = FirebaseDatabase.getInstance("https://mycompose-60672-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = database.getReference("states")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val states = snapshot.children.mapNotNull { stateSnapshot ->
                    val name = stateSnapshot.child("name").getValue(String::class.java)
                    val fipsCode = stateSnapshot.child("fips_code").getValue(String::class.java)
                    val id = stateSnapshot.child("id").getValue(String::class.java)
                    val latitude = stateSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = stateSnapshot.child("longitude").getValue(Double::class.java)

                    if (name != null && fipsCode != null && id != null && latitude != null && longitude != null) {
                        LocationStates(name, fipsCode, id, latitude, longitude)
                    } else null
                }

                viewModelScope.launch {
                    _stateList.emit(states)
                    _isLoading.emit(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                viewModelScope.launch {
                    _isLoading.emit(false)
                }
            }
        })
    }
}
