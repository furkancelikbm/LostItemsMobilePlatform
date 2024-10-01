package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

            // Log the fetched data
            Log.d("MessageBoxScreen", "Fetched rooms: $fetchedRooms")

            // Get the current user ID
            val currentUserId = profileRepository.getCurrentUserId()

            // Pair messages with their corresponding user profiles
            val pairedRooms = fetchedRooms.map { room ->
                // Determine the receiverId
                val receiverId = if (room.first.senderId == currentUserId) {
                    room.first.receiverId
                } else {
                    room.first.senderId
                }

                // Fetch the user profile for the receiverId
                val userProfile = profileRepository.getUserProfileByAdUserId(receiverId)

                // Return a pair of message and user profile
                Pair(room.first, userProfile)
            }

            // Set the paired rooms
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
    if (isLoading) {
        CircularProgressIndicator()
    } else if (errorMessage != null) {
        Text(text = errorMessage, color = Color.Red)
    } else {
        LazyColumn {
            items(rooms) { (message, profile) ->
                MessageCard(message = message, userProfile = profile)
            }
        }
    }
}

@Composable
fun MessageCard(message: MessageModel, userProfile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        AsyncImage(
            model = userProfile.profilePicture,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Text(
            text = "${userProfile.firstName} ${userProfile.lastName}",
            style = MaterialTheme.typography.bodyMedium
        )

        // Last message
        Text(
            text = message.messageContent,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // Timestamp formatting
        val formattedTimestamp = formatTimestamp(message.timestamp)
        Text(
            text = "Last message: $formattedTimestamp",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
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
