package com.example.mycompose.repository

import android.util.Log
import com.example.mycompose.model.MessageModel
import com.example.mycompose.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Send message method
    suspend fun sendMessage(adId: String, receiverId: String, content: String) {
        val senderId = getCurrentUserId() ?: return
        val roomId = createRoomId(adId, senderId, receiverId)

        val message = createMessage(senderId, receiverId, content, adId, roomId)

        try {
            // Add message to the messages collection
            addMessageToRoom(roomId, message)

            // Update room document with the last message details
            updateRoomWithLastMessage(roomId, content)

            Log.d("MessageRepository", "Message sent successfully: $content")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error sending message: ${e.message}")
        }
    }

    // Get messages in real-time
    fun getMessagesRealtime(adId: String, receiverId: String, onMessagesChanged: (List<MessageModel>) -> Unit) {
        val senderId = getCurrentUserId() ?: return
        val roomId = createRoomId(adId, senderId, receiverId)

        firestore.collection("messages")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MessageRepository", "Error listening for messages.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MessageModel::class.java)
                    }
                    onMessagesChanged(messageList)
                    Log.d("MessageRepository", "Messages retrieved: $messageList")
                } else {
                    Log.d("MessageRepository", "No messages found.")
                }
            }
    }

    // Get rooms and last message data for current user
    suspend fun getRoomsForCurrentUser(
        pageSize: Int = 50,
        lastVisibleMessage: MessageModel? = null
    ): List<Pair<MessageModel, UserProfile>> {

        val currentUserId = getCurrentUserId() ?: return emptyList()
        val rooms = mutableMapOf<String, Pair<MessageModel, UserProfile>>()

        try {
            val messageSnapshots = getMessagesForUser(currentUserId, pageSize, lastVisibleMessage)

            messageSnapshots.forEach { doc ->
                val message = doc.toObject(MessageModel::class.java) ?: return@forEach
                val roomId = message.roomId ?: return@forEach

                if (!rooms.containsKey(roomId)) {
                    val receiverId = if (message.senderId == currentUserId) message.receiverId else message.senderId
                    val receiverProfile = getUserProfile(receiverId)

                    receiverProfile?.let {
                        rooms[roomId] = Pair(message, it)
                        Log.d("MessageRepository", "Room added: $roomId, Receiver: $receiverId")
                    } ?: Log.e("MessageRepository", "Receiver profile not found: $receiverId")
                } else {
                    val existingMessage = rooms[roomId]?.first
                    if (existingMessage == null || message.timestamp > existingMessage.timestamp) {
                        rooms[roomId] = Pair(message, rooms[roomId]!!.second)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error fetching rooms: ${e.message}")
        }

        return rooms.values.toList()
    }

    // Get current user's ID
    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid.also {
            if (it == null) {
                Log.e("MessageRepository", "User is not logged in!")
            }
        }
    }

    // Create unique room ID
    private fun createRoomId(adId: String, userId1: String, userId2: String): String {
        val sortedUserIds = listOf(userId1, userId2).sorted()
        return "${adId}-${sortedUserIds[0]}-${sortedUserIds[1]}"
    }

    // Create a message model
    private fun createMessage(senderId: String, receiverId: String, content: String, adId: String, roomId: String): MessageModel {
        return MessageModel(
            senderId = senderId,
            receiverId = receiverId,
            messageContent = content,
            adId = adId,
            roomId = roomId
        )
    }

    // Add message to Firestore
    private suspend fun addMessageToRoom(roomId: String, message: MessageModel) {
        firestore.collection("messages")
            .document(roomId)
            .collection("messages")
            .add(message)
            .await()
    }

    // Update the room document with the last message and timestamp
    private suspend fun updateRoomWithLastMessage(roomId: String, lastMessage: String) {
        val roomData = mapOf(
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        firestore.collection("messages")
            .document(roomId)
            .set(roomData, SetOptions.merge())
            .await()
    }

    // Get all messages for a user
    private suspend fun getMessagesForUser(
        currentUserId: String,
        pageSize: Int,
        lastVisibleMessage: MessageModel?
    ): List<com.google.firebase.firestore.DocumentSnapshot> {

        val query = firestore.collectionGroup("messages")
            .whereEqualTo("receiverId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())

        lastVisibleMessage?.let {
            query.startAfter(it.timestamp)
        }

        return query.get().await().documents
    }

    // Get user profile for a given user ID
    private suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error fetching user profile for $userId: ${e.message}")
            null
        }
    }
}
