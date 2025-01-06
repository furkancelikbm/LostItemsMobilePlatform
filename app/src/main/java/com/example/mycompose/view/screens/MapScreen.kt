package com.example.mycompose.view.screens

import MapViewModel
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val mapViewModel: MapViewModel = viewModel()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState()
    val focusRequester = remember { FocusRequester() }
    var searchText by remember { mutableStateOf("") }
    val suggestions by mapViewModel.locationSuggestions
    val placeName by mapViewModel.placeName

    var showSuggestions by remember { mutableStateOf(false) }
    var selectedMarkerPosition by remember { mutableStateOf<LatLng?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            mapViewModel.fetchUserLocation(
                context,
                fusedLocationClient,
                cameraPositionState
            ) { place ->
                searchText = place
            }
        }
    }

    // Observe camera position state
    val cameraPosition = mapViewModel.cameraPositionState.value
    cameraPositionState.position = cameraPosition

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                        if (isGpsEnabled) {
                            mapViewModel.fetchUserLocation(context, fusedLocationClient, cameraPositionState) {
                                searchText = placeName
                            }
                        } else {
                            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        }
                    } else {
                        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.padding(34.dp)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Center on Location")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TextField(
                value = searchText,
                onValueChange = { input ->
                    searchText = input
                    if (input.isNotEmpty()) {
                        mapViewModel.fetchLocationSuggestions(input, context)
                        showSuggestions = true
                    } else {
                        showSuggestions = false
                    }
                },
                placeholder = { Text("Search location") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusRequester.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            if (showSuggestions && suggestions.isNotEmpty()) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, 100),
                    properties = PopupProperties(dismissOnClickOutside = true, focusable = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        LazyColumn {
                            items(suggestions) { suggestion ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchText = suggestion.description
                                            showSuggestions = false
                                            mapViewModel.selectSuggestedLocation(suggestion, cameraPositionState, context)
                                        }
                                        .padding(8.dp)
                                ) {
                                    Text(text = suggestion.description, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    // Update the selected marker position on map click
                    selectedMarkerPosition = latLng
                    // Slow down the camera transition using animateCamera method
                    cameraPositionState.move(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f) // Zoom level 15f, adjust as needed
                    )
                }
            ) {
                // Using LaunchedEffect to launch the coroutine in a composable context when selectedMarkerPosition changes
                LaunchedEffect(selectedMarkerPosition) {
                    selectedMarkerPosition?.let { latLng ->
                        val name = mapViewModel.fetchPlaceName(context, latLng)
                        name?.let {
                            searchText = it // Update search bar with place name
                        }
                    }
                }

                // Display the selected marker if selectedMarkerPosition is not null
                selectedMarkerPosition?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Selected Location",
                        snippet = "Lat: ${it.latitude}, Lng: ${it.longitude}"
                    )
                }

                // If selectedMarkerPosition is null, display the user's location marker
                if (selectedMarkerPosition == null) {
                    mapViewModel.userLocation.value?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Your Location",
                            snippet = "This is where you are currently located."
                        )
                    }
                }
            }

        }
}
}

