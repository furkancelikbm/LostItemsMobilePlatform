package com.example.mycompose.view.screens

import LocationInputField
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mycompose.viewmodel.CreateAdScreenViewModel
import com.example.mycompose.view.components.TransparentCircularProgressBar

@Composable
fun CreateAdScreen(
    navController: NavHostController,
    viewModel: CreateAdScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(it) }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdButton(
                onClick = { galleryLauncher.launch(arrayOf("image/*")) },
                text = "Select Photos"
            )

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
                            onClick = { viewModel.removeImage(uniqueId) },
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

            AdButton(onClick = {
                focusManager.clearFocus()
                viewModel.submitAd{
                    Toast.makeText(context, viewModel.successMessage, Toast.LENGTH_SHORT).show()
                    navController.navigate("Home")}},
                text = "Submit")
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
