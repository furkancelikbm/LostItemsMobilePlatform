package com.example.mycompose.view.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.mycompose.model.MessageModel
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import com.example.mycompose.model.UserProfile

@Composable
fun MessageBoxScreen(
    navController: NavController
) {
    val messageRepository = MessageRepository() // Pass it to MessageRepository
    val profileRepository = ProfileRepository()

    val (messages, setMessages) = remember { mutableStateOf<List<MessageModel>>(emptyList()) }
    val (isLoading, setLoading) = remember { mutableStateOf(true) }
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val (userProfile, setUserProfile) = remember { mutableStateOf<UserProfile?>(null) } // Store user profile

    LaunchedEffect(Unit) {
        fetchMessages(messageRepository, setMessages, setLoading, setErrorMessage)
        val profile = profileRepository.getUserProfile() // Fetch user profile here
        setUserProfile(profile) // Update state with fetched profile
    }

    MessageBoxContent(
        messages = messages,
        isLoading = isLoading,
        errorMessage = errorMessage,
        navController = navController,
        userProfile = userProfile // Pass user profile to MessageBoxContent
    )
}

suspend fun fetchMessages(
    messageRepository: MessageRepository,
    setMessages: (List<MessageModel>) -> Unit,
    setLoading: (Boolean) -> Unit,
    setErrorMessage: (String?) -> Unit
) {
    try {
        setLoading(true)
        val messages = messageRepository.getMessagesForCurrentUser()
        setMessages(messages)
    } catch (e: Exception) {
        setErrorMessage(e.message)
    } finally {
        setLoading(false)
    }
}

@Composable
fun MessageBoxContent(
    messages: List<MessageModel>,
    isLoading: Boolean,
    errorMessage: String?,
    navController: NavController,
    userProfile: UserProfile? // Accept user profile
) {
    if (isLoading) {
        // Show a loading indicator
        CircularProgressIndicator()
    } else if (errorMessage != null) {
        // Show an error message
        Text(text = errorMessage, color = Color.Red)
    } else {
        // Display the messages
        LazyColumn {
            items(messages) { message ->
                // Display each message with user profile info
                Text(text = "${userProfile?.firstName ?: "Unknown"}: ${message.messageContent}")
            }
        }
    }
}
