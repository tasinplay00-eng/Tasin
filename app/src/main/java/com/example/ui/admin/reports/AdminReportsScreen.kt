package com.example.ui.admin.reports

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseBadge
import com.example.ui.theme.PulseError
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseSuccess
import kotlinx.coroutines.launch

@Composable
fun AdminReportsScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val reports by repository.reports.collectAsState()
    var selectedFilter by remember { mutableStateOf("pending") }
    var activeReportForAction by remember { mutableStateOf<ReportItem?>(null) }
    var moderationNoteInput by remember { mutableStateOf("") }

    val filteredReports = remember(selectedFilter, reports) {
        if (selectedFilter == "all") reports
        else reports.filter { it.status == selectedFilter }
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
                text = "Reports & Content Violations",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("pending" to "Pending", "resolved" to "Resolved", "all" to "All").forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PulsePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (filteredReports.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "No reports found in this status.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredReports, key = { it.id }) { report ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeReportForAction = report
                                moderationNoteInput = report.moderationNote
                            }
                            .testTag("admin_report_item_${report.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PulseBadge(
                                    text = report.reason.uppercase(),
                                    color = if (report.status == "pending") PulseError else PulseSuccess
                                )
                                Text(
                                    text = report.createdAt,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Target: ${report.targetType.uppercase()} (#${report.targetId})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "Reported by @${report.reporterUsername}${if (report.reportedUsername != null) " against @${report.reportedUsername}" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (report.moderationNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Moderator Note: ${report.moderationNote}",
                                    fontSize = 12.sp,
                                    color = PulseSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Action Resolution Dialog
    if (activeReportForAction != null) {
        val rep = activeReportForAction!!
        AlertDialog(
            onDismissRequest = { activeReportForAction = null },
            title = { Text("Resolve Report (${rep.id})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Violation: ${rep.reason}", fontWeight = FontWeight.Bold, color = PulseError)
                    Text("Target: ${rep.targetType} (#${rep.targetId})")

                    OutlinedTextField(
                        value = moderationNoteInput,
                        onValueChange = { moderationNoteInput = it },
                        label = { Text("Moderator Note") },
                        placeholder = { Text("Reason for resolution / action...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminResolveReport(rep.id, "resolved", moderationNoteInput)
                                activeReportForAction = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PulseSuccess),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Resolved (Take Action)")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminResolveReport(rep.id, "rejected", moderationNoteInput)
                                activeReportForAction = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss as False Report")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeReportForAction = null }) { Text("Cancel") }
            }
        )
    }
}
