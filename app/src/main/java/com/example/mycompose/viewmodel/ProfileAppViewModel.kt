package com.example.mycompose.viewmodel

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mycompose.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileAppViewModel : ViewModel() {
    var startingScreen by mutableStateOf(Screens.Login.name)
    var showBottomBar by mutableStateOf(false)
    var isLoading by mutableStateOf(true)

    fun loadUserProfile(context: android.content.Context) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
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
                            showBottomBar = true
                            Screens.Home.name
                        }
                        else -> {
                            showBottomBar = false
                            Screens.CompleteProfileScreen.name
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error retrieving profile information.", Toast.LENGTH_SHORT).show()
                    startingScreen = Screens.Login.name
                    showBottomBar = false
                    isLoading = false
                }
        } else {
            startingScreen = Screens.Login.name
            showBottomBar = false
            isLoading = false
        }
    }
}
