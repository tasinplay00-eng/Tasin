package com.example.ui.admin.videos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseBadge
import com.example.ui.theme.PulseError
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseSuccess
import com.example.ui.user.feed.formatCount
import kotlinx.coroutines.launch

@Composable
fun AdminVideosScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val videos by repository.videos.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedVideoForAction by remember { mutableStateOf<VideoItem?>(null) }

    val filteredVideos = remember(selectedFilter, videos) {
        when (selectedFilter) {
            "Hidden" -> videos.filter { it.moderationStatus == "hidden" }
            "Featured" -> videos.filter { it.isFeatured }
            else -> videos
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Video Moderation Queue",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Hidden", "Featured").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PulsePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredVideos, key = { it.id }) { video ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedVideoForAction = video }
                        .testTag("admin_video_item_${video.id}")
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(74.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${video.creatorUsername}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (video.moderationStatus == "hidden") {
                                    PulseBadge(text = "HIDDEN", color = PulseError)
                                } else if (video.isFeatured) {
                                    PulseBadge(text = "FEATURED", color = PulseSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = video.caption,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${formatCount(video.viewsCount)} views • ${formatCount(video.likesCount)} likes • ${video.commentsCount} comments",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = { selectedVideoForAction = video }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                    }
                }
            }
        }
    }

    // Video Action Dialog
    if (selectedVideoForAction != null) {
        val target = selectedVideoForAction!!
        AlertDialog(
            onDismissRequest = { selectedVideoForAction = null },
            title = { Text("Moderate Video (${target.id})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(target.caption, fontSize = 13.sp)

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminToggleVideoFeatured(target.id)
                                selectedVideoForAction = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PulseSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (target.isFeatured) "Remove from Featured" else "Mark as Featured Video", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val nextStatus = if (target.moderationStatus == "hidden") "approved" else "hidden"
                                repository.adminUpdateVideoStatus(target.id, nextStatus)
                                selectedVideoForAction = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (target.moderationStatus == "hidden") PulseSuccess else PulseError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (target.moderationStatus == "hidden") "Restore Video to Feed" else "Hide Video from Public Feed")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminDeleteVideo(target.id)
                                selectedVideoForAction = null
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PulseError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permanently Delete Video")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedVideoForAction = null }) { Text("Close") }
            }
        )
    }
}
