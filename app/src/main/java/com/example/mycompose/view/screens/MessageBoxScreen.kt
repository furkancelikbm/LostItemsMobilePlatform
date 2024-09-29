package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mycompose.model.MessageModel
import com.example.mycompose.repository.MessageRepository

@Composable
fun MessageBoxScreen(
    navController: NavController,
    messageRepository: MessageRepository // Add a repository to fetch messages
) {
    var messages by remember { mutableStateOf<List<MessageModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch messages on the screen load
    LaunchedEffect(Unit) {
        try {
            messages = messageRepository.getMessagesForCurrentUser()
        } catch (e: Exception) {
            Log.e("MessageBoxScreen", "Error fetching messages: ${e.message}")
            errorMessage = "Error fetching messages"
        } finally {
            isLoading = false
        }
    }

    // Display loading indicator or messages
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else if (errorMessage != null) {
        Text(text = errorMessage!!, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
    } else if (messages.isNotEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages) { message ->
                MessageItem(message) {
                    navController.navigate("chat/${message.senderId}")
                }
            }
        }
    } else {
        Text(text = "No messages found.", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun MessageItem(message: MessageModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = message.messageContent, style = MaterialTheme.typography.bodyMedium)
        Text(text = message.timestamp.toString(), style = MaterialTheme.typography.bodySmall)
    }
}
