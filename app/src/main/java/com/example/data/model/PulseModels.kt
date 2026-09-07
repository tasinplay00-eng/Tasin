package com.example.data.model

import com.example.core.AdminRole

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val avatar: String,
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val likesCount: Int = 0,
    val isVerified: Boolean = false,
    val isPrivateAccount: Boolean = false,
    val isBanned: Boolean = false,
    val isSuspended: Boolean = false,
    val isFollowing: Boolean = false,
    val createdAt: String = "2026-09-01"
)

data class VideoItem(
    val id: String,
    val userId: String,
    val creatorUsername: String,
    val creatorDisplayName: String,
    val creatorAvatar: String,
    val isCreatorVerified: Boolean = false,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String = "",
    val description: String = "",
    val hashtags: List<String> = emptyList(),
    val soundId: String? = null,
    val soundTitle: String = "Original Sound - Pulse",
    val soundArtist: String = "Pulse Audio",
    val visibility: String = "public", // 'public', 'followers', 'private'
    val allowComments: Boolean = true,
    val allowDownload: Boolean = true,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowingCreator: Boolean = false,
    val moderationStatus: String = "approved", // 'approved', 'pending', 'rejected', 'hidden'
    val isFeatured: Boolean = false,
    val createdAt: String = "Just now"
)

data class CommentItem(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String,
    val isVerified: Boolean = false,
    val videoId: String,
    val text: String,
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: String = "2m ago"
)

data class SoundItem(
    val id: String,
    val title: String,
    val artist: String,
    val cover: String,
    val audioUrl: String,
    val usageCount: Int = 0,
    val isFeatured: Boolean = false,
    val isDisabled: Boolean = false,
    val createdAt: String = "2026-08-20"
)

data class HashtagItem(
    val id: String,
    val name: String,
    val usageCount: Int = 0,
    val isFeatured: Boolean = false,
    val isHidden: Boolean = false
)

data class NotificationItem(
    val id: String,
    val userId: String,
    val actorId: String? = null,
    val actorUsername: String = "Pulse User",
    val actorAvatar: String = "",
    val type: String, // 'follow', 'follow_request', 'like', 'comment', 'reply', 'mention', 'admin'
    val title: String,
    val message: String,
    val targetType: String? = null,
    val targetId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String = "10m ago"
)

data class ConversationItem(
    val id: String,
    val otherUserId: String,
    val otherUsername: String,
    val otherDisplayName: String,
    val otherAvatar: String,
    val isOtherVerified: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageTime: String = "1h ago",
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val mediaUrl: String? = null,
    val mediaType: String = "text", // 'text', 'image', 'video', 'profile_share'
    val isRead: Boolean = false,
    val createdAt: String = "12:45 PM",
    val isMine: Boolean = false
)

data class ReportItem(
    val id: String,
    val reporterId: String,
    val reporterUsername: String,
    val reportedUserId: String? = null,
    val reportedUsername: String? = null,
    val targetType: String, // 'video', 'user', 'comment', 'message'
    val targetId: String,
    val reason: String, // 'Spam', 'Harassment', 'Hate', 'Violence', 'Sexual content', 'Copyright', 'Other'
    val status: String = "pending", // 'pending', 'reviewing', 'resolved', 'rejected'
    val moderationNote: String = "",
    val resolvedBy: String? = null,
    val createdAt: String = "Today, 10:15 AM"
)

data class VerificationRequestItem(
    val id: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val avatar: String,
    val reason: String,
    val supportingInfo: String,
    val status: String = "pending", // 'pending', 'approved', 'rejected'
    val createdAt: String = "Today"
)

data class AdminUserItem(
    val id: String,
    val userId: String,
    val email: String,
    val role: AdminRole,
    val isActive: Boolean = true,
    val createdAt: String = "2026-08-15"
)

data class AdminAuditLog(
    val id: String,
    val adminId: String,
    val adminEmail: String,
    val action: String, // USER_BANNED, VIDEO_HIDDEN, SETTINGS_CHANGED, etc.
    val targetType: String,
    val targetId: String,
    val metadata: String = "{}",
    val createdAt: String = "Just now"
)

data class AppSettingsItem(
    val appName: String = "Pulse",
    val maintenanceMode: Boolean = false,
    val registrationEnabled: Boolean = true,
    val videoUploadEnabled: Boolean = true,
    val maxVideoDurationSeconds: Int = 180,
    val maxUploadSizeMb: Int = 100,
    val commentsEnabled: Boolean = true,
    val messagingEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true
)

data class AdminDashboardMetrics(
    val totalUsers: Int = 14820,
    val activeUsers: Int = 9240,
    val newUsersToday: Int = 312,
    val totalVideos: Int = 54890,
    val videosUploadedToday: Int = 415,
    val totalViews: Long = 1845000L,
    val totalLikes: Long = 720300L,
    val totalComments: Long = 89400L,
    val pendingReports: Int = 14,
    val verifiedUsers: Int = 185,
    val bannedUsers: Int = 42
)

data class LiveStreamInfo(
    val streamId: String,
    val title: String,
    val creatorUsername: String,
    val playbackUrl: String,
    val viewerCount: Int = 0,
    val isLive: Boolean = false
)
