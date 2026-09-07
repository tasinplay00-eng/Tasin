package com.example.ui.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.model.UserProfile
import com.example.data.model.VideoItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.components.PulseFollowButton
import com.example.ui.components.PulseGradientButton
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.user.feed.formatCount
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String?,
    repository: PulseRepository,
    onVideoClick: (VideoItem) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val myId by SessionManager.currentUserId.collectAsState()
    val adminRole by SessionManager.adminRole.collectAsState()
    val targetId = userId ?: myId ?: "u_pulse_creator_01"
    val isMe = targetId == myId

    val users by repository.users.collectAsState()
    val allVideos by repository.videos.collectAsState()

    val profile = users.find { it.id == targetId } ?: UserProfile(
        id = targetId,
        username = "alex_pulse",
        displayName = "Alex Rivera",
        avatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
        bio = "Pulse Creator & Visual Storyteller ✨ Building short video magic!",
        followersCount = 1420,
        followingCount = 280,
        likesCount = 18500,
        isVerified = true
    )

    var selectedTab by remember { mutableStateOf("Videos") } // "Videos", "Liked", "Saved"
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var verificationReason by remember { mutableStateOf("") }
    var verificationLinks by remember { mutableStateOf("") }

    val userVideos = remember(targetId, allVideos) {
        allVideos.filter { it.userId == targetId }
    }
    val likedVideos = remember(allVideos) {
        allVideos.filter { it.isLiked }
    }
    val savedVideos = remember(allVideos) {
        allVideos.filter { it.isSaved }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar with Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "@${profile.username}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Admin Portal Gateway Button (if admin or super admin)
                if (isMe) {
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("portal_admin_switch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Portal",
                            tint = if (adminRole != AdminRole.NONE) PulseSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("profile_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Profile Details Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            PulseAvatar(
                imageUrl = profile.avatar,
                size = 90.dp,
                isVerified = profile.isVerified
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = profile.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row: Following, Followers, Likes
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileStatItem("Following", formatCount(profile.followingCount))
                ProfileStatItem("Followers", formatCount(profile.followersCount))
                ProfileStatItem("Likes", formatCount(profile.likesCount))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (isMe) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    OutlinedButton(
                        onClick = { showVerificationDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (profile.isVerified) "Verified ✓" else "Request Verification", fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    PulseFollowButton(
                        isFollowing = profile.isFollowing,
                        onToggle = { coroutineScope.launch { repository.toggleFollow(profile.id) } },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { showReportDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = "Report User")
                    }
                }
            }

            // Bio
            if (profile.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = profile.bio,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Tabs: Videos, Liked, Saved
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                "Videos" to Icons.Default.GridOn,
                "Liked" to Icons.Default.FavoriteBorder,
                "Saved" to Icons.Default.BookmarkBorder
            ).forEach { (tab, icon) ->
                val isSelected = selectedTab == tab
                IconButton(
                    onClick = { selectedTab = tab },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab,
                        tint = if (isSelected) PulsePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Video Grid
        val currentGridVideos = when (selectedTab) {
            "Liked" -> likedVideos
            "Saved" -> savedVideos
            else -> userVideos
        }

        if (currentGridVideos.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "No videos yet in $selectedTab",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(1.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currentGridVideos) { video ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .clickable { onVideoClick(video) }
                    ) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = formatCount(video.viewsCount),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Verification Request Dialog
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text("Apply for Pulse Verification") },
            text = {
                Column {
                    Text("Verified badges are awarded to authentic creators, artists, and public figures.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = verificationReason,
                        onValueChange = { verificationReason = it },
                        label = { Text("Category & Bio Reason") },
                        placeholder = { Text("e.g. Musician, Tech Reviewer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = verificationLinks,
                        onValueChange = { verificationLinks = it },
                        label = { Text("Portfolio / Social Links") },
                        placeholder = { Text("https://youtube.com/...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.submitVerificationRequest(verificationReason, verificationLinks)
                            showVerificationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePrimary)
                ) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerificationDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report User") },
            text = { Text("Report @${profile.username} for violating Pulse community guidelines?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.submitReport("user", profile.id, "Inappropriate content", profile.id)
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePrimary)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
