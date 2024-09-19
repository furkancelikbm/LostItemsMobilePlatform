package com.example.mycompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideScreen(
    navController: NavController,
    viewModel: RideViewModel = viewModel()
) {
    val pickupLocationPlaces by viewModel.pickupLocationPlaces.collectAsStateWithLifecycle()

    // Use a Box to center the entire content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shadowElevation = 5.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextField(
                        value = viewModel.pickUp,
                        onValueChange = viewModel::onPickUpValueChanged,
                        placeholder = {
                            Text(text = "Pickup Location")
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
            LazyColumn {
                items(pickupLocationPlaces) { place ->
                    ListItem(
                        headlineContent = {
                            Text(text = place.name)
                        },
                        modifier = Modifier.clickable {
                            viewModel.onPlaceClick(place.name)
                        }
                    )
                }
            }
        }
    }
}
