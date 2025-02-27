import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.mycompose.model.Place

@Composable
fun LocationInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    locations: List<Place>,
    onLocationClick: (Place) -> Unit,
    checkAndFirstPlace: () -> Unit
) {
    var showSuggestions by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // TextField for input
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                    showSuggestions = true // Show suggestions as user types
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            showSuggestions = false // Hide suggestions when not focused
                            checkAndFirstPlace() // Check and select the first place when focus is lost
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (value.text.isEmpty()) {
                            Text(text = placeholder, color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Popup for displaying location suggestions
        if (showSuggestions && locations.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 100), // Offset adjusted for better placement below TextField
                properties = PopupProperties(
                    dismissOnClickOutside = true,
                    focusable = false // Allows user to continue typing after suggestions pop up
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    LazyColumn {
                        items(locations) { place ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLocationClick(place)
                                        showSuggestions = false // Hide suggestions after selection
                                    }
                                    .padding(8.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = place.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
