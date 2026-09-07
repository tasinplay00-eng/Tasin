package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.model.VideoItem
import com.example.data.repository.PulseRepository
import com.example.ui.admin.admins.AdminManagementScreen
import com.example.ui.admin.audit.AdminAuditLogsScreen
import com.example.ui.admin.auth.AdminLoginScreen
import com.example.ui.admin.broadcast.AdminBroadcastScreen
import com.example.ui.admin.content.AdminContentLibraryScreen
import com.example.ui.admin.dashboard.AdminDashboardScreen
import com.example.ui.admin.reports.AdminReportsScreen
import com.example.ui.admin.settings.AdminAppSettingsScreen
import com.example.ui.admin.users.AdminUsersScreen
import com.example.ui.admin.verification.AdminVerificationScreen
import com.example.ui.admin.videos.AdminVideosScreen
import com.example.ui.theme.PulseDarkBorder
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulsePrimaryVariant
import com.example.ui.theme.PulseSecondary
import com.example.ui.theme.PulseTertiary
import com.example.ui.user.comments.CommentsSheet
import com.example.ui.user.create.CreateVideoScreen
import com.example.ui.user.discover.DiscoverScreen
import com.example.ui.user.feed.FeedScreen
import com.example.ui.user.inbox.InboxScreen
import com.example.ui.user.profile.ProfileScreen
import com.example.ui.user.settings.SettingsScreen

enum class PulseNavMode {
    USER_APP,
    ADMIN_LOGIN,
    ADMIN_PORTAL
}

enum class UserScreenTab {
    FEED,
    DISCOVER,
    CREATE,
    INBOX,
    PROFILE,
    SETTINGS
}

@Composable
fun PulseRootApp(
    repository: PulseRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(PulseNavMode.USER_APP) }
    var currentTab by remember { mutableStateOf(UserScreenTab.FEED) }
    var adminActiveSection by remember { mutableStateOf("dashboard") }

    // Navigation state
    var selectedProfileUserId by remember { mutableStateOf<String?>(null) }
    var activeCommentsVideoId by remember { mutableStateOf<String?>(null) }
    val adminRole by SessionManager.adminRole.collectAsState()

    Scaffold(
        bottomBar = {
            if (currentMode == PulseNavMode.USER_APP && currentTab != UserScreenTab.CREATE && currentTab != UserScreenTab.SETTINGS) {
                PulseBottomBar(
                    currentTab = currentTab,
                    onSelectTab = { tab ->
                        if (tab == UserScreenTab.PROFILE) {
                            selectedProfileUserId = null // Open own profile
                        }
                        currentTab = tab
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentMode) {
                PulseNavMode.USER_APP -> {
                    when (currentTab) {
                        UserScreenTab.FEED -> {
                            FeedScreen(
                                repository = repository,
                                onOpenComments = { activeCommentsVideoId = it },
                                onOpenShare = { video ->
                                    Toast.makeText(context, "Link copied to clipboard: https://pulse.social/v/${video.id}", Toast.LENGTH_SHORT).show()
                                },
                                onCreatorClick = { userId ->
                                    selectedProfileUserId = userId
                                    currentTab = UserScreenTab.PROFILE
                                },
                                onSoundClick = { soundId ->
                                    currentTab = UserScreenTab.DISCOVER
                                }
                            )
                        }
                        UserScreenTab.DISCOVER -> {
                            DiscoverScreen(
                                repository = repository,
                                onVideoClick = { video ->
                                    activeCommentsVideoId = video.id
                                },
                                onUserClick = { userId ->
                                    selectedProfileUserId = userId
                                    currentTab = UserScreenTab.PROFILE
                                },
                                onHashtagClick = { tag ->
                                    Toast.makeText(context, "Browsing #$tag", Toast.LENGTH_SHORT).show()
                                },
                                onSoundClick = { soundId ->
                                    Toast.makeText(context, "Selected Pulse sound track", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        UserScreenTab.CREATE -> {
                            CreateVideoScreen(
                                repository = repository,
                                onUploadSuccess = {
                                    Toast.makeText(context, "Video Published Successfully! 🎉", Toast.LENGTH_LONG).show()
                                    currentTab = UserScreenTab.FEED
                                },
                                onCancel = {
                                    currentTab = UserScreenTab.FEED
                                }
                            )
                        }
                        UserScreenTab.INBOX -> {
                            InboxScreen(
                                repository = repository,
                                onUserClick = { userId ->
                                    selectedProfileUserId = userId
                                    currentTab = UserScreenTab.PROFILE
                                }
                            )
                        }
                        UserScreenTab.PROFILE -> {
                            ProfileScreen(
                                userId = selectedProfileUserId,
                                repository = repository,
                                onVideoClick = { video ->
                                    activeCommentsVideoId = video.id
                                },
                                onNavigateToAdmin = {
                                    if (adminRole != AdminRole.NONE) {
                                        currentMode = PulseNavMode.ADMIN_PORTAL
                                    } else {
                                        currentMode = PulseNavMode.ADMIN_LOGIN
                                    }
                                },
                                onOpenSettings = {
                                    currentTab = UserScreenTab.SETTINGS
                                }
                            )
                        }
                        UserScreenTab.SETTINGS -> {
                            SettingsScreen(
                                onBack = { currentTab = UserScreenTab.PROFILE },
                                onLogout = {
                                    currentTab = UserScreenTab.FEED
                                    Toast.makeText(context, "Logged Out", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                PulseNavMode.ADMIN_LOGIN -> {
                    AdminLoginScreen(
                        repository = repository,
                        onLoginSuccess = {
                            currentMode = PulseNavMode.ADMIN_PORTAL
                            adminActiveSection = "dashboard"
                        },
                        onBackToUserApp = {
                            currentMode = PulseNavMode.USER_APP
                        }
                    )
                }

                PulseNavMode.ADMIN_PORTAL -> {
                    when (adminActiveSection) {
                        "dashboard" -> {
                            AdminDashboardScreen(
                                repository = repository,
                                onNavigateSection = { adminActiveSection = it },
                                onLogout = {
                                    SessionManager.setAdminRole(AdminRole.NONE)
                                    currentMode = PulseNavMode.USER_APP
                                },
                                onReturnToApp = {
                                    currentMode = PulseNavMode.USER_APP
                                }
                            )
                        }
                        "users" -> {
                            AdminUsersScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "videos" -> {
                            AdminVideosScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "reports" -> {
                            AdminReportsScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "verification" -> {
                            AdminVerificationScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "hashtags_sounds" -> {
                            AdminContentLibraryScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "broadcast" -> {
                            AdminBroadcastScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "admins" -> {
                            AdminManagementScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "settings" -> {
                            AdminAppSettingsScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                        "audit" -> {
                            AdminAuditLogsScreen(
                                repository = repository,
                                onBack = { adminActiveSection = "dashboard" }
                            )
                        }
                    }
                }
            }

            // Comments Bottom Sheet
            if (activeCommentsVideoId != null) {
                CommentsSheet(
                    videoId = activeCommentsVideoId!!,
                    repository = repository,
                    onDismiss = { activeCommentsVideoId = null },
                    onReportComment = { commentId ->
                        Toast.makeText(context, "Comment reported for review.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun PulseBottomBar(
    currentTab: UserScreenTab,
    onSelectTab: (UserScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFeed = currentTab == UserScreenTab.FEED
    val backgroundColor = if (isFeed) Color.Black.copy(alpha = 0.92f) else Color(0xFF07070A)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PulseDarkBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                icon = if (currentTab == UserScreenTab.FEED) Icons.Default.Home else Icons.Outlined.Home,
                isSelected = currentTab == UserScreenTab.FEED,
                isFeed = isFeed,
                onClick = { onSelectTab(UserScreenTab.FEED) },
                testTag = "tab_feed"
            )

            BottomNavItem(
                label = "Discover",
                icon = if (currentTab == UserScreenTab.DISCOVER) Icons.Default.Search else Icons.Outlined.Search,
                isSelected = currentTab == UserScreenTab.DISCOVER,
                isFeed = isFeed,
                onClick = { onSelectTab(UserScreenTab.DISCOVER) },
                testTag = "tab_discover"
            )

            // Center Accent Create Button (Immersive UI Shutter)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(50.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(PulsePrimary, PulseTertiary)))
                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable { onSelectTab(UserScreenTab.CREATE) }
                    .testTag("tab_create")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Video",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            BottomNavItem(
                label = "Inbox",
                icon = if (currentTab == UserScreenTab.INBOX) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                isSelected = currentTab == UserScreenTab.INBOX,
                isFeed = isFeed,
                onClick = { onSelectTab(UserScreenTab.INBOX) },
                testTag = "tab_inbox"
            )

            BottomNavItem(
                label = "Profile",
                icon = if (currentTab == UserScreenTab.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                isSelected = currentTab == UserScreenTab.PROFILE,
                isFeed = isFeed,
                onClick = { onSelectTab(UserScreenTab.PROFILE) },
                testTag = "tab_profile"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    isFeed: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = Color.White
    val inactiveColor = Color.White.copy(alpha = 0.55f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(PulsePrimary)
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}
