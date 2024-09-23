package com.example.mycompose.viewmodel

import android.util.Log
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

    init {
        fetchAds()
    }

    private fun fetchAds() {
        viewModelScope.launch {
            try {
                val ads = adRepository.getAds()
                _photoItems.value = ads
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching ads", e)
            }
        }
    }
}
