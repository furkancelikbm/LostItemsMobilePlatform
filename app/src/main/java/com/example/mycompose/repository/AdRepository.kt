package com.example.mycompose.repository

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mycompose.model.AdModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AdRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    var isLoading by mutableStateOf(true)

    suspend fun addAd(ad: AdModel, userId: String) {
        isLoading = true
        try {
            // Create a document reference to get the ID
            val documentRef = firestore.collection("ads").document()

            // Set the ad with userId and the document ID
            val adWithId = ad.copy(userId = userId, id = documentRef.id)
            documentRef.set(adWithId).await()
        } finally {
            isLoading = false
        }
    }

    suspend fun getAds(): List<AdModel> {
        try {
            val snapshot = firestore.collection("ads").get().await()
            if (!snapshot.isEmpty) {
                return snapshot.documents.map { doc ->
                    doc.toObject(AdModel::class.java)?.copy(id = doc.id) ?: throw Exception("Failed to parse ad")
                }
            } else {
                // Handle empty result scenario
                return emptyList()
            }
        } catch (e: Exception) {
            // Handle exception
            throw e
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val fileRef = storage.child("AdImages/$fileName")
        fileRef.putFile(uri).await()
        return fileRef.downloadUrl.await().toString()
    }
}
