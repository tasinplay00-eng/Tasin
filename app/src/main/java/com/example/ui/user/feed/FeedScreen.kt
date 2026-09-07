package com.example.ui.user.feed

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.components.PulseVideoSurface
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    repository: PulseRepository,
    onOpenComments: (String) -> Unit,
    onOpenShare: (VideoItem) -> Unit,
    onCreatorClick: (String) -> Unit,
    onSoundClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("For You") }
    val allVideos by repository.videos.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val filteredVideos = remember(selectedTab, allVideos) {
        when (selectedTab) {
            "Following" -> allVideos.filter { it.isFollowingCreator }
                .ifEmpty { allVideos.take(2) }
            "Trending" -> allVideos.sortedByDescending { it.likesCount + it.viewsCount }
            else -> allVideos // "For You"
        }
    }

    val pagerState = rememberPagerState(pageCount = { filteredVideos.size })

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (filteredVideos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos in this feed yet.", color = Color.White)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = filteredVideos[page]
                val isPageActive = pagerState.currentPage == page

                LaunchedEffect(isPageActive) {
                    if (isPageActive) {
                        repository.recordView(video.id)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Video Surface with double-tap like
                    PulseVideoSurface(
                        video = video,
                        isActive = isPageActive,
                        onDoubleTapLike = {
                            coroutineScope.launch { repository.toggleLike(video.id) }
                        }
                    )

                    // Right Side Floating Interaction Actions
                    FeedSideActions(
                        video = video,
                        onLike = { coroutineScope.launch { repository.toggleLike(video.id) } },
                        onComment = { onOpenComments(video.id) },
                        onSave = { coroutineScope.launch { repository.toggleSave(video.id) } },
                        onShare = { onOpenShare(video) },
                        onFollow = { coroutineScope.launch { repository.toggleFollow(video.userId) } },
                        onCreatorClick = { onCreatorClick(video.userId) },
                        onSoundClick = { video.soundId?.let(onSoundClick) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 90.dp)
                    )

                    // Bottom Creator & Video Info Overlay
                    FeedVideoInfoOverlay(
                        video = video,
                        onCreatorClick = { onCreatorClick(video.userId) },
                        onSoundClick = { video.soundId?.let(onSoundClick) },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 90.dp, bottom = 90.dp)
                    )
                }
            }
        }

        // Top Navigation Tabs (Following | For You | Trending)
        FeedTopTabs(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun FeedTopTabs(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Following", "For You", "Trending")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            } else Modifier
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FeedSideActions(
    video: VideoItem,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onFollow: () -> Unit,
    onCreatorClick: () -> Unit,
    onSoundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
    ) {
        // Creator Avatar with Follow Plus Badge
        Box(contentAlignment = Alignment.Center) {
            PulseAvatar(
                imageUrl = video.creatorAvatar,
                size = 48.dp,
                isVerified = video.isCreatorVerified,
                onClick = onCreatorClick
            )
            if (!video.isFollowingCreator) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp)
                        .clip(CircleShape)
                        .background(PulsePrimary)
                        .border(1.5.dp, Color.Black, CircleShape)
                        .clickable { onFollow() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Follow",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Like Button
        FeedActionButton(
            icon = if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = formatCount(video.likesCount),
            tint = if (video.isLiked) PulsePrimary else Color.White,
            onClick = onLike,
            testTag = "feed_like_button"
        )

        // Comment Button
        FeedActionButton(
            icon = Icons.Default.ChatBubbleOutline,
            label = formatCount(video.commentsCount),
            tint = Color.White,
            onClick = onComment,
            testTag = "feed_comment_button"
        )

        // Save / Bookmark Button
        FeedActionButton(
            icon = if (video.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            label = formatCount(video.savesCount),
            tint = if (video.isSaved) PulseSecondary else Color.White,
            onClick = onSave,
            testTag = "feed_bookmark_button"
        )

        // Share Button
        FeedActionButton(
            icon = Icons.Default.Share,
            label = formatCount(video.sharesCount),
            tint = Color.White,
            onClick = onShare,
            testTag = "feed_share_button"
        )

        // Rotating Music Disc with neon border
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(Color(0xFF0F1018))
                .border(1.5.dp, Brush.sweepGradient(listOf(PulsePrimary, PulseSecondary, PulsePrimary)), CircleShape)
                .clickable { onSoundClick() }
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = "Sound Disc",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun FeedActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.38f))
                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FeedVideoInfoOverlay(
    video: VideoItem,
    onCreatorClick: () -> Unit,
    onSoundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Handle & Verification
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onCreatorClick() }
        ) {
            Text(
                text = "@${video.creatorUsername}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (video.isCreatorVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = PulseSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Caption & Hashtags
        Text(
            text = video.caption,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 14.sp,
            maxLines = if (isExpanded) 10 else 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        )

        if (video.hashtags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                video.hashtags.take(3).forEach { tag ->
                    Text(
                        text = "#$tag",
                        color = PulseSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sound Information Marquee/Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .clickable { onSoundClick() }
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Audio",
                tint = PulseSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${video.soundTitle} • ${video.soundArtist}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
