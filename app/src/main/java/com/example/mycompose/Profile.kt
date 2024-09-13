package com.example.mycompose

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.widgets.ProfileImage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Profile(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current

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
                onImageChangeClick = { uri ->
                    viewModel.newPicUri.value = uri
                }
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))
            OutlinedTextField(
                value = viewModel.editFirstName.value,
                onValueChange = { viewModel.editFirstName.value = it },
                label = { Text(text = "First name") },
                readOnly = !viewModel.editMode.value
            )
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            OutlinedTextField(
                value = viewModel.editLastName.value,
                onValueChange = { viewModel.editLastName.value = it },
                label = { Text(text = "Last name") },
                readOnly = !viewModel.editMode.value
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (viewModel.editMode.value) {
                            viewModel.saveProfileData(context, navController, "Home")
                        }
                        viewModel.editMode.value = !viewModel.editMode.value
                    },
                    modifier = Modifier.height(40.dp),
                ) {
                    Text(text = if (!viewModel.editMode.value) "Edit Profile" else "Save")
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Icon(imageVector = if (!viewModel.editMode.value) Icons.Default.Edit else Icons.Default.Save, contentDescription = null)
                }
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("Login")
                    },
                    modifier = Modifier.height(40.dp),
                ) {
                    Text(text = "Log Out")
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen(navController: NavController = rememberNavController()) {
    Profile(navController = navController)
}

