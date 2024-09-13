package com.example.mycompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.NavigationBar.BottomNavigationBar
import com.example.mycompose.NavigationBar.NavigationViewModel
import com.example.mycompose.navigation.Navigation
import com.example.mycompose.navigation.Screens
import com.example.mycompose.ui.theme.MycomposeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycomposeTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    ProfileApp()
                    println("main calisti")
                }
                fun oylesine(){}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MycomposeTheme {
        // Preview content here
    }
}
@Composable
fun ProfileApp() {
    val navController = rememberNavController()
    val navigationViewModel: NavigationViewModel = viewModel()
    val context = LocalContext.current

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    var startingScreen by remember { mutableStateOf(Screens.Login.name) }
    var showBottomBar by remember { mutableStateOf(false) } // State to toggle BottomNavigationBar
    var isLoading by remember { mutableStateOf(true) } // State for loading indicator

    // Load user profile data to determine starting screen
    LaunchedEffect(firebaseUser) {
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("first_name").orEmpty()
                    val lastName = document.getString("last_name").orEmpty()
                    val picUrl = document.getString("profile_picture").orEmpty()

                    startingScreen = when {
                        firstName.isNotEmpty() && lastName.isNotEmpty() && picUrl.isNotEmpty() -> {
                            showBottomBar = true // Show BottomNavigationBar if profile is complete
                            Screens.Home.name
                        }
                        else -> {
                            showBottomBar = false
                            Screens.CompleteProfileScreen.name
                        }
                    }
                    isLoading = false // Stop showing progress indicator when data is loaded
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error retrieving profile information.", Toast.LENGTH_SHORT).show()
                    startingScreen = Screens.Login.name
                    showBottomBar = false
                    isLoading = false // Stop loading if an error occurs
                }
        } else {
            startingScreen = Screens.Login.name
            showBottomBar = false
            isLoading = false // Stop loading if no user is logged in
        }
    }

    if (isLoading) {
        // Show CircularProgressIndicator while loading
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Show content when loading is complete
        Scaffold(
            bottomBar = {
                if (showBottomBar) { // Show BottomNavigationBar only when user is on Home screen
                    BottomNavigationBar(navController = navController, viewModel = navigationViewModel)
                }
            }
        ) { padding ->
            Navigation(
                navController = navController,
                startingScreen = startingScreen,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

