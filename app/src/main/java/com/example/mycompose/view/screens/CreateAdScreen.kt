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
import coil.compose.rememberAsyncImagePainter
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.ProfileRepository
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CreateAdScreen(navController: NavHostController) {
    var adModel by remember { mutableStateOf(AdModel("", "", "", LocationItem(0.0, 0.0, ""), listOf(), "")) }
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var selectedImages by remember { mutableStateOf<List<Pair<Uri, String>>>(listOf()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // State for loading indicator

    val profileRepository = remember { ProfileRepository() }
    val adRepository = remember { AdRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Retrieve the current user's userId from FirebaseAuth
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val location = navController.currentBackStackEntry?.savedStateHandle?.get<LocationItem>("location")
    location?.let {
        adModel = adModel.copy(location = it)
    }

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

    LaunchedEffect(Unit) {
        userProfile = profileRepository.getUserProfile()
    }

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

        OutlinedTextField(
            value = adModel.location.address,
            onValueChange = { name ->
                adModel = adModel.copy(location = adModel.location.copy(address = name))
            },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    navController.navigate("MapScreen") {
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
                    val painter = rememberAsyncImagePainter(uri)
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

        if (isLoading) { // Show loading indicator
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        AdButton(
            onClick = {
                when {
                    adModel.title.isBlank() -> {
                        errorMessage = "Title cannot be empty."
                        showError = true
                    }
                    adModel.description.isBlank() -> {
                        errorMessage = "Description cannot be empty."
                        showError = true
                    }
                    adModel.location.address.isBlank() -> {
                        errorMessage = "Location cannot be empty."
                        showError = true
                    }
                    selectedImages.isEmpty() -> {
                        errorMessage = "You must add at least one photo."
                        showError = true
                    }
                    else -> {
                        isLoading = true // Start loading
                        coroutineScope.launch {
                            try {
                                // Upload images and get their URLs
                                val imageUrls = selectedImages.map { (uri, _) ->
                                    adRepository.uploadImage(uri)
                                }

                                // Add image URLs to ad model
                                adModel = adModel.copy(imageUrls = imageUrls)

                                // Add ad to Firestore with userId
                                userId?.let { adRepository.addAd(adModel, it) }
                                navController.navigate("Home")
                            } catch (e: Exception) {
                                errorMessage = "Failed to create ad."
                                showError = true
                            } finally {
                                isLoading = false // Stop loading
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
