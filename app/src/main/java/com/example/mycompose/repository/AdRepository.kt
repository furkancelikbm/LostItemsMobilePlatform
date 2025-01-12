package com.example.mycompose.repository

import android.net.Uri
import android.util.Log
import com.example.mycompose.model.AdModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AdRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()  // To get current user ID

    suspend fun addAd(ad: AdModel) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val formattedDate = dateFormat.format(System.currentTimeMillis())

            val adWithDate = ad.copy(adDate = formattedDate)
            firestore.collection("ads").document().set(adWithDate).await()
        } catch (e: Exception) {
            Log.e("AdRepository", "Error adding ad: ${e.message}")
            throw e
        }
    }

    // Fetch all ads
    suspend fun getAds(): List<AdModel> {
        val snapshot = firestore.collection("ads").get().await()
        return snapshot.documents.map { doc ->
            val data = doc.data ?: throw Exception("Missing data")
            AdModel(
                id = doc.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                location = data["location"] as? String ?: "",
                imageUrls = data["imageUrls"] as? List<String> ?: listOf(),
                userId = data["userId"] as? String ?: "",
                locationId = data["locationId"] as? String ?: "",
                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
                adDate = data["adDate"] as? String ?: "",
                latitude = data["latitude"] as? Double,
                longitude = data["longitude"] as? Double
            )
        }
    }

    // Fetch only the current user's ads by filtering based on the userId
    suspend fun getUserAds(): List<AdModel> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()  // Get current user ID
        val snapshot = firestore.collection("ads")
            .whereEqualTo("userId", currentUserId)  // Query ads by the current user ID
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val data = doc.data ?: throw Exception("Missing data")
            AdModel(
                id = doc.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                location = data["location"] as? String ?: "",
                imageUrls = data["imageUrls"] as? List<String> ?: listOf(),
                userId = data["userId"] as? String ?: "",
                locationId = data["locationId"] as? String ?: "",
                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
                adDate = data["adDate"] as? String ?: "",
                latitude = data["latitude"] as? Double,
                longitude = data["longitude"] as? Double
            )
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        return try {
            val fileName = "${System.currentTimeMillis()}.jpg"
            val fileRef = storage.child("AdImages/$fileName")
            fileRef.putFile(uri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAdById(adId: String): AdModel? {
        val document = firestore.collection("ads").document(adId).get().await()
        val data = document.data ?: return null
        return AdModel(
            id = document.id,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            location = data["location"] as? String ?: "",
            imageUrls = data["imageUrls"] as? List<String> ?: listOf(),
            userId = data["userId"] as? String ?: "",
            locationId = data["locationId"] as? String ?: "",
            timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
            adDate = data["adDate"] as? String ?: "",
            latitude = data["latitude"] as? Double,
            longitude = data["longitude"] as? Double
        )
    }
}
