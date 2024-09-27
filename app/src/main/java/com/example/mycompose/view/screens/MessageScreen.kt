package com.example.mycompose.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.privacysandbox.ads.adservices.adid.AdId
import com.example.mycompose.model.MessageModel
import com.example.mycompose.repository.MessageRepository
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    navController: NavController,
    adId: String,
    userId: String,
    messageRepository: MessageRepository // Inject your repository
) {
    var messageContent by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageModel>>(emptyList()) }

    // LaunchedEffect will listen for real-time changes from Firestore
    LaunchedEffect(adId) {
        // Start listening for message updates in real-time
        messageRepository.getMessagesRealtime(adId) { updatedMessages ->
            messages = updatedMessages
        }
    }

    // Create a coroutine scope for handling button click side effects
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display existing messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true // Scroll from the bottom (most recent message)
        ) {
            items(messages) { message ->
                MessageItem(message)
            }
        }

        // Input field for typing a new message
        OutlinedTextField(
            value = messageContent,
            onValueChange = { messageContent = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Type a message") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Button to send a message
        Button(
            onClick = {
                // Use the coroutine scope to handle sending the message
                coroutineScope.launch {
                    val message = MessageModel(
                        senderId = "currentUserId", // Replace with actual sender's ID
                        receiverId = userId,
                        messageContent = messageContent
                    )
                    // Send the message and refresh the list of messages
                    messageRepository.sendMessage(adId, message)
                    messageContent = "" // Clear the input field after sending
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }
    }
}

@Composable
fun MessageItem(message: MessageModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Customize the message bubble based on sender/receiver
        if (message.senderId == "currentUserId") {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(text = message.messageContent, color = Color.White)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(text = message.messageContent, color = Color.Black)
            }
        }
    }
}
