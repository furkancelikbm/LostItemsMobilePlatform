package com.example.mycompose.view.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CreateAdScreen(navController: NavHostController) {
    var adModel by remember { mutableStateOf(AdModel("", "", "", "", listOf(), "")) }
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var selectedImages by remember { mutableStateOf<List<Pair<Uri, String>>>(listOf()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val profileRepository = remember { ProfileRepository() }
    val adRepository = remember { AdRepository() }
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            if (selectedImages.size < 5) {
                val uniqueId = UUID.randomUUID().toString() // Generate a unique ID
                selectedImages = selectedImages + Pair(it, uniqueId)
            }
        }
    }

    // Fetch user profile
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userProfile = profileRepository.getUserProfile()
        }
    }

    // Observe location state
    val location = navController.currentBackStackEntry?.savedStateHandle?.get<String>("location")
    location?.let { adModel = adModel.copy(location = it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create New Ad", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        AdTextField(
            value = adModel.title,
            onValueChange = { adModel = adModel.copy(title = it) },
            label = "Title"
        )
        Spacer(modifier = Modifier.height(8.dp))
        AdTextField(
            value = adModel.description,
            onValueChange = { adModel = adModel.copy(description = it) },
            label = "Description"
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Location TextField with Icon
        OutlinedTextField(
            value = adModel.location,
            onValueChange = { adModel = adModel.copy(location = it) },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    navController.navigate("MapScreen") {
                        // Use the result callback to get the selected location
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Select Location")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdButton(
            onClick = {
                galleryLauncher.launch(arrayOf("image/*"))
            },
            text = "Select Photos"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow {
            items(selectedImages) { (uri, _) ->
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(4.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                ) {
                    val painter = rememberImagePainter(uri)
                    Image(painter = painter, contentDescription = null, modifier = Modifier.fillMaxSize())

                    val uniqueId = selectedImages.find { it.first == uri }?.second
                    IconButton(
                        onClick = {
                            uniqueId?.let {
                                selectedImages = selectedImages.filter { it.second != uniqueId }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        AdButton(
            onClick = {
                // Form validation
                when {
                    adModel.title.isBlank() -> {
                        errorMessage = "Title cannot be empty."
                        showError = true
                    }
                    adModel.description.isBlank() -> {
                        errorMessage = "Description cannot be empty."
                        showError = true
                    }
                    adModel.location.isBlank() -> {
                        errorMessage = "Location cannot be empty."
                        showError = true
                    }
                    selectedImages.isEmpty() -> {
                        errorMessage = "You must add at least one photo."
                        showError = true
                    }
                    else -> {
                        showError = false
                        coroutineScope.launch {
                            try {
                                val imageUrls = selectedImages.map { (uri, _) ->
                                    adRepository.uploadImage(uri)
                                }

                                val newAd = adModel.copy(
                                    id = UUID.randomUUID().toString(),
                                    userId = userProfile.userId,
                                    imageUrls = imageUrls
                                )

                                adRepository.addAd(newAd)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("CreateAdScreen", "Error adding ad: ", e)
                            }
                        }
                    }
                }
            },
            text = "Submit"
        )
    }
}

@Composable
fun AdTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun CreateAdScreenPreview() {
    CreateAdScreen(navController = rememberNavController())
}

@Composable
fun AdButton(onClick: () -> Unit, text: String) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}
