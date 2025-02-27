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
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.ui.theme.MycomposeTheme
import com.example.mycompose.view.screens.ProfileApp
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycomposeTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    FirebaseApp.initializeApp(this) //ne ise yarıyor ekledim öylesine

                    ProfileApp(rememberNavController())
                    println("main calisti")
                } }
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

