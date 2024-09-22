package com.example.mycompose.repository

import android.net.Uri
import android.util.Log
import com.example.mycompose.model.AdModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AdRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun addAd(ad: AdModel) {
        try {
            firestore.collection("ads").document().set(ad).await()
        } catch (e: Exception) {
            // Hata durumu: loglama veya kullanıcıya bildirme
            throw e
        }
    }


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
                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
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
            // Hata durumu: loglama veya kullanıcıya bildirme
            throw e
        }
    }
}
