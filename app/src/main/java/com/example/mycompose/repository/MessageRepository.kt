package com.example.mycompose.repository

import android.util.Log
import com.example.mycompose.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Method to send a new message to a specific ad's private room
    suspend fun sendMessage(adId: String, message: MessageModel) {
        try {
            firestore.collection("message")
                .document(adId) // Use adId as the document ID for each conversation
                .collection("messages") // Reference the messages sub-collection
                .add(message) // Add the message
                .await() // Wait for the operation to complete
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error sending message: ${e.message}")
        }
    }

    // Method to fetch messages in real-time for a specific ad's private room
    fun getMessagesRealtime(adId: String, onMessagesChanged: (List<MessageModel>) -> Unit) {
        firestore.collection("message")
            .document(adId) // Use adId to reference the specific conversation document
            .collection("messages") // Reference the messages sub-collection
            .orderBy("timestamp", Query.Direction.DESCENDING) // Assuming you have a timestamp field
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MessageRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MessageModel::class.java)
                    }
                    onMessagesChanged(messageList)
                }
            }
    }

    // Fetch messages for the current user from their conversations
    suspend fun getMessagesForCurrentUser(): List<MessageModel> {
        val currentUserId = getCurrentUserId()
        val messages = mutableListOf<MessageModel>()

        // Fetch messages from all conversation documents related to the current user
        val snapshot = firestore.collection("message")
            .get()
            .await() // Get all conversation documents

        for (conversation in snapshot.documents) {
            // Fetch messages from each conversation's messages sub-collection
            val messagesSnapshot = firestore.collection("message")
                .document(conversation.id)
                .collection("messages")
                .whereEqualTo("receiverId", currentUserId) // Adjust according to your use case
                .get()
                .await()

            for (doc in messagesSnapshot.documents) {
                val message = doc.toObject(MessageModel::class.java)
                message?.let { messages.add(it) }
            }
        }
        return messages
    }

    // Get the current user ID
    fun getCurrentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid // Returns null if no user is logged in
    }
}
