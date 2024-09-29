package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import coil.compose.AsyncImage
import com.example.mycompose.model.MessageModel
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    navController: NavController,
    adId: String,
    receiverId: String,
    senderId: String,
    messageRepository: MessageRepository,
    profileRepository: ProfileRepository
) {
    var messageContent by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageModel>>(emptyList()) }
    var senderProfile by remember { mutableStateOf("") }
    var receiverProfile by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adId) {
        messageRepository.getMessagesRealtime(adId) { updatedMessages ->
            messages = updatedMessages
        }
        senderProfile = profileRepository.getUserProfileByAdUserId(senderId).profilePicture
        receiverProfile = profileRepository.getUserProfileByAdUserId(receiverId).profilePicture
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    senderId = senderId,
                    receiverId = receiverId,
                    senderProfile = senderProfile,
                    receiverProfile = receiverProfile
                )
            }
        }

        OutlinedTextField(
            value = messageContent,
            onValueChange = { messageContent = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Type a message") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (messageContent.isNotBlank()) {
                    errorMessage = null // Reset error message
                    coroutineScope.launch {
                        try {
                            val message = MessageModel(
                                senderId = senderId,
                                receiverId = receiverId,
                                messageContent = messageContent
                            )
                            messageRepository.sendMessage(adId, message)
                            messageContent = "" // Clear input
                        } catch (e: Exception) {
                            errorMessage = "Error sending message: ${e.message}"
                        }
                    }
                } else {
                    errorMessage = "Message cannot be empty."
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }

        errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun MessageItem(message: MessageModel, senderId: String, receiverId: String,senderProfile:String,receiverProfile:String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Message received by the current user (show on left)
        if (message.senderId != senderId) {
            AsyncImage(
                model = receiverProfile,
                contentDescription = "Receiver Profile Picture",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(text = message.messageContent, color = Color.Black)
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(text = message.messageContent, color = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            AsyncImage(
                model = senderProfile,
                contentDescription = "Sender Profile Picture",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
