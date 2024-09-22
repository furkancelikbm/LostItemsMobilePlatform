package com.example.mycompose.ui

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mycompose.viewmodel.ProfileViewModel
import com.example.mycompose.repository.ProfileRepository
import com.example.mycompose.viewmodel.ProfileViewModelFactory
import com.example.mycompose.view.components.ProfileImage

@Composable
fun CompleteProfileScreen(navController: NavController) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(ProfileRepository()))
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    if (viewModel.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.padding(top = 25.dp))
            ProfileImage(
                imageUrl = viewModel.newPicUri.value ?: Uri.parse(viewModel.userProfile.value.profilePicture),
                onImageChangeClick = { uri -> viewModel.newPicUri.value = uri }
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))
            OutlinedTextField(
                value = viewModel.userProfile.value.firstName,
                onValueChange = { firstName -> viewModel.userProfile.value = viewModel.userProfile.value.copy(firstName = firstName) },
                label = { Text(text = "First name") }
            )
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            OutlinedTextField(
                value = viewModel.userProfile.value.lastName,
                onValueChange = { lastName -> viewModel.userProfile.value = viewModel.userProfile.value.copy(lastName = lastName) },
                label = { Text(text = "Last name") }
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))

            if (viewModel.errorMessage.value.isNotEmpty()) {
                Text(
                    text = viewModel.errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    // Check if all required fields are filled
                    val isFormValid = viewModel.userProfile.value.firstName.isNotBlank() &&
                            viewModel.userProfile.value.lastName.isNotBlank() &&
                            viewModel.newPicUri.value != null

                    if (isFormValid) {
                        viewModel.saveProfileData {
                            // On successful profile update
                            Toast.makeText(context, viewModel.successMessage, Toast.LENGTH_SHORT).show()
                            navController.navigate("ProfileApp") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else {
                        viewModel.errorMessage.value = "Please fill in all fields and select a profile picture."
                    }
                },
                modifier = Modifier.height(40.dp)
            ) {
                Text(text = "Save")
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
            }
        }
    }
}
