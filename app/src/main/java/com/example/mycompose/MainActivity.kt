package com.example.mycompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.ui.theme.MycomposeTheme
import com.example.mycompose.view.screens.Home
import com.example.mycompose.view.screens.ProfileApp
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycomposeTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    // Initialize Firebase (shouldn't be needed unless it's not initialized elsewhere)
                    // Fetch cities where state_code = "72" from Firebase
                    val database = Firebase.database
                    val myRef = database.getReference("cities")

                    // Set up the query
                    val query = myRef.orderByChild("state_code").equalTo("72")

                    Log.d("MainActivity", "Starting Firebase query...")

                    // Query the database
                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Log.d("FirebaseCities", "Found cities with state_code 72")
                                for (citySnapshot in snapshot.children) {
                                    val city = citySnapshot.getValue(City::class.java)
                                    Log.d("FirebaseCities", "City name: ${city?.name}, state_code: ${city?.state_code}")
                                }
                            } else {
                                Log.d("FirebaseCities", "No cities found with state_code 72")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Error fetching data: ${error.message}")
                        }
                    })

                    ProfileApp(rememberNavController())
                    println("main calisti")



                } }
        }
    }
}
data class City(
    val name: String? = null,
    val state_code: String? = null
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MycomposeTheme {
        // Preview content here
    }
}

