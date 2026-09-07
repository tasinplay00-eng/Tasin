package com.example.ui.user.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SoundItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseGradientButton
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoScreen(
    repository: PulseRepository,
    onUploadSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val sounds by repository.sounds.collectAsState()
    val hashtagsList by repository.hashtags.collectAsState()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var selectedSound by remember { mutableStateOf(sounds.firstOrNull()?.title ?: "Original Sound") }
    var selectedVisibility by remember { mutableStateOf("public") } // public, followers, private
    var allowComments by remember { mutableStateOf(true) }
    var allowDownload by remember { mutableStateOf(true) }
    var trimRange by remember { mutableStateOf(0f..30f) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }

    // Modern Zero-Permission Photo/Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
            }
            Text(
                text = "New Pulse Video",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Empty placeholder for balance
            Spacer(modifier = Modifier.width(48.dp))
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Video Selection or Preview Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
                .testTag("video_selector_container")
        ) {
            if (selectedVideoUri != null) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
                    contentDescription = "Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Video Selected (Tap to Change)", color = Color.White, fontSize = 12.sp)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Select Video",
                        tint = PulsePrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose Video from Device",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "MP4, WebM up to 100MB",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Caption Input
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Caption & Description",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = caption,
                onValueChange = { if (it.length <= 300) caption = it },
                placeholder = { Text("Write a compelling hook or caption...") },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caption_input_field")
            )
            Text(
                text = "${caption.length}/300",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }

        // Hashtags Quick Adder
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(
                text = "Add Trending Tags",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(hashtagsList) { tag ->
                    AssistChip(
                        onClick = {
                            if (!caption.contains("#${tag.name}")) {
                                caption = if (caption.isBlank()) "#${tag.name} " else "$caption #${tag.name} "
                            }
                        },
                        label = { Text("#${tag.name}", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }
        }

        // Audio/Sound Selection
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Music & Sound",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sounds) { sound ->
                    val isSelected = selectedSound == sound.title
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSound = sound.title },
                        label = { Text(sound.title, maxLines = 1, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PulsePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Visibility Options
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Who can watch this video?",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("public" to "Public", "followers" to "Followers", "private" to "Only Me").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedVisibility == key,
                        onClick = { selectedVisibility = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PulseSecondary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        // Permissions Toggles
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Allow Comments", color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = allowComments, onCheckedChange = { allowComments = it })
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Allow Downloads", color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = allowDownload, onCheckedChange = { allowDownload = it })
            }
        }

        // Upload Progress Indicator
        AnimatedVisibility(visible = isUploading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Uploading to Pulse Cloud... ${(uploadProgress * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = PulsePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PulsePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Publish Button
        PulseGradientButton(
            text = if (isUploading) "Uploading..." else "Publish Video",
            onClick = {
                isUploading = true
                coroutineScope.launch {
                    // Simulate progressive chunk upload
                    for (i in 1..10) {
                        uploadProgress = i / 10f
                        delay(120)
                    }
                    val extractedTags = caption.split(" ")
                        .filter { it.startsWith("#") }
                        .map { it.replace("#", "").trim() }
                        .ifEmpty { listOf("pulsecreator") }

                    repository.uploadVideo(
                        videoUrl = selectedVideoUri?.toString() ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                        thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
                        caption = caption.ifBlank { "Check out my new pulse video! ✨ #pulsecreator" },
                        description = "Uploaded via Pulse Studio",
                        hashtags = extractedTags,
                        soundTitle = selectedSound,
                        visibility = selectedVisibility,
                        allowComments = allowComments,
                        allowDownload = allowDownload
                    )
                    isUploading = false
                    onUploadSuccess()
                }
            },
            enabled = !isUploading,
            icon = Icons.Default.CloudUpload,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}
