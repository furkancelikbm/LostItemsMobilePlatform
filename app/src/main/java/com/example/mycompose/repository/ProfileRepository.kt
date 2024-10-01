package com.example.mycompose.repository

import com.example.mycompose.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class ProfileRepository @Inject constructor(){

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    suspend fun getUserProfile(): UserProfile {
        val userId = getCurrentUserId().toString()
        val document = firestore.collection("users").document(userId).get().await()
        return UserProfile(
            userId = userId,
            firstName = document.getString("first_name").orEmpty(),
            lastName = document.getString("last_name").orEmpty(),
            profilePicture = document.getString("profile_picture").orEmpty()
        )
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        val userId = getCurrentUserId().toString()
        firestore.collection("users").document(userId).update(
            "first_name", userProfile.firstName,
            "last_name", userProfile.lastName,
            "profile_picture", userProfile.profilePicture
        ).await()
    }

    suspend fun uploadProfilePicture(uri: Uri): String {
        val userId = getCurrentUserId()
        val storageRef = storage.getReference("profile_pictures/$userId")
        val uploadTask = storageRef.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    suspend fun getUserProfileByAdUserId(userId: String): UserProfile {
        val document = firestore.collection("users").document(userId).get().await()
        return UserProfile(
            userId = userId,
            firstName = document.getString("first_name").orEmpty(),
            lastName = document.getString("last_name").orEmpty(),
            profilePicture = document.getString("profile_picture").orEmpty()
        )
    }


    fun getCurrentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid // Returns null if no user is logged in
    }
}
