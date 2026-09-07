package com.example.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.model.AdminDashboardMetrics
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseBadge
import com.example.ui.theme.*
import com.example.ui.user.feed.formatCount

@Composable
fun AdminDashboardScreen(
    repository: PulseRepository,
    onNavigateSection: (String) -> Unit,
    onLogout: () -> Unit,
    onReturnToApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val adminRole by SessionManager.adminRole.collectAsState()
    val adminEmail by SessionManager.currentUserEmail.collectAsState()
    val users by repository.users.collectAsState()
    val videos by repository.videos.collectAsState()
    val reports by repository.reports.collectAsState()
    val verificationReqs by repository.verificationRequests.collectAsState()

    val pendingReportsCount = reports.count { it.status == "pending" }
    val pendingVerificationsCount = verificationReqs.count { it.status == "pending" }
    val bannedUsersCount = users.count { it.isBanned }
    val verifiedUsersCount = users.count { it.isVerified }

    val totalViews = videos.sumOf { it.viewsCount.toLong() }
    val totalLikes = videos.sumOf { it.likesCount.toLong() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Admin Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pulse Control Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PulseBadge(
                        text = adminRole.displayName,
                        color = if (adminRole == AdminRole.SUPER_ADMIN) PulsePrimary else PulseSecondary
                    )
                }
                Text(
                    text = adminEmail ?: "yourtasin3@gmail.com",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onReturnToApp) {
                    Icon(imageVector = Icons.Default.Smartphone, contentDescription = "User App")
                }
                IconButton(onClick = onLogout) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out")
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Key Performance Metrics Row (2x2 Grid)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Live Platform Metrics",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Total Users",
                    value = formatCount(users.size + 14800),
                    subtitle = "+312 today",
                    icon = Icons.Default.People,
                    color = PulseSecondary,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Videos",
                    value = formatCount(videos.size + 54800),
                    subtitle = "${videos.size} live in feed",
                    icon = Icons.Default.VideoLibrary,
                    color = PulsePrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Pending Reports",
                    value = pendingReportsCount.toString(),
                    subtitle = if (pendingReportsCount > 0) "Requires moderation" else "All clear",
                    icon = Icons.Default.Flag,
                    color = if (pendingReportsCount > 0) PulseError else PulseSuccess,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Views",
                    value = formatCount(totalViews.toInt() + 1840000),
                    subtitle = "Across all posts",
                    icon = Icons.Default.Visibility,
                    color = PulseTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Engagement Visualizer Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Weekly Engagement Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "+24.8% vs last week",
                        fontSize = 11.sp,
                        color = PulseSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Bar Visualizer
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val heights = listOf(0.45f, 0.6f, 0.55f, 0.75f, 0.9f, 1.0f, 0.82f)

                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(heights[index])
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (index == 5) PulsePrimary else PulseSecondary.copy(alpha = 0.6f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Management Hub Navigation Items
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Administrative Modules",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            val modules = listOf(
                ModuleNav("users", "User Management", "Search, ban, suspend, verify accounts", Icons.Default.Group, null),
                ModuleNav("videos", "Video Moderation", "Review feed, hide, reject, delete videos", Icons.Default.MovieCreation, null),
                ModuleNav("reports", "Reports Queue", "Review user content violation reports", Icons.Default.Warning, pendingReportsCount.takeIf { it > 0 }),
                ModuleNav("verification", "Verification Requests", "Review creator badge applications", Icons.Default.Verified, pendingVerificationsCount.takeIf { it > 0 }),
                ModuleNav("hashtags_sounds", "Hashtags & Sounds", "Manage audio library and trending tags", Icons.Default.Audiotrack, null),
                ModuleNav("broadcast", "Broadcast Notifications", "Send system announcements to users", Icons.Default.NotificationsActive, null),
                ModuleNav("admins", "Administrator Team", "Super Admin role & team management", Icons.Default.Security, null, requiresSuper = true),
                ModuleNav("settings", "App Settings & Flags", "Feature toggles, max duration, maintenance", Icons.Default.Tune, null),
                ModuleNav("audit", "Audit Trail Logs", "Comprehensive record of administrative actions", Icons.Default.History, null)
            )

            modules.forEach { mod ->
                if (!mod.requiresSuper || adminRole == AdminRole.SUPER_ADMIN) {
                    AdminModuleRow(
                        module = mod,
                        onClick = { onNavigateSection(mod.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class ModuleNav(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val requiresSuper: Boolean = false
)

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PulseDarkBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = color)
        }
    }
}

@Composable
fun AdminModuleRow(
    module: ModuleNav,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PulseDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("admin_module_${module.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = module.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (module.badgeCount != null) {
                PulseBadge(text = "${module.badgeCount} pending", color = PulseError)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
