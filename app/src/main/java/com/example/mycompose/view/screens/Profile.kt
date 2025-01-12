package com.example.mycompose.view.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.mycompose.model.AdModel
import com.example.mycompose.viewmodel.ProfileViewModel
import com.example.mycompose.model.UserProfile

@Composable
fun Profile(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile = profileViewModel.userProfile.value
    val newPicUri = profileViewModel.newPicUri.value
    val loading = profileViewModel.loading
    val successMessage = profileViewModel.successMessage
    val errorMessage = profileViewModel.errorMessage.value
    val userAds = profileViewModel.userAds.value // List of user's ads
    var showAllAds by remember { mutableStateOf(false) } // State to track "Show More"

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        profileViewModel.loadUserAds() // Load user's ads
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 8.dp
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        ProfileHeaderSection(
                            userProfile = userProfile,
                            newPicUri = newPicUri,
                            onImagePicked = { uri -> profileViewModel.newPicUri.value = uri },
                            onSaveProfile = {
                                profileViewModel.saveProfileData {
                                    navController.navigate("home") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "İlanlarım",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Show only two ads if 'showAllAds' is false, otherwise show all ads
                    items(userAds.take(if (showAllAds) userAds.size else 2)) { ad ->
                        UserAdCard(ad)
                    }

                    item {
                        // Show "Show More" button only if there are more than two ads
                        if (userAds.size > 2 && !showAllAds) {
                            Button(
                                onClick = { showAllAds = true },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                            ) {
                                Text("Show More", color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileHeaderSection(
    userProfile: UserProfile, // Your UserProfile model
    newPicUri: Uri?,
    onImagePicked: (Uri) -> Unit,
    onSaveProfile: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileImageSection(
            imageUri = newPicUri ?: Uri.parse(userProfile.profilePicture),
            onImagePicked = onImagePicked
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${userProfile.firstName} ${userProfile.lastName}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        EditableField(
            label = "Ad",
            value = userProfile.firstName,
            onValueChange = { /* Handle profile name change */ }
        )
        Spacer(modifier = Modifier.height(16.dp))
        EditableField(
            label = "Soyad",
            value = userProfile.lastName,
            onValueChange = { /* Handle profile surname change */ }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSaveProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text("Profili Kaydet", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun UserAdCard(ad: AdModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberImagePainter(data = ad.imageUrls.firstOrNull()),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = ad.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ad.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProfileImageSection(
    imageUri: Uri,
    onImagePicked: (Uri) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onImagePicked(it) } }
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            .clickable { launcher.launch("image/*") }
    ) {
        Image(
            painter = rememberImagePainter(imageUri),
            contentDescription = "Profile Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit Image",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            tint = Color.White
        )
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences // You can modify other keyboard options if needed
        )
    )
}





