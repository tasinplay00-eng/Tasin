package com.example.ui.admin.content

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseBadge
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.user.feed.formatCount
import kotlinx.coroutines.launch

@Composable
fun AdminContentLibraryScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf("Hashtags") } // "Hashtags" or "Sounds"

    val hashtags by repository.hashtags.collectAsState()
    val sounds by repository.sounds.collectAsState()

    var showAddHashtagDialog by remember { mutableStateOf(false) }
    var newHashtagName by remember { mutableStateOf("") }

    var showAddSoundDialog by remember { mutableStateOf(false) }
    var newSoundTitle by remember { mutableStateOf("") }
    var newSoundArtist by remember { mutableStateOf("") }
    var newSoundUrl by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Hashtags & Sounds Library",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = {
                if (activeTab == "Hashtags") showAddHashtagDialog = true else showAddSoundDialog = true
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Content", tint = PulsePrimary)
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Hashtags", "Sounds").forEach { tab ->
                FilterChip(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    label = { Text(tab) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PulsePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (activeTab == "Hashtags") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(hashtags, key = { it.id }) { tag ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "#${tag.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PulseSecondary
                                )
                                Text(
                                    text = "${formatCount(tag.usageCount)} video usages",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (tag.isFeatured) {
                                PulseBadge(text = "FEATURED", color = PulsePrimary)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sounds, key = { it.id }) { sound ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = PulsePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sound.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${sound.artist} • ${formatCount(sound.usageCount)} uses",
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

    if (showAddHashtagDialog) {
        AlertDialog(
            onDismissRequest = { showAddHashtagDialog = false },
            title = { Text("Create Official Hashtag") },
            text = {
                OutlinedTextField(
                    value = newHashtagName,
                    onValueChange = { newHashtagName = it },
                    label = { Text("Hashtag Name") },
                    placeholder = { Text("pulsechallenge2026") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newHashtagName.isNotBlank()) {
                        coroutineScope.launch {
                            repository.adminAddHashtag(newHashtagName)
                            newHashtagName = ""
                            showAddHashtagDialog = false
                        }
                    }
                }) {
                    Text("Add Tag")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHashtagDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddSoundDialog) {
        AlertDialog(
            onDismissRequest = { showAddSoundDialog = false },
            title = { Text("Add Licensed Audio Track") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newSoundTitle,
                        onValueChange = { newSoundTitle = it },
                        label = { Text("Sound Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSoundArtist,
                        onValueChange = { newSoundArtist = it },
                        label = { Text("Artist Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSoundUrl,
                        onValueChange = { newSoundUrl = it },
                        label = { Text("Audio Stream URL") },
                        placeholder = { Text("https://actions.google.com/...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newSoundTitle.isNotBlank()) {
                        coroutineScope.launch {
                            repository.adminAddSound(
                                title = newSoundTitle,
                                artist = newSoundArtist.ifBlank { "Pulse Records" },
                                audioUrl = newSoundUrl.ifBlank { "https://actions.google.com/sounds/v1/science_fiction/synth_pulse.ogg" }
                            )
                            newSoundTitle = ""
                            newSoundArtist = ""
                            newSoundUrl = ""
                            showAddSoundDialog = false
                        }
                    }
                }) {
                    Text("Add Sound")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSoundDialog = false }) { Text("Cancel") }
            }
        )
    }
}
