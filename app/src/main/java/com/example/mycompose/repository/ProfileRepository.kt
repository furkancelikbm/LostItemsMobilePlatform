package com.example.mycompose.repository

import com.example.mycompose.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getUserProfile(): UserProfile {
        val document = firestore.collection("users").document(userId).get().await()
        return UserProfile(
            firstName = document.getString("first_name").orEmpty(),
            lastName = document.getString("last_name").orEmpty(),
            profilePicture = document.getString("profile_picture").orEmpty()
        )
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        firestore.collection("users").document(userId).update(
            "first_name", userProfile.firstName,
            "last_name", userProfile.lastName,
            "profile_picture", userProfile.profilePicture
        ).await()
    }

    suspend fun uploadProfilePicture(uri: Uri): String {
        val storageRef = storage.getReference("profile_pictures/$userId")
        val uploadTask = storageRef.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }
}
