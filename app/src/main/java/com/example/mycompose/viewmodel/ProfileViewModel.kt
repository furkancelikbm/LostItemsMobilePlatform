package com.example.mycompose.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    var loading by mutableStateOf(false)
    var userProfile = mutableStateOf(UserProfile())
    var newPicUri = mutableStateOf<Uri?>(null)
    var errorMessage = mutableStateOf("")
    var successMessage by mutableStateOf("") // Add this line


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
