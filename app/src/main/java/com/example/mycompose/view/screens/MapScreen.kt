package com.example.mycompose.view.screens

import MapViewModel
import android.content.Context
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
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val coroutineScope = rememberCoroutineScope()
    val debounceTime = 500L  // Time in milliseconds for debouncing
    var debounceJob by remember { mutableStateOf<Job?>(null) }


    var showSuggestions by remember { mutableStateOf(false) }
    var selectedMarkerPosition by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Loading state

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            mapViewModel.fetchUserLocation(
                context,
                fusedLocationClient
            )
        }
    }

    // Observe camera position state
    val cameraPosition = mapViewModel.userLocation.value
    cameraPosition?.let {
        // Automatically update camera position when user location is available
        val newCameraPosition = com.google.android.gms.maps.model.CameraPosition.Builder()
            .target(it)
            .zoom(15f)
            .build()

        // Update camera position if no selected marker or suggestion is present
        if (selectedMarkerPosition == null) {
            cameraPositionState.position = newCameraPosition
        }
    }

    // Update searchText when placeName changes
    LaunchedEffect(placeName) {
        if (placeName.isNotEmpty()) {
            searchText = placeName // Update the searchText with the place name
        }
    }

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
                        // Clear any selected marker
                        selectedMarkerPosition = null
                        // Fetch location and update the map to the user's current location
                        mapViewModel.checkGpsAndFetchLocation(context, fusedLocationClient) {
                            searchText = it // Update searchText after fetching placeName
                        }
                    } else {
                        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.padding(34.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Locate Me")
            }
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            TextField(
                value = searchText,
                onValueChange = { input ->
                    searchText = input
                    if (input.isNotEmpty()) {
                        debounceJob?.cancel() // Cancel any ongoing job if the user types again
                        debounceJob = coroutineScope.launch {
                            delay(debounceTime) // Wait for the user to stop typing
                            mapViewModel.fetchLocationSuggestions(input, context)
                            showSuggestions = true
                        }
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

            // Suggestion popup
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
                                            searchText = suggestion.fullText
                                            showSuggestions = false
                                            mapViewModel.selectSuggestedLocation(suggestion, context)
                                            suggestion.latLng?.let { latLng ->
                                                selectedMarkerPosition = latLng // Ensure latLng is not null
                                                cameraPositionState.move(
                                                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                                )
                                            }
                                        }
                                        .padding(8.dp)
                                ) {
                                    Text(text = suggestion.fullText, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            // Show loading indicator while fetching place name
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    // Reset selected marker when the user clicks on the map
                    selectedMarkerPosition = latLng
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                    isLoading = true // Set loading to true while fetching the place name
                    mapViewModel.getPlaceNameForLatLng(context, latLng)
                    isLoading = false // Set loading to false after fetching the place name
                    // Update searchText with the fetched place name
                    val placeName = mapViewModel.placeName.value
                    searchText = placeName // Update searchText with the place name
                }
            ) {
                // Show the selected marker if it's not null
                selectedMarkerPosition?.let { position ->
                    Marker(
                        state = MarkerState(position = position),
                        title = "Selected Location",
                        snippet = "Lat: ${position.latitude}, Lng: ${position.longitude}"
                    )
                }

                // Show the user's current location marker if no marker is selected
                if (selectedMarkerPosition == null) {
                    mapViewModel.userLocation.value?.let { position ->
                        Marker(
                            state = MarkerState(position = position),
                            title = "Your Location",
                            snippet = "This is where you are currently located."
                        )
                    }
                }
            }
        }
    }
}
