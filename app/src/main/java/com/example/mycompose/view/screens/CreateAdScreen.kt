package com.example.mycompose.view.screens

import LocationInputField
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mycompose.RideViewModel
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.ProfileRepository
import com.example.mycompose.view.components.TransparentCircularProgressBar
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CreateAdScreen(navController: NavHostController, viewModel: RideViewModel) {
    var adModel by remember { mutableStateOf(AdModel("", "", "", "", listOf(), "","")) }
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var selectedImages by remember { mutableStateOf<List<Pair<Uri, String>>>(listOf()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var locationId by remember {
        mutableStateOf("")
    }

    val profileRepository = remember { ProfileRepository() }
    val adRepository = remember { AdRepository() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            if (selectedImages.size < 5) {
                val uniqueId = UUID.randomUUID().toString()
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

    val pickupLocationPlaces by viewModel.pickupLocationPlaces.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus() // Clear focus when clicking anywhere in the Box
                viewModel.checkAndSelectFirstPlace()
            }
    ) {
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

            LocationInputField(
                value = viewModel.pickUp,
                onValueChange = { newValue -> viewModel.onPickUpValueChanged(newValue)},
                placeholder = "Pickup Location",
                locations = pickupLocationPlaces,
                onLocationClick = { place ->
                    viewModel.onPlaceClick(place.name)
                    locationId = place.id
                    Log.d("CreateAdScreen", "Selected Place ID: ${place.id}") // Log the place.id
                },
                checkAndFirstPlace = {viewModel.checkAndSelectFirstPlace()
                    locationId=viewModel.unSelectedLocationId
                    Log.d("CreateAdScreen", "unSelected Place ID: ${viewModel.unSelectedLocationId}") // Log the place.id
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
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(8.dp)
                            )
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
                        viewModel.pickUp.text.isBlank() -> {
                            errorMessage = "Location cannot be empty."
                            showError = true
                        }
                        selectedImages.isEmpty() -> {
                            errorMessage = "You must add at least one photo."
                            showError = true
                        }
                        locationId.isEmpty()||locationId=="0"->{
                            errorMessage = "lütfen geçerli bir yer seçin"
                            showError = true
                        }

                        else -> {
                            showError = false
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val imageUrls = selectedImages.map { (uri, _) ->
                                        adRepository.uploadImage(uri)
                                    }

                                    val newAd = adModel.copy(
                                        id = UUID.randomUUID().toString(),
                                        userId = userProfile.userId,
                                        imageUrls = imageUrls,
                                        location = viewModel.pickUp.text,
                                        locationId = locationId
                                    )

                                    adRepository.addAd(newAd)

                                    viewModel.onPickUpValueChanged(TextFieldValue(""))
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    Log.e("CreateAdScreen", "Error adding ad: ", e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                text = "Submit"
            )
        }

        // Show loading indicator if isLoading is true
        TransparentCircularProgressBar(isLoading = isLoading)
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

@Composable
fun AdButton(onClick: () -> Unit, text: String) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun CreateAdScreenPreview() {
    // Mock viewModel instance or use a proper viewModel provider
    val mockViewModel = remember { RideViewModel() }
    CreateAdScreen(navController = rememberNavController(), viewModel = mockViewModel)
}
