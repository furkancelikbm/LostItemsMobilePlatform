package com.example.mycompose.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.AdModel
import com.example.mycompose.repository.AdRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val adRepository: AdRepository) : ViewModel() {

    private val _photoItems = MutableStateFlow<List<AdModel>>(emptyList())
    val photoItems = _photoItems.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var selectedLocation = mutableStateOf("All Locations")
        private set
    var selectedState = mutableStateOf("")
        private set

    init {
        fetchAds()
    }

    private fun fetchAds() {
        viewModelScope.launch {
            _isRefreshing.value=true
            try {
                val ads = adRepository.getAds()
                _photoItems.value = ads
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching ads", e)
            }
            finally {
                _isRefreshing.value=false //set loading to false
            }
        }
    }

    // Reload ads
    fun refreshPhotos() {
        fetchAds() // Call fetchAds to refresh the data
    }
}
