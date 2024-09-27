package com.example.mycompose.repository

import android.util.Log
import com.example.mycompose.model.MessageModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Method to send a new message
    suspend fun sendMessage(adId: String, message: MessageModel) {
        val db = FirebaseFirestore.getInstance()
        firestore.collection("messages").document().set(message).await()
        val messageRef = db.collection("ads").document(adId).collection("messages")
        messageRef.add(message)
    }

    // Example method to fetch messages in real-time
    fun getMessagesRealtime(adId: String, onMessagesChanged: (List<MessageModel>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("ads").document(adId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Assuming you have a timestamp field
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MessageRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messageList = snapshot.documents.map { doc ->
                        doc.toObject(MessageModel::class.java)!!
                    }
                    onMessagesChanged(messageList)
                }
            }
    }
}
