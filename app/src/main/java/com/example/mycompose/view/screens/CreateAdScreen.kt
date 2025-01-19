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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.TextFieldValue
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

@Composable
fun CreateAdScreen(
    navController: NavHostController
) {
    val viewModel: CreateAdScreenViewModel = viewModel()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val predictionViewModel: AdPredictionViewModel = hiltViewModel()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.addImage(it) } }

    val selectedCategory = viewModel.selectedCategory.value
    val mostFrequentLabel = predictionViewModel.predictionResult.value

    val backStackEntry = navController.currentBackStackEntry
    val savedStateHandle = backStackEntry?.savedStateHandle
    savedStateHandle?.getLiveData<String>("selectedCategory")?.observeAsState()?.value?.let { category ->
        category?.let { viewModel.selectedCategory.value = it }
    }

    val selectedImages = viewModel.selectedImages
    if (selectedImages.isNotEmpty()) {
        selectedImages.forEach { (uri, _) ->
            val bitmap = getBitmapFromUri(context, uri)
            bitmap?.let { predictionViewModel.predictImage(it) }
        }
    }

    val searchText = navController.currentBackStackEntry?.savedStateHandle?.get<String>("searchText") ?: ""
    val location = remember { mutableStateOf(TextFieldValue(searchText)) }

    if (location.value.text.isNotEmpty()) {
        viewModel.locationInputFieldViewModel.onPickUpValueChanged(location.value)
    }

    val latitude = navController.currentBackStackEntry?.savedStateHandle?.get<Double>("latitude")
    val longitude = navController.currentBackStackEntry?.savedStateHandle?.get<Double>("longitude")

    if (latitude != null && longitude != null) {
        viewModel.locationValidation.value = location.value.text
        viewModel.locationInputFieldViewModel.updateLocationData(longitude, latitude, location.value)
    } else {
        Log.d("CreateAdScreen", "Latitude or Longitude not provided")
    }

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Scrollable column
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                onValueChange = { newValue ->
                    viewModel.locationInputFieldViewModel.onPickUpValueChanged(newValue)
                    if (newValue.text.isEmpty()) {
                        viewModel.locationInputFieldViewModel.clearPickUpLocation()
                        location.value = TextFieldValue("")
                    }
                },
                placeholder = "Pickup Location",
                locations = viewModel.locationInputFieldViewModel.pickupLocationPlaces.collectAsState().value,
                onLocationClick = { place ->
                    viewModel.locationInputFieldViewModel.onPlaceClick(place.name)
                    viewModel.locationValidation.value = place.id
                },
                checkAndFirstPlace = {
                    viewModel.locationInputFieldViewModel.checkAndSelectFirstPlace()
                    viewModel.locationValidation.value = viewModel.locationInputFieldViewModel.unSelectedLocationId
                },
                onLocationButtonClick = {
                    navController.navigate("mapScreen")
                },
                onRemoveClick = {
                    viewModel.locationInputFieldViewModel.clearPickUpLocation()
                    location.value = TextFieldValue("")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = if (selectedCategory.isNotEmpty()) selectedCategory else "Choose a Category",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("ChooseCategoryScreen") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Arrow")
                },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Prediction: $mostFrequentLabel", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            AdButton(onClick = { galleryLauncher.launch(arrayOf("image/*")) }, text = "Select Photos")

            Spacer(modifier = Modifier.height(16.dp))

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

            if (viewModel.showError.value) {
                Text(
                    text = viewModel.errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

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
