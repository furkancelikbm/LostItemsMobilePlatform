package com.example.mycompose.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: ProfileRepository , private  val adRepository: AdRepository) : ViewModel() {
    var loading by mutableStateOf(false)
    var userProfile = mutableStateOf(UserProfile())
    var newPicUri = mutableStateOf<Uri?>(null)
    var errorMessage = mutableStateOf("")
    var successMessage by mutableStateOf("") // Add this line
    // In ProfileViewModel
    var userAds = mutableStateOf<List<AdModel>>(emptyList())

    // Add this to load user ads (you can call this from the `loadUserProfile` function)
    fun loadUserAds() {
        viewModelScope.launch {
            try {
                userAds.value = adRepository.getUserAds() // Fetch ads from repository
            } catch (e: Exception) {
                errorMessage.value = "Error loading ads"
            }
        }
    }



    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                userProfile.value = repository.getUserProfile()
            } catch (e: Exception) {
                errorMessage.value = "Error loading profile"
            }
        }
    }


    fun saveProfileData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            loading = true
            try {
                val updatedProfile = userProfile.value.copy(
                    profilePicture = newPicUri.value?.let { uri -> repository.uploadProfilePicture(uri) }
                        ?: userProfile.value.profilePicture
                )
                repository.updateUserProfile(updatedProfile)
                // Handle success (e.g., navigate to another screen)
            } catch (e: Exception) {
                errorMessage.value = "Failed to update profile"
            } finally {
                loading = false
                successMessage = "Profile updated successfully!"
                onSuccess()

            }
        }
    }
}
