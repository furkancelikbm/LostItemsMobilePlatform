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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mycompose.model.MessageModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.ui.viewmodel.MessageBoxViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycompose.repository.ProfileRepository

@Composable
fun MessageBoxScreen(
    navController: NavController
) {
    val messageBoxViewModel: MessageBoxViewModel = viewModel()
    val state by messageBoxViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        messageBoxViewModel.loadMessages()
    }

    MessageBoxContent(
        rooms = state.rooms,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
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
    val modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .background(Color.White, MaterialTheme.shapes.medium)
        .clip(MaterialTheme.shapes.medium)
        .clickable {
            navController.navigate("message/${message.adId}/${userProfile.userId}/${profileRepository.getCurrentUserId()}")
            Log.d("MessageCard", "Navigating to message screen with adId: ${message.adId}, alici: ${userProfile.userId} gonderici :${profileRepository.getCurrentUserId()}")
        }
        .border(1.dp, Color.LightGray)
        .padding(16.dp)

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = userProfile.profilePicture,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${userProfile.firstName} ${userProfile.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.messageContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
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
