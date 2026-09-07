package com.example.ui.admin.admins

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
import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.model.AdminUserItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseBadge
import com.example.ui.theme.PulseError
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseSuccess
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val adminUsers by repository.adminUsers.collectAsState()
    val currentAdminEmail by SessionManager.currentUserEmail.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newAdminEmail by remember { mutableStateOf("") }
    var newAdminRole by remember { mutableStateOf(AdminRole.MODERATOR) }

    var selectedAdminForAction by remember { mutableStateOf<AdminUserItem?>(null) }

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
                    text = "Administrator Access & RBAC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_admin_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Admin", tint = PulsePrimary)
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Super Admin Privilege Notice
        Card(
            colors = CardDefaults.cardColors(containerColor = PulsePrimary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PulsePrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Super Admin Exclusive: Only super_admin can add, reassign roles, or deactivate administrator accounts.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(adminUsers, key = { it.id }) { admin ->
                val isSelf = admin.email.equals(currentAdminEmail, ignoreCase = true)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAdminForAction = admin }
                        .testTag("admin_user_row_${admin.email}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = admin.email,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (isSelf) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PulseBadge(text = "YOU", color = PulseSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PulseBadge(
                                    text = admin.role.displayName,
                                    color = if (admin.role == AdminRole.SUPER_ADMIN) PulsePrimary else PulseSecondary
                                )
                                if (!admin.isActive) {
                                    PulseBadge(text = "DISABLED", color = PulseError)
                                }
                            }
                        }

                        IconButton(onClick = { selectedAdminForAction = admin }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Role")
                        }
                    }
                }
            }
        }
    }

    // Add Admin Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Grant Administrator Access") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newAdminEmail,
                        onValueChange = { newAdminEmail = it },
                        label = { Text("User Email") },
                        placeholder = { Text("staff@pulse.social") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Role Level:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    listOf(AdminRole.ADMIN, AdminRole.MODERATOR, AdminRole.SUPPORT).forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newAdminRole = role }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = newAdminRole == role,
                                onClick = { newAdminRole = role }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${role.displayName} (Level ${role.level})")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAdminEmail.isNotBlank()) {
                            coroutineScope.launch {
                                repository.adminAddAdmin(newAdminEmail, newAdminRole)
                                newAdminEmail = ""
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePrimary)
                ) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Admin Dialog
    if (selectedAdminForAction != null) {
        val target = selectedAdminForAction!!
        val isSelf = target.email.equals(currentAdminEmail, ignoreCase = true)

        AlertDialog(
            onDismissRequest = { selectedAdminForAction = null },
            title = { Text("Modify Admin: ${target.email}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Role: ${target.role.displayName}", fontWeight = FontWeight.Bold)

                    if (!isSelf) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.adminToggleAdminActive(target.id)
                                    selectedAdminForAction = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (target.isActive) PulseError else PulseSuccess
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (target.isActive) "Disable Admin Account" else "Re-enable Admin Account")
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.adminDeleteAdmin(target.id)
                                    selectedAdminForAction = null
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PulseError),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Revoke & Remove Administrator")
                        }
                    } else {
                        Text("You cannot modify your own Super Admin access.", color = PulseError, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedAdminForAction = null }) { Text("Close") }
            }
        )
    }
}
