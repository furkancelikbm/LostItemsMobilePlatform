package com.example.mycompose

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.navigation.Screens
import com.example.mycompose.widgets.ProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun Profile(navController: NavController) {
    val context = LocalContext.current
    var isProfileFilled by remember { mutableStateOf(false) }
    var profileLoaded by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var picUrl by remember { mutableStateOf("") }
    var newPicUri by remember { mutableStateOf<Uri?>(null) } // New URI for updated picture
    var editMode by remember { mutableStateOf(false) } // Toggle between edit and view mode
    var errorMessage by remember { mutableStateOf("") } // To show error messages

    // Load user data when the composable is first launched
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                firstName = document.getString("first_name").orEmpty()
                lastName = document.getString("last_name").orEmpty()
                picUrl = document.getString("profile_picture").orEmpty()

                // Pre-fill edit fields with existing data if available
                editFirstName = firstName
                editLastName = lastName

                isProfileFilled = firstName.isNotEmpty() && lastName.isNotEmpty() && picUrl.isNotEmpty()
                profileLoaded = true // Mark profile as loaded

                if (isProfileFilled) {
                    navController.navigate(Screens.Home.name) {
                        popUpTo(Screens.Profile.name) { inclusive = true }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error checking profile status.", Toast.LENGTH_SHORT).show()
            }
    }

    if (!profileLoaded) {
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
                imageUrl = newPicUri ?: Uri.parse(picUrl), // Show new pic URI if available
                onImageChangeClick = { uri ->
                    newPicUri = uri // Save new URI but do not update yet
                }
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))
            OutlinedTextField(
                value = editFirstName,
                onValueChange = { editFirstName = it },
                label = { Text(text = "First name") },
                readOnly = !editMode
            )
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            OutlinedTextField(
                value = editLastName,
                onValueChange = { editLastName = it },
                label = { Text(text = "Last name") },
                readOnly = !editMode
            )
            Spacer(modifier = Modifier.padding(vertical = 25.dp))

            // Show error message if any
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
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
                        if (editMode) {
                            // Validate fields only when saving
                            if (editFirstName.isEmpty() || editLastName.isEmpty()) {
                                errorMessage = "Please fill in all fields."
                            } else {
                                errorMessage = "" // Clear error message
                                if (newPicUri != null) { // Update profile picture if new URI is available
                                    updateProfilePicture(newPicUri!!) { updatedPicUrl ->
                                        picUrl = updatedPicUrl
                                        saveProfileData(context, editFirstName, editLastName, updatedPicUrl) {
                                            // Navigate to home screen after successful update
                                            navController.navigate(Screens.Home.name) {
                                                popUpTo(Screens.Profile.name) { inclusive = true }
                                            }
                                        }
                                    }
                                } else {
                                    saveProfileData(context, editFirstName, editLastName, picUrl) {
                                        // Navigate to home screen after successful update
                                        navController.navigate(Screens.Home.name) {
                                            popUpTo(Screens.Profile.name) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }
                        editMode = !editMode
                    },
                    modifier = Modifier.height(40.dp),
                ) {
                    Text(text = if (!editMode) "Edit Profile" else "Save")
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Icon(imageVector = if (!editMode) Icons.Default.Edit else Icons.Default.Save, contentDescription = null)
                }
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screens.Login.name)
                    },
                    modifier = Modifier.height(40.dp),
                ) {
                    Text(text = "Log Out!!")
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                }
            }
        }
    }
}


private fun saveProfileData(context: Context, firstName: String, lastName: String, picUrl: String, onSuccess: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    FirebaseFirestore.getInstance().collection("users")
        .document(userId)
        .update(
            "first_name", firstName,
            "last_name", lastName,
            "profile_picture", picUrl // Update the profile picture URL
        )
        .addOnSuccessListener {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            onSuccess() // Invoke success callback
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
        }
}

private fun updateProfilePicture(uri: Uri, onSuccess: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/$userId")
    val uploadTask = storageRef.putFile(uri)
    uploadTask.addOnFailureListener {
        // Handle unsuccessful uploads
    }.addOnSuccessListener { taskSnapshot ->
        taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
            onSuccess(downloadUri.toString())
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewProfile(navController: NavController = rememberNavController()) {
    Profile(navController = navController)
}

