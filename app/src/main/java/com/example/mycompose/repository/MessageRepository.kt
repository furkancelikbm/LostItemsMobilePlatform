package com.example.mycompose.repository

import android.util.Log
import com.example.mycompose.model.MessageModel
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
            roomId = roomId)

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
                }
            }
    }


    // Şu anki kullanıcı ID'sini alma
    private fun getCurrentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid // Eğer kullanıcı giriş yapmamışsa null döner
    }

    // Özel oda ID'sini oluşturma metodu
    private fun createRoomId(adId: String, userId1: String, userId2: String): String {

        return "$adId-$userId1-$userId2"
    }
}
