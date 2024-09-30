package com.example.mycompose.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.mycompose.model.MessageModel
import com.example.mycompose.repository.MessageRepository
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mycompose.model.UserProfile

@Composable
fun MessageBoxScreen(
    navController: NavController
) {
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

            setRooms(fetchedRooms)
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
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Profile picture
        AsyncImage(
            model = userProfile.profilePicture,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        // Full name
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

        // Timestamp
        Text(
            text = "Last message: ${message.timestamp}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
