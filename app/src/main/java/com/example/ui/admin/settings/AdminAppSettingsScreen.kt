package com.example.ui.admin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettingsItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseGradientButton
import com.example.ui.theme.PulsePrimary
import kotlinx.coroutines.launch

@Composable
fun AdminAppSettingsScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val settings by repository.appSettings.collectAsState()

    var maintenanceMode by remember(settings) { mutableStateOf(settings.maintenanceMode) }
    var registrationEnabled by remember(settings) { mutableStateOf(settings.registrationEnabled) }
    var videoUploadEnabled by remember(settings) { mutableStateOf(settings.videoUploadEnabled) }
    var maxDuration by remember(settings) { mutableStateOf(settings.maxVideoDurationSeconds.toFloat()) }
    var commentsEnabled by remember(settings) { mutableStateOf(settings.commentsEnabled) }
    var messagingEnabled by remember(settings) { mutableStateOf(settings.messagingEnabled) }
    var downloadsEnabled by remember(settings) { mutableStateOf(settings.downloadsEnabled) }

    var saveConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
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
                text = "Platform Configuration & Feature Flags",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AdminSettingToggle("Maintenance Mode", "Temporarily prevent general users from accessing Pulse", maintenanceMode) {
                maintenanceMode = it
            }

            AdminSettingToggle("User Registration", "Allow new users to sign up", registrationEnabled) {
                registrationEnabled = it
            }

            AdminSettingToggle("Video Upload Studio", "Allow creators to upload new short videos", videoUploadEnabled) {
                videoUploadEnabled = it
            }

            // Duration Slider
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Max Video Duration", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("${maxDuration.toInt()} seconds", fontWeight = FontWeight.Bold, color = PulsePrimary)
                }
                Slider(
                    value = maxDuration,
                    onValueChange = { maxDuration = it },
                    valueRange = 15f..300f,
                    steps = 18
                )
            }

            AdminSettingToggle("Comments System", "Globally enable comments across all videos", commentsEnabled) {
                commentsEnabled = it
            }

            AdminSettingToggle("Direct Messaging", "Globally enable 1-on-1 direct messaging", messagingEnabled) {
                messagingEnabled = it
            }

            AdminSettingToggle("Video Downloads", "Allow viewers to download videos to their device", downloadsEnabled) {
                downloadsEnabled = it
            }

            Spacer(modifier = Modifier.height(10.dp))

            PulseGradientButton(
                text = "Apply Global Changes",
                onClick = {
                    coroutineScope.launch {
                        val newSettings = AppSettingsItem(
                            maintenanceMode = maintenanceMode,
                            registrationEnabled = registrationEnabled,
                            videoUploadEnabled = videoUploadEnabled,
                            maxVideoDurationSeconds = maxDuration.toInt(),
                            commentsEnabled = commentsEnabled,
                            messagingEnabled = messagingEnabled,
                            downloadsEnabled = downloadsEnabled
                        )
                        repository.adminUpdateSettings(newSettings)
                        saveConfirmation = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_settings_button")
            )

            if (saveConfirmation) {
                Text(
                    text = "✓ Global Platform settings updated and logged to audit trail.",
                    color = PulsePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminSettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
