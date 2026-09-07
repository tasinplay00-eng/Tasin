package com.example.ui.admin.users

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
import com.example.data.model.UserProfile
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.components.PulseBadge
import com.example.ui.theme.PulseError
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseSuccess
import com.example.ui.user.feed.formatCount
import kotlinx.coroutines.launch

@Composable
fun AdminUsersScreen(
    repository: PulseRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val users by repository.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForAction by remember { mutableStateOf<UserProfile?>(null) }

    val filteredUsers = remember(searchQuery, users) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.username.contains(searchQuery.trim(), ignoreCase = true) ||
            it.displayName.contains(searchQuery.trim(), ignoreCase = true) ||
            (it.email?.contains(searchQuery.trim(), ignoreCase = true) == true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
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
                text = "User Accounts Management",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users by username, name, or email...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_users_search_input")
        )

        // Users List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers, key = { it.id }) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedUserForAction = user }
                        .testTag("admin_user_card_${user.username}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        PulseAvatar(imageUrl = user.avatar, size = 48.dp, isVerified = user.isVerified)
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (user.isBanned) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PulseBadge(text = "BANNED", color = PulseError)
                                }
                            }
                            Text(
                                text = "@${user.username} • ${user.email ?: "No email"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatCount(user.followersCount)} followers • ${formatCount(user.likesCount)} likes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = { selectedUserForAction = user }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                    }
                }
            }
        }
    }

    // User Action Bottom Sheet Dialog
    if (selectedUserForAction != null) {
        val target = selectedUserForAction!!
        AlertDialog(
            onDismissRequest = { selectedUserForAction = null },
            title = { Text("Manage @${target.username}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User ID: ${target.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminToggleUserVerification(target.id)
                                selectedUserForAction = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PulseSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (target.isVerified) "Revoke Verification" else "Grant Blue Checkmark Verification", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminToggleUserBan(target.id)
                                selectedUserForAction = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (target.isBanned) PulseSuccess else PulseError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (target.isBanned) "Unban User Account" else "Ban User Account")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.adminDeleteUser(target.id)
                                selectedUserForAction = null
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PulseError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permanently Delete User & Content")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedUserForAction = null }) { Text("Close") }
            }
        )
    }
}
