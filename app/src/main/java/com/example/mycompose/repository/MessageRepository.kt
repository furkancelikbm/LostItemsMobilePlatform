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

    // Mesaj gönderme metodu
    suspend fun sendMessage(adId: String, receiverId: String, content: String) {
        val senderId = getCurrentUserId() ?: return
        val roomId = createRoomId(adId, senderId, receiverId) // Oda ID'sini oluştur

        // Yeni mesaj oluştur
        val message = MessageModel(
            senderId = senderId,
            receiverId = receiverId,
            messageContent = content,
            adId = adId,
            roomId = roomId
        )

        try {
            // Mesajı özel odanın alt koleksiyonuna ekle
            firestore.collection("messages")
                .document(roomId)
                .collection("messages")
                .add(message)
                .await()

            // Oda dökümanını son mesaj ve zaman damgasıyla güncelle veya oluştur
            val roomData = mapOf(
                "lastMessage" to content,
                "lastMessageTimestamp" to System.currentTimeMillis()
            )

            firestore.collection("messages")
                .document(roomId)
                .set(roomData, SetOptions.merge()) // Belge yoksa oluştur, varsa güncelle
                .await()

            Log.d("MessageRepository", "Mesaj başarıyla gönderildi: $content")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Mesaj gönderilirken hata oluştu: ${e.message}")
        }
    }

    // Mesajları gerçek zamanlı olarak alma metodu
    fun getMessagesRealtime(adId: String, receiverId: String, onMessagesChanged: (List<MessageModel>) -> Unit) {
        val senderId = getCurrentUserId() ?: return
        val roomId = createRoomId(adId, senderId, receiverId) // Oda ID'sini oluştur

        firestore.collection("messages")
            .document(roomId) // Oda ID'si
            .collection("messages") // Mesajlar alt koleksiyonu
            .orderBy("timestamp", Query.Direction.DESCENDING) // Zaman damgasına göre sıralama
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MessageRepository", "Mesajlar dinlenirken hata oluştu.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MessageModel::class.java)
                    }
                    onMessagesChanged(messageList)
                    Log.d("MessageRepository", "Mesajlar başarıyla alındı: $messageList")
                } else {
                    Log.d("MessageRepository", "Mesaj bulunamadı.")
                }
            }
    }

    // Kullanıcıyla ilgili tüm odalardan son mesajları al
    suspend fun getRoomsForCurrentUser(): List<Pair<MessageModel, UserProfile>> {
        val currentUserId = getCurrentUserId() ?: return emptyList()
        val rooms = mutableMapOf<String, Pair<MessageModel, UserProfile>>() // Oda ID'sine göre benzersiz odalar

        try {
            // Kullanıcının gönderdiği veya aldığı mesajları al
            val messageSnapshots = firestore.collectionGroup("messages")
                .whereEqualTo("senderId", currentUserId)
                .get()
                .await()
                .documents +
                    firestore.collectionGroup("messages")
                        .whereEqualTo("receiverId", currentUserId)
                        .get()
                        .await()
                        .documents

            Log.d("MessageRepository", "Toplam mesaj sayısı: ${messageSnapshots.size}")

            // Her mesaj için odaları belirle
            for (doc in messageSnapshots) {
                val message = doc.toObject(MessageModel::class.java) ?: continue
                val roomId = message.roomId ?: continue

                // Eğer oda daha önce eklenmemişse
                if (!rooms.containsKey(roomId)) {
                    // Alıcıyı al
                    val receiverId = if (message.senderId == currentUserId) message.receiverId else message.senderId
                    val receiverProfile = firestore.collection("users")
                        .document(receiverId)
                        .get()
                        .await()
                        .toObject(UserProfile::class.java)

                    if (receiverProfile != null) {
                        rooms[roomId] = Pair(message, receiverProfile) // Oda ve alıcı profili ekle
                        Log.d("MessageRepository", "Oda eklendi: $roomId, Alıcı: $receiverId")
                    } else {
                        Log.e("MessageRepository", "Alıcı profili bulunamadı: $receiverId")
                    }
                } else {
                    // Oda zaten mevcutsa, sadece son mesajı güncelle
                    val existingMessage = rooms[roomId]?.first
                    if (existingMessage == null || message.timestamp > existingMessage.timestamp) {
                        rooms[roomId] = Pair(message, rooms[roomId]!!.second) // Son mesajı güncelle
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Odalar getirilirken hata oluştu: ${e.message}")
        }

        return rooms.values.toList() // Benzersiz odaları döner
    }

    // Şu anki kullanıcı ID'sini alma
    private fun getCurrentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("MessageRepository", "Kullanıcı giriş yapmamış!")
        }
        return currentUser?.uid // Eğer kullanıcı giriş yapmamışsa null döner
    }

    private fun createRoomId(adId: String, userId1: String, userId2: String): String {
        // Sort user IDs to maintain consistency
        val sortedUserIds = listOf(userId1, userId2).sorted()
        return "${adId}-${sortedUserIds[0]}-${sortedUserIds[1]}"
    }

}
