package com.example.mycompose

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.NavigationBar.BottomNavigationBar
import com.example.mycompose.NavigationBar.NavigationViewModel
import com.example.mycompose.navigation.Navigation
import com.example.mycompose.navigation.Screens
import com.example.mycompose.widgets.ProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CompleteProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile(context, navController, "Home")
    }


    if (!viewModel.profileLoaded.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 25.dp))
            ProfileImage(
                imageUrl = viewModel.newPicUri.value ?: Uri.parse(viewModel.picUrl.value),
                onImageChangeClick = { uri -> viewModel.newPicUri.value = uri }
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))
            OutlinedTextField(
                value = viewModel.editFirstName.value,
                onValueChange = { viewModel.editFirstName.value = it },
                label = { Text(text = "First name") }
            )
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            OutlinedTextField(
                value = viewModel.editLastName.value,
                onValueChange = { viewModel.editLastName.value = it },
                label = { Text(text = "Last name") }
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))

            // Show error message if any
            if (viewModel.errorMessage.value.isNotEmpty()) {
                Text(
                    text = viewModel.errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = @androidx.compose.ui.tooling.preview.Preview {
                    // Check if all required fields are filled
                    val isFormValid = viewModel.editFirstName.value.isNotBlank() &&
                            viewModel.editLastName.value.isNotBlank() &&
                            viewModel.newPicUri.value != null

                    if (isFormValid) {
                        // Save profile data and navigate to home screen
                        viewModel.saveProfileData(context, navController, "Home")
                        navController.navigate(Screens.Home.name) {
                            // Clear back stack to avoid navigating back to the CompleteProfileScreen
                            popUpTo(Screens.CompleteProfileScreen.name) { inclusive = true }
                        }
                    } else {
                        // Update error message if form is invalid
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

@Composable
fun CheckProfileCompletion(navController: NavController) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    var profileComplete by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) } // To show progress indicator while loading

    LaunchedEffect(firebaseUser) {
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("first_name").orEmpty()
                    val lastName = document.getString("last_name").orEmpty()
                    val profilePicture = document.getString("profile_picture").orEmpty()

                    // Check if profile is complete
                    profileComplete = firstName.isNotEmpty() && lastName.isNotEmpty() && profilePicture.isNotEmpty()
                    loading = false

                    // Navigate based on profile completeness
                    if (profileComplete) {
                        navController.navigate(Screens.Home.name)
                    } else {
                        navController.navigate(Screens.CompleteProfileScreen.name)
                    }
                }
                .addOnFailureListener {
                    loading = false
                    // Handle error
                }
        }
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}






@Preview(showBackground=true)
@Composable
fun PreviewCompleteProfileScreen(){
    val navController= rememberNavController()
    CompleteProfileScreen(navController=navController)
}
