package com.example.mycompose.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.MessageBoxState
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageBoxViewModel : ViewModel() {

    // Repositories
    private val profileRepository = ProfileRepository()
    private val messageRepository = MessageRepository()

    // StateFlow for UI state management
    private val _state = MutableStateFlow(MessageBoxState())
    val state: StateFlow<MessageBoxState> get() = _state

    // Load messages for the current user
    fun loadMessages() {
        _state.value = MessageBoxState(isLoading = true)

        viewModelScope.launch {
            try {
                // Fetch the rooms for the current user
                val fetchedRooms = messageRepository.getRoomsForCurrentUser()

                // Get the current user's ID
                val currentUserId = profileRepository.getCurrentUserId()

                // Map the fetched rooms to their corresponding user profiles
                val pairedRooms = fetchedRooms.map { room ->
                    val receiverId = if (room.first.senderId == currentUserId) {
                        room.first.receiverId
                    } else {
                        room.first.senderId
                    }

                    // Fetch the user profile by the receiver ID
                    val userProfile = profileRepository.getUserProfileByAdUserId(receiverId)
                    Pair(room.first, userProfile)
                }

                // Update the state with the fetched rooms and their profiles
                _state.value = MessageBoxState(rooms = pairedRooms)
            } catch (e: Exception) {
                // Handle error by updating the state with an error message
                _state.value = MessageBoxState(errorMessage = "Error fetching rooms: ${e.message}")
                Log.e("MessageBoxViewModel", "Error: ${e.message}")
            }
        }
    }
}
