package com.example.mycompose

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var loading by mutableStateOf(false)
        private set
    val firstName = mutableStateOf("")
    val lastName = mutableStateOf("")
    val editFirstName = mutableStateOf("")
    val editLastName = mutableStateOf("")
    val picUrl = mutableStateOf("")
    val newPicUri = mutableStateOf<Uri?>(null)
    val editMode = mutableStateOf(false)
    val errorMessage = mutableStateOf("")
    val isProfileFilled = mutableStateOf(false)
    val profileLoaded = mutableStateOf(false)

    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    fun loadUserProfile(context: Context, navController: NavController, homeScreen: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                firstName.value = document.getString("first_name").orEmpty()
                lastName.value = document.getString("last_name").orEmpty()
                picUrl.value = document.getString("profile_picture").orEmpty()

                // Pre-fill edit fields with existing data if available
                editFirstName.value = firstName.value
                editLastName.value = lastName.value

                isProfileFilled.value = firstName.value.isNotEmpty() && lastName.value.isNotEmpty() && picUrl.value.isNotEmpty()
                profileLoaded.value = true // Mark profile as loaded

                if (isProfileFilled.value) {
                    navController.navigate(homeScreen) {
                        popUpTo("Profile") { inclusive = true }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error checking profile status.", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SuspiciousIndentation")
    fun saveProfileData(context: Context, navController: NavController, homeScreen: String) {
        loading = true //progress bar circle için yazdım
            if (newPicUri.value != null) {
                updateProfilePicture(newPicUri.value!!) { updatedPicUrl ->
                    picUrl.value = updatedPicUrl
                    updateUserData(context, updatedPicUrl, navController, homeScreen)
                }
            } else {
                updateUserData(context, picUrl.value, navController, homeScreen)
            }

    }

    private fun updateUserData(context: Context, profilePicUrl: String, navController: NavController, homeScreen: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update(
                "first_name", editFirstName.value,
                "last_name", editLastName.value,
                "profile_picture", profilePicUrl
            )
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                loading = false // Stop showing progress bar
                navController.navigate("ProfileApp") {
                    popUpTo("Profile") { inclusive = true } // Update this as needed
                    // Ensure Home screen is not recreated if already at the top
                    launchSingleTop = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateProfilePicture(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/$userId")
        val uploadTask = storageRef.putFile(uri)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }
        }
    }
}
