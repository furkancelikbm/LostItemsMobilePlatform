package com.example.mycompose.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CreateAdScreenViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val adRepository: AdRepository = AdRepository()
) : ViewModel() {

    var adModel = mutableStateOf(AdModel("", "", "", "",listOf(),"", "", System.currentTimeMillis()))
        private set

    var userProfile = mutableStateOf(UserProfile())
        private set

    var selectedImages = mutableStateListOf<Pair<Uri, String>>()
        private set

    var showError = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf("")
        private set

    var isLoading = mutableStateOf(false)
        private set

    var locationId = mutableStateOf("")
        private set

    var successMessage by mutableStateOf("")
        private set

    val locationInputFieldViewModel = LocationInputFieldViewModel()

    var selectedCategory = mutableStateOf("")
    


    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            userProfile.value = profileRepository.getUserProfile()
        }
    }

    fun addImage(uri: Uri) {
        if (selectedImages.size < 5) {
            selectedImages.add(Pair(uri, UUID.randomUUID().toString()))
        }
    }

    fun removeImage(uniqueId: String) {
        selectedImages.removeAll { it.second == uniqueId }
    }

    fun submitAd(function: () -> Unit) {
        val model = adModel.value
        val profile = userProfile.value


        when {
            model.title.isBlank() -> {
                errorMessage.value = "Title cannot be empty."
                showError.value = true
            }
            model.description.isBlank() -> {
                errorMessage.value = "Description cannot be empty."
                showError.value = true
            }
            locationInputFieldViewModel.pickUp.text.isBlank() -> {
                errorMessage.value = "Location cannot be empty."
                showError.value = true
            }
            selectedImages.isEmpty() -> {
                errorMessage.value = "You must add at least one photo."
                showError.value = true
            }
            locationId.value.isEmpty() || locationId.value == "0" -> {
                errorMessage.value = "Please select a valid location."
                showError.value = true
            }
            else -> {
                showError.value = false
                isLoading.value = true
                viewModelScope.launch {
                    try {
                        val imageUrls = selectedImages.map { (uri, _) ->
                            adRepository.uploadImage(uri)
                        }
                        val newAd = adModel.value.copy(
                            id = UUID.randomUUID().toString(),
                            userId = profile.userId,
                            imageUrls = imageUrls,
                            location = locationInputFieldViewModel.pickUp.text,
                            locationId = locationId.value,
                            category = selectedCategory.value
                        )
                        adRepository.addAd(newAd)

                        // Reset values after submission
                        resetFields()
                    } catch (e: Exception) {
                        errorMessage.value = "Error adding ad: ${e.localizedMessage}"
                        showError.value = true
                    } finally {
                        isLoading.value = false
                        successMessage = "created ad successfully!"
                        function()
                    }
                }
            }
        }
    }

    private fun resetFields() {
        adModel.value = AdModel("", "", "", "", listOf(), "", "", System.currentTimeMillis())
        locationId.value = ""
        locationInputFieldViewModel.onPickUpValueChanged(TextFieldValue(""))
        selectedImages.clear()
    }
}
