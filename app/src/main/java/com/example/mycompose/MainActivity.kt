package com.example.mycompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.navigation.Navigation
import com.example.mycompose.navigation.Screens
import com.example.mycompose.ui.theme.MycomposeTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycomposeTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    ProfileApp()
                }
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
fun ProfileApp(navController: NavHostController = rememberNavController()) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val startingScreen = if (firebaseUser == null) {
        Screens.Login.name
    } else {
        Screens.Home.name // Updated to 'Home' for direct access
    }

    Navigation(navController = navController, startingScreen = startingScreen)
}
