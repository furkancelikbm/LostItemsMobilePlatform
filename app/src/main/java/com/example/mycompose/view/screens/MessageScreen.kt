package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mycompose.viewmodel.MessageViewModel
import coil.compose.AsyncImage
import com.example.mycompose.model.MessageModel

@Composable
fun MessageScreen(
    navController: NavController,
    adId: String,
    receiverId: String,
    senderId: String,
    messageViewModel: MessageViewModel
) {
    val messages by remember { messageViewModel.messages }
    val senderProfile by remember { messageViewModel.senderProfile }
    val receiverProfile by remember { messageViewModel.receiverProfile }
    val senderName by remember { messageViewModel.senderName }
    val receiverName by remember { messageViewModel.receiverName }
    val errorMessage by remember { messageViewModel.errorMessage }
    var messageContent by remember { mutableStateOf(messageViewModel.messageContent.value) }

    // Fetch chat data when the screen is launched
    LaunchedEffect(adId) {
        messageViewModel.fetchChatData(adId, receiverId, senderId)
        Log.d("MessageScreen", "SenderId: $senderName")    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = {navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack ,
                    contentDescription = "Back")
            }
        }
        // Display ad title and user names
        Text(text = adId, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Chat with $receiverName", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "sen $senderName", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))

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
                    receiverProfile = receiverProfile,
                    senderName = senderName,
                    receiverName = receiverName
                )
            }
        }

        OutlinedTextField(
            value = messageContent,
            onValueChange = {
                messageContent = it
                messageViewModel.messageContent.value = it // Update ViewModel
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Type a message") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                messageViewModel.sendMessage(adId, receiverId, messageContent)
                messageContent = "" // Clear input
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
fun MessageItem(
    message: MessageModel,
    senderId: String,
    receiverId: String,
    senderProfile: String,
    receiverProfile: String,
    senderName: String,
    receiverName: String
) {
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

            Column {
                Text(text = receiverName, style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(text = message.messageContent, color = Color.Black)
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))

            Column {
                Text(text = senderName, style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(text = message.messageContent, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            AsyncImage(
                model = senderProfile,
                contentDescription = "Sender Profile Picture",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
