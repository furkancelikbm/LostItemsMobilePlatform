package com.example.mycompose.view.screens

import LocationInputField
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.mycompose.viewmodel.CreateAdScreenViewModel
import com.example.mycompose.view.components.TransparentCircularProgressBar
import com.example.mycompose.viewmodel.AdPredictionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun CreateAdScreen(
    navController: NavHostController,
    viewModel: CreateAdScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Inject AdPredictionViewModel with Hilt
    val predictionViewModel: AdPredictionViewModel = hiltViewModel()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addImage(it)
            // Convert the selected image Uri to Bitmap for prediction
            val bitmap = getBitmapFromUri(context, it)
            bitmap?.let {predictionViewModel.predictImage(it)
                // Log the prediction output here
                Log.d("CreateAdScreen", "Prediction Result: ${predictionViewModel.predictionResult.value}") }
        }
    }

    val selectedCategory =viewModel.selectedCategory.value

    // Observe for selected category result
    val backStackEntry = navController.currentBackStackEntry
    val savedStateHandle = backStackEntry?.savedStateHandle
    savedStateHandle?.getLiveData<String>("selectedCategory")?.observeAsState()!!.value?.let { category ->
        category?.let {
            viewModel.selectedCategory.value = it
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
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
            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick ={navController.popBackStack()} ) {
                    Icon(
                        imageVector =Icons.Default.ArrowBack ,
                        contentDescription = "Back" )
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

            OutlinedTextField(
                value = if (selectedCategory.isNotEmpty()) selectedCategory else "Choose a Category",
                onValueChange = {},
                readOnly = true, // Make the field read-only
                label = { Text(
                    "Category",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("ChooseCategoryScreen") }, // Clickable on the entire field
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow"
                    )
                },
                enabled = false // This ensures it looks like a static input but is still clickable
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
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(8.dp)
                            )
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

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    val inputStream = context.contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}