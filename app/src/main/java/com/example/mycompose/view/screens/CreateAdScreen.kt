package com.example.mycompose.view.screens

import LocationInputField
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mycompose.util.getBitmapFromUri
import com.example.mycompose.view.components.TransparentCircularProgressBar
import com.example.mycompose.viewmodel.CreateAdScreenViewModel
import com.example.mycompose.viewmodel.AdPredictionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun CreateAdScreen(
    navController: NavHostController) {

    val viewModel: CreateAdScreenViewModel = viewModel()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Inject AdPredictionViewModel with Hilt
    val predictionViewModel: AdPredictionViewModel = hiltViewModel()

    // Gallery launcher for image selection
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.addImage(it) } }

    // Selected category and most frequent prediction label
    val selectedCategory = viewModel.selectedCategory.value
    val mostFrequentLabel = predictionViewModel.predictionResult.value


    // Observe saved category
    val backStackEntry = navController.currentBackStackEntry
    val savedStateHandle = backStackEntry?.savedStateHandle
    savedStateHandle?.getLiveData<String>("selectedCategory")?.observeAsState()?.value?.let { category ->
        category?.let { viewModel.selectedCategory.value = it }
    }

    // Trigger prediction for each selected image
    val selectedImages = viewModel.selectedImages
    if (selectedImages.isNotEmpty()) {
        selectedImages.forEach { (uri, _) ->
            val bitmap = getBitmapFromUri(context, uri)
            bitmap?.let { predictionViewModel.predictImage(it) }
        }
    }

    // UI layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                focusManager.clearFocus()
                viewModel.locationInputFieldViewModel.checkAndSelectFirstPlace()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }

            Text(text = "Create New Ad", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Title and Description Input Fields
            AdTextField(
                value = viewModel.adModel.value.title,
                onValueChange = { viewModel.adModel.value = viewModel.adModel.value.copy(title = it) },
                label = "Title"
            )

            Spacer(modifier = Modifier.height(8.dp))

            AdTextField(
                value = viewModel.adModel.value.description,
                onValueChange = { viewModel.adModel.value = viewModel.adModel.value.copy(description = it) },
                label = "Description"
            )

            Spacer(modifier = Modifier.height(8.dp))

            LocationInputField(
                value = viewModel.locationInputFieldViewModel.pickUp,
                onValueChange = { viewModel.locationInputFieldViewModel.onPickUpValueChanged(it) },
                placeholder = "Pickup Location",
                locations = viewModel.locationInputFieldViewModel.pickupLocationPlaces.collectAsState().value,
                onLocationClick = { place ->
                    viewModel.locationInputFieldViewModel.onPlaceClick(place.name)
                    viewModel.locationId.value = place.id
                    Log.d("CreateAdScreen", "Selected Place ID: ${place.id}")
                },
                checkAndFirstPlace = {
                    viewModel.locationInputFieldViewModel.checkAndSelectFirstPlace()
                    viewModel.locationId.value = viewModel.locationInputFieldViewModel.unSelectedLocationId
                },
                onLocationButtonClick = {
                    Log.d("CreateAdScreen", "Location button clicked")
                    navController.navigate("mapScreen") // Navigate to the MapScreen
                }
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            OutlinedTextField(
                value = if (selectedCategory.isNotEmpty()) selectedCategory else "Choose a Category",
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        "Category",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("ChooseCategoryScreen") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Arrow")
                },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prediction Label
            Text(text = "Prediction: $mostFrequentLabel", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // Select Photos Button
            AdButton(onClick = { galleryLauncher.launch(arrayOf("image/*")) }, text = "Select Photos")

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Images Gallery
            LazyRow {
                items(viewModel.selectedImages) { (uri, uniqueId) ->
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .padding(4.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        val painter = rememberImagePainter(uri)
                        Image(painter = painter, contentDescription = null, modifier = Modifier.fillMaxSize())

                        IconButton(
                            onClick = {
                                viewModel.removeImage(uniqueId)
                                if (viewModel.selectedImages.isNotEmpty()) {
                                    val remainingImage = viewModel.selectedImages.last()
                                    val bitmap = getBitmapFromUri(context, remainingImage.first)
                                    bitmap?.let { predictionViewModel.predictImage(it) }
                                } else {
                                    predictionViewModel.predictionResult.value = ""
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

            // Error Message Display
            if (viewModel.showError.value) {
                Text(
                    text = viewModel.errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Submit Button
            AdButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submitAd {
                        Toast.makeText(context, viewModel.successMessage, Toast.LENGTH_SHORT).show()
                        navController.navigate("Home")
                    }
                },
                text = "Submit"
            )
        }

        // Loading Progress Bar
        TransparentCircularProgressBar(isLoading = viewModel.isLoading.value)
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
    CreateAdScreen(navController = rememberNavController())
}
