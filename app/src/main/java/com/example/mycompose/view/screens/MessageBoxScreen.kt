package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mycompose.model.MessageModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.MessageRepository
import com.example.mycompose.repository.ProfileRepository
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageBoxScreen(
    navController: NavController
) {
    val profileRepository = ProfileRepository()
    val messageRepository = MessageRepository()
    val (rooms, setRooms) = remember { mutableStateOf<List<Pair<MessageModel, UserProfile>>>(emptyList()) }
    val (isLoading, setLoading) = remember { mutableStateOf(true) }
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            setLoading(true)
            val fetchedRooms = messageRepository.getRoomsForCurrentUser()

            Log.d("MessageBoxScreen", "Fetched rooms: $fetchedRooms")

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

            setRooms(pairedRooms)
        } catch (e: Exception) {
            setErrorMessage(e.message)
            Log.e("MessageBoxScreen", "Error fetching rooms: ${e.message}")
        } finally {
            setLoading(false)
        }
    }

    MessageBoxContent(
        rooms = rooms,
        isLoading = isLoading,
        errorMessage = errorMessage,
        navController = navController
    )
}

@Composable
fun MessageBoxContent(
    rooms: List<Pair<MessageModel, UserProfile>>,
    isLoading: Boolean,
    errorMessage: String?,
    navController: NavController
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage, color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(rooms) { (message, profile) ->
                    MessageCard(message = message, userProfile = profile, navController = navController, profileRepository = ProfileRepository())
                }
            }
        }
    }
}

@Composable
fun MessageCard(
    message: MessageModel,
    userProfile: UserProfile,
    navController: NavController,
    profileRepository: ProfileRepository
) {
    // Create a modifier for the card
    val modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .background(Color.White, MaterialTheme.shapes.medium)
        .clip(MaterialTheme.shapes.medium)
        .clickable {
            // Navigate to the message screen with adId, receiverId, and senderId
            val senderId=profileRepository.getCurrentUserId()
            navController.navigate("message/${message.adId}/${userProfile.userId}/${senderId}")
            Log.d("MessageCard", "Ad ID: ${message.adId}, alıcı: ${userProfile.firstName}, gonderici: ${message.senderId}")

        }
        .border(1.dp, Color.LightGray) // Border for the striped effect
        .padding(16.dp)

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = userProfile.profilePicture,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(60.dp) // Increased size for the profile image
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp)) // Increased spacing
            Column {
                Text(
                    text = "${userProfile.firstName} ${userProfile.lastName}",
                    style = MaterialTheme.typography.titleMedium, // Larger text style
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp)) // Added spacing between texts
                Text(
                    text = message.messageContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp)) // Added spacing
                val formattedTimestamp = formatTimestamp(message.timestamp)
                Text(
                    text = "Last message: $formattedTimestamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}


// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val currentDate = System.currentTimeMillis()
    val differenceInMillis = currentDate - timestamp
    return when {
        differenceInMillis < 24 * 60 * 60 * 1000 -> "Today"
        differenceInMillis < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> {
            java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        }
    }
}
