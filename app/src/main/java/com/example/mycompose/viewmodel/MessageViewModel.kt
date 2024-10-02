package com.example.mycompose.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.MessageModel
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.launch


class MessageViewModel(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // State variables
    val messages = mutableStateOf<List<MessageModel>>(emptyList())
    val senderProfile = mutableStateOf("")
    val receiverProfile = mutableStateOf("")
    val senderName = mutableStateOf("")
    val receiverName = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)
    val messageContent = mutableStateOf("")

    // Fetch messages, sender, and receiver profile data
    fun fetchChatData(adId: String, receiverId: String, senderId: String) {
        viewModelScope.launch {
            try {
                // Fetch messages from repository
                messageRepository.getMessagesRealtime(adId, receiverId) { updatedMessages ->
                    messages.value = updatedMessages
                }

                // Fetch sender and receiver profile data
                val senderProfileData = profileRepository.getUserProfileByAdUserId(senderId)
                val receiverProfileData = profileRepository.getUserProfileByAdUserId(receiverId)

                senderProfile.value = senderProfileData.profilePicture
                receiverProfile.value = receiverProfileData.profilePicture
                senderName.value = "${senderProfileData.firstName} ${senderProfileData.lastName}"
                receiverName.value = "${receiverProfileData.firstName} ${receiverProfileData.lastName}"
            } catch (e: Exception) {
                errorMessage.value = "Error loading chat data: ${e.message}"
            }
        }
    }

    // Send a message
    fun sendMessage(adId: String, receiverId: String, messageContent: String) {
        if (messageContent.isNotBlank()) {
            errorMessage.value = null
            viewModelScope.launch {
                try {
                    messageRepository.sendMessage(adId, receiverId, messageContent)
                    this@MessageViewModel.messageContent.value = "" // Clear message input
                } catch (e: Exception) {
                    errorMessage.value = "Error sending message: ${e.message}"
                }
            }
        } else {
            errorMessage.value = "Message cannot be empty."
        }
    }
}
