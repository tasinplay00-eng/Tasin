package com.example.ui.user.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.HashtagItem
import com.example.data.model.SoundItem
import com.example.data.model.UserProfile
import com.example.data.model.VideoItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.components.PulseFollowButton
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseDarkBorder
import com.example.ui.user.feed.formatCount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(
    repository: PulseRepository,
    onVideoClick: (VideoItem) -> Unit,
    onUserClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onSoundClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Top") }

    val videos by repository.videos.collectAsState()
    val users by repository.users.collectAsState()
    val hashtags by repository.hashtags.collectAsState()
    val sounds by repository.sounds.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Debounced search query
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery.trim().lowercase()
    }

    val isSearching = debouncedQuery.isNotBlank()

    val filteredVideos = remember(debouncedQuery, videos) {
        if (debouncedQuery.isBlank()) videos
        else videos.filter {
            it.caption.lowercase().contains(debouncedQuery) ||
            it.creatorUsername.lowercase().contains(debouncedQuery) ||
            it.hashtags.any { tag -> tag.lowercase().contains(debouncedQuery) }
        }
    }

    val filteredUsers = remember(debouncedQuery, users) {
        if (debouncedQuery.isBlank()) users
        else users.filter {
            it.username.lowercase().contains(debouncedQuery) ||
            it.displayName.lowercase().contains(debouncedQuery)
        }
    }

    val filteredSounds = remember(debouncedQuery, sounds) {
        if (debouncedQuery.isBlank()) sounds
        else sounds.filter {
            it.title.lowercase().contains(debouncedQuery) ||
            it.artist.lowercase().contains(debouncedQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Search Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search creators, sounds, hashtags...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PulseDarkBorder, RoundedCornerShape(24.dp))
                    .heightIn(min = 46.dp)
                    .testTag("discover_search_input")
            )
        }

        // Filter Chips if searching
        if (isSearching) {
            val filters = listOf("Top", "Users", "Videos", "Sounds")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
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
        }

        if (!isSearching) {
            // Default Discover Layout: Trending Tags, Featured Creators, Trending Videos Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Trending Hashtags Carousel
                item {
                    Text(
                        text = "Trending Hashtags 🔥",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(hashtags) { tag ->
                            HashtagPill(tag = tag, onClick = { onHashtagClick(tag.name) })
                        }
                    }
                }

                // Suggested Creators Carousel
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Featured Creators ✨",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(users.take(5)) { user ->
                            CreatorCard(
                                user = user,
                                onUserClick = { onUserClick(user.id) },
                                onFollowToggle = {
                                    coroutineScope.launch { repository.toggleFollow(user.id) }
                                }
                            )
                        }
                    }
                }

                // Trending Videos Grid
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Trending Now 🚀",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )
                }

                // 2-column video feed items
                items(videos.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { video ->
                            DiscoverVideoTile(
                                video = video,
                                onClick = { onVideoClick(video) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Search Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedFilter in listOf("Top", "Users") && filteredUsers.isNotEmpty()) {
                    item {
                        Text(
                            text = "Creators",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(filteredUsers) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserClick(user.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            PulseAvatar(imageUrl = user.avatar, size = 48.dp, isVerified = user.isVerified)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "@${user.username} • ${formatCount(user.followersCount)} followers",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            PulseFollowButton(
                                isFollowing = user.isFollowing,
                                onToggle = {
                                    coroutineScope.launch { repository.toggleFollow(user.id) }
                                }
                            )
                        }
                    }
                }

                if (selectedFilter in listOf("Top", "Sounds") && filteredSounds.isNotEmpty()) {
                    item {
                        Text(
                            text = "Sounds",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(filteredSounds) { sound ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSoundClick(sound.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            AsyncImage(
                                model = sound.cover,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sound.title,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${sound.artist} • ${formatCount(sound.usageCount)} videos",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = PulsePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                if (selectedFilter in listOf("Top", "Videos") && filteredVideos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Videos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(filteredVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVideoClick(video) }
                                .padding(vertical = 4.dp)
                        ) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "@${video.creatorUsername} • ${formatCount(video.viewsCount)} views",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HashtagPill(tag: HashtagItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, PulseDarkBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${tag.name}",
                fontWeight = FontWeight.Bold,
                color = PulseSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatCount(tag.usageCount),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreatorCard(
    user: UserProfile,
    onUserClick: () -> Unit,
    onFollowToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PulseDarkBorder),
        modifier = Modifier
            .width(130.dp)
            .clickable { onUserClick() }
            .testTag("creator_card_${user.username}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            PulseAvatar(imageUrl = user.avatar, size = 52.dp, isVerified = user.isVerified)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = user.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "@${user.username}",
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PulseFollowButton(
                isFollowing = user.isFollowing,
                onToggle = onFollowToggle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DiscoverVideoTile(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(230.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, PulseDarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient with views count
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatCount(video.viewsCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
