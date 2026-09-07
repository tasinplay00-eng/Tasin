package com.example.ui.admin.broadcast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseGradientButton
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSuccess
import kotlinx.coroutines.launch

@Composable
fun AdminBroadcastScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All Users") }
    var sendConfirmation by remember { mutableStateOf(false) }

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
                text = "Send Broadcast Notification",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Publish instant push alerts and in-app inbox announcements to users globally.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = broadcastTitle,
                onValueChange = {
                    broadcastTitle = it
                    sendConfirmation = false
                },
                label = { Text("Announcement Headline") },
                placeholder = { Text("e.g. Creator Fund 2026 Live!") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("broadcast_title_input")
            )

            OutlinedTextField(
                value = broadcastMessage,
                onValueChange = {
                    broadcastMessage = it
                    sendConfirmation = false
                },
                label = { Text("Message Body") },
                placeholder = { Text("Details of the update or community guideline milestone...") },
                minLines = 4,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("broadcast_message_input")
            )

            Text("Target Audience:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All Users", "Verified Only", "Creators").forEach { audience ->
                    FilterChip(
                        selected = targetAudience == audience,
                        onClick = { targetAudience = audience },
                        label = { Text(audience) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            PulseGradientButton(
                text = "Broadcast Now",
                onClick = {
                    if (broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank()) {
                        coroutineScope.launch {
                            repository.adminBroadcastNotification(
                                title = broadcastTitle.trim(),
                                message = broadcastMessage.trim(),
                                target = targetAudience
                            )
                            broadcastTitle = ""
                            broadcastMessage = ""
                            sendConfirmation = true
                        }
                    }
                },
                enabled = broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank(),
                icon = Icons.Default.Campaign,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_broadcast_button")
            )

            if (sendConfirmation) {
                Text(
                    text = "✓ Broadcast successfully dispatched to $targetAudience and recorded in audit log.",
                    color = PulseSuccess,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
