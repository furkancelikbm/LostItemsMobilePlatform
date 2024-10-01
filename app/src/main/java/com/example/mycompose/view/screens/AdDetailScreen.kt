import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.mycompose.model.AdModel
import com.example.mycompose.model.UserProfile
import com.example.mycompose.repository.AdRepository
import com.example.mycompose.repository.ProfileRepository
import com.google.accompanist.pager.*

@Composable
fun AdDetailScreen(
    navController: NavController,
    adId: String,
    adRepository: AdRepository,
    profileRepository: ProfileRepository // Add ProfileRepository to fetch user profile
) {
    var ad by remember { mutableStateOf<AdModel?>(null) }
    var selectedImage by remember { mutableStateOf<String?>(null) } // For fullscreen image
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }


    // Fetch ad details and user profile
    LaunchedEffect(adId) {
        try {
            ad = adRepository.getAdById(adId)
            userProfile = ad?.userId?.let { userId->
                profileRepository.getUserProfileByAdUserId(userId)
            } // Fetch user profile
        } catch (e: Exception) {
            Log.e("AdDetailScreen", "Error fetching data: ${e.message}")
        }
    }

    if (ad != null && userProfile != null) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            val pagerState = rememberPagerState()

            // Horizontal image pager
            HorizontalPager(
                count = ad!!.imageUrls.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clickable {
                            // When image is clicked, store the image URL for fullscreen view
                            selectedImage = ad!!.imageUrls[page]
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(data = ad!!.imageUrls[page]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dots as indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(ad!!.imageUrls.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ad title
            Text(
                text = ad!!.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Location
            Text(
                text = "Location: ${ad!!.location}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ad description
            Text(
                text = ad!!.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Profile picture
                Image(
                    painter = rememberImagePainter(data = userProfile!!.profilePicture),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                // First and Last name
                Column {
                    Text(
                        text = "${userProfile!!.firstName} ${userProfile!!.lastName}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom
            // Get current user ID
            val receiverId = userProfile!!.userId
            val senderId = profileRepository.getCurrentUserId()

            // Show Send Message button only if senderId is not equal to receiverId
            if (senderId != receiverId) {
                Button(
                    onClick = {
                        navController.navigate("message/${adId}/${receiverId}/${senderId}")
                        Log.d("MessageScreen", "Sender: $senderId, Receiver: $receiverId")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Send Message", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }

            // Fullscreen image dialog
            if (selectedImage != null) {
                Dialog(
                    onDismissRequest = { selectedImage = null }, // Close dialog when clicked outside
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false // Make dialog fullscreen
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)) // Transparent background
                            .clickable { selectedImage = null } // Close dialog on background click
                    ) {
                        Image(
                            painter = rememberImagePainter(data = selectedImage),
                            contentDescription = null,
                            contentScale = ContentScale.Fit, // Image fits the screen
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    } else {
        Text(text = "Loading...", modifier = Modifier.padding(16.dp))
    }
}
