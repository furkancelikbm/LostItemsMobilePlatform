package com.example.mycompose.repository

import android.net.Uri
import com.example.mycompose.model.AdModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AdRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun addAd(ad: AdModel) {
        firestore.collection("ads").document().set(ad).await()
        // İlan Firestore'a kaydedildi
    }

    suspend fun getAds(): List<AdModel> {
        val snapshot = firestore.collection("ads").get().await()
        return snapshot.documents.map { doc ->
            doc.toObject(AdModel::class.java)?.copy(id = doc.id) ?: throw Exception("Failed to parse ad")
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val fileRef = storage.child("AdImages/$fileName")
        fileRef.putFile(uri).await()
        return fileRef.downloadUrl.await().toString()
    }
}
