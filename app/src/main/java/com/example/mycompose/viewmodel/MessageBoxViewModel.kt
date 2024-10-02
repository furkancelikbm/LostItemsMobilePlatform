package com.example.mycompose.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.MessageBoxState
import com.example.mycompose.model.MessageModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageBoxViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()
    private val messageRepository = MessageRepository()

    // Use MutableStateFlow to track the state
    private val _state = MutableStateFlow(MessageBoxState())
    val state: StateFlow<MessageBoxState> get() = _state

    fun loadMessages() {
        _state.value = MessageBoxState(isLoading = true)

        viewModelScope.launch {
            try {
                val fetchedRooms = messageRepository.getRoomsForCurrentUser()
                val currentUserId = profileRepository.getCurrentUserId()

                val pairedRooms = fetchedRooms.map { room ->
                    val receiverId = if (room.first.senderId == currentUserId) {
                        room.first.receiverId
                    } else {
                        room.first.senderId
                    }

                    val userProfile = profileRepository.getUserProfileByAdUserId(receiverId)
                    Pair(room.first, userProfile)
                }

                _state.value = MessageBoxState(rooms = pairedRooms)
            } catch (e: Exception) {
                _state.value = MessageBoxState(errorMessage = "Error fetching rooms: ${e.message}")
                Log.e("MessageBoxViewModel", "Error: ${e.message}")
            }
        }
    }
}