package com.example.ui.admin.verification

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
import com.example.data.model.VerificationRequestItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.components.PulseBadge
import com.example.ui.theme.PulseError
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseSuccess
import kotlinx.coroutines.launch

@Composable
fun AdminVerificationScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val requests by repository.verificationRequests.collectAsState()

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
                text = "Creator Verification Requests",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        if (requests.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "No verification requests at this time.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(requests, key = { it.id }) { req ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_request_${req.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulseAvatar(imageUrl = req.avatar, size = 46.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = req.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "@${req.username}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                PulseBadge(
                                    text = req.status.uppercase(),
                                    color = when (req.status) {
                                        "approved" -> PulseSuccess
                                        "rejected" -> PulseError
                                        else -> PulseSecondary
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Category / Reason: ${req.reason}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (req.supportingInfo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Links: ${req.supportingInfo}",
                                    fontSize = 12.sp,
                                    color = PulseSecondary
                                )
                            }

                            if (req.status == "pending") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.adminResolveVerification(req.id, true)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PulseSuccess),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Approve & Verify")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.adminResolveVerification(req.id, false)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Decline")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
