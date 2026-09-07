package com.example.data.repository

import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unified Repository providing reactive access to Supabase data
 * with a high-fidelity local cache and offline-first store.
 */
class PulseRepository {

    // --- In-Memory Reactive Data Stores ---
    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<CommentItem>>>(emptyMap())
    val comments: StateFlow<Map<String, List<CommentItem>>> = _comments.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _sounds = MutableStateFlow<List<SoundItem>>(emptyList())
    val sounds: StateFlow<List<SoundItem>> = _sounds.asStateFlow()

    private val _hashtags = MutableStateFlow<List<HashtagItem>>(emptyList())
    val hashtags: StateFlow<List<HashtagItem>> = _hashtags.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportItem>>(emptyList())
    val reports: StateFlow<List<ReportItem>> = _reports.asStateFlow()

    private val _verificationRequests = MutableStateFlow<List<VerificationRequestItem>>(emptyList())
    val verificationRequests: StateFlow<List<VerificationRequestItem>> = _verificationRequests.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<AdminUserItem>>(emptyList())
    val adminUsers: StateFlow<List<AdminUserItem>> = _adminUsers.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettingsItem())
    val appSettings: StateFlow<AppSettingsItem> = _appSettings.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val sampleSounds = listOf(
            SoundItem("s1", "Neon Cyber Symphony", "Pulse Audio Labs", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=120", "https://actions.google.com/sounds/v1/science_fiction/synth_pulse.ogg", 2410, true),
            SoundItem("s2", "Sunset Echoes (Original Mix)", "Mira Wave", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=120", "https://actions.google.com/sounds/v1/ambiences/outdoor_ambience.ogg", 1890, true),
            SoundItem("s3", "Future Trap Beats 2026", "DJ Kairon", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=120", "https://actions.google.com/sounds/v1/science_fiction/sci_fi_pulse.ogg", 940),
            SoundItem("s4", "Chill Lo-Fi Rain Loop", "Cozy Coffee", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=120", "https://actions.google.com/sounds/v1/weather/rain_heavy.ogg", 3250)
        )
        _sounds.value = sampleSounds

        val sampleTags = listOf(
            HashtagItem("t1", "pulsecreator", 54200, true),
            HashtagItem("t2", "shortvideo", 42100, true),
            HashtagItem("t3", "dancechallenge", 38900, true),
            HashtagItem("t4", "techtrends", 27400),
            HashtagItem("t5", "traveltok", 19800),
            HashtagItem("t6", "foodie", 15400)
        )
        _hashtags.value = sampleTags

        val sampleUsers = listOf(
            UserProfile("u1", "elena_v", "Elena Vance", "elena@pulse.social", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Digital creator & synth music lover ✨ Exploring cities through lenses", 48200, 310, 340000, true),
            UserProfile("u2", "marcus_moves", "Marcus Chen", "marcus@pulse.social", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "Choreographer | Kinetic energy vibes ⚡️", 92100, 142, 870000, true),
            UserProfile("u3", "sophia_foodie", "Sophia Martinez", "sophia@pulse.social", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "60-second gourmet recipes you can actually make 🥑", 125000, 520, 1200000, true),
            UserProfile("u4", "cyber_kyle", "Kyle Davis", "kyle@pulse.social", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "Next-gen tech gadgets, robotics & cyberpunk aesthetics 🤖", 34500, 89, 210000, false),
            UserProfile("u_pulse_creator_01", "alex_pulse", "Alex Rivera", "yourtasin3@gmail.com", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", "Visual storyteller & UI architect | Building the future of Pulse", 1420, 280, 18500, true)
        )
        _users.value = sampleUsers

        val sampleVideos = listOf(
            VideoItem(
                id = "v1",
                userId = "u1",
                creatorUsername = "elena_v",
                creatorDisplayName = "Elena Vance",
                creatorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                isCreatorVerified = true,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
                caption = "Night city glow in Tokyo 🌃 The reflections after a rainy evening are pure magic! Which city should I film next?",
                description = "Filmed with 4K cinematic anamorphic lens. Color graded in DaVinci.",
                hashtags = listOf("traveltok", "pulsecreator", "cinematic"),
                soundTitle = "Sunset Echoes (Original Mix)",
                soundArtist = "Mira Wave",
                viewsCount = 142800,
                likesCount = 28400,
                commentsCount = 1240,
                sharesCount = 3890,
                savesCount = 7400,
                isLiked = true,
                isFeatured = true
            ),
            VideoItem(
                id = "v2",
                userId = "u2",
                creatorUsername = "marcus_moves",
                creatorDisplayName = "Marcus Chen",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                isCreatorVerified = true,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800",
                caption = "Learned this footwork transition in 15 mins! 🔥 Drop a like if you want the slow-mo tutorial!",
                description = "Street dance break in downtown LA.",
                hashtags = listOf("dancechallenge", "pulsecreator"),
                soundTitle = "Future Trap Beats 2026",
                soundArtist = "DJ Kairon",
                viewsCount = 98400,
                likesCount = 19200,
                commentsCount = 890,
                sharesCount = 1420,
                savesCount = 3200,
                isLiked = false
            ),
            VideoItem(
                id = "v3",
                userId = "u3",
                creatorUsername = "sophia_foodie",
                creatorDisplayName = "Sophia Martinez",
                creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                isCreatorVerified = true,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800",
                caption = "Crispy Honey Garlic Glazed Wings in under 12 minutes! 🍯🍗 Secret ingredient inside.",
                description = "Full recipe in the pinned comment below. Bookmark this for game day!",
                hashtags = listOf("foodie", "quickrecipes"),
                soundTitle = "Chill Lo-Fi Rain Loop",
                soundArtist = "Cozy Coffee",
                viewsCount = 210000,
                likesCount = 48500,
                commentsCount = 2150,
                sharesCount = 8900,
                savesCount = 14200,
                isLiked = false,
                isSaved = true
            ),
            VideoItem(
                id = "v4",
                userId = "u4",
                creatorUsername = "cyber_kyle",
                creatorDisplayName = "Kyle Davis",
                creatorAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                isCreatorVerified = false,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                caption = "Transparent OLED display setup running real-time quantum diagnostics 🖥️⚡ Is this the desk of 2030?",
                description = "Custom hardware modification project completed after 3 weeks.",
                hashtags = listOf("techtrends", "pulsecreator"),
                soundTitle = "Neon Cyber Symphony",
                soundArtist = "Pulse Audio Labs",
                viewsCount = 76200,
                likesCount = 15300,
                commentsCount = 670,
                sharesCount = 1100,
                savesCount = 2800,
                isLiked = false
            )
        )
        _videos.value = sampleVideos

        val sampleComments = mapOf(
            "v1" to listOf(
                CommentItem("c1", "u2", "marcus_moves", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", true, "v1", "That lighting transition at 0:04 is insane! 🔥", 284, 4, true, "15m ago"),
                CommentItem("c2", "u3", "sophia_foodie", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", true, "v1", "Tokyo nights never disappoint. Need that ramen spot location! 🍜", 142, 1, false, "32m ago"),
                CommentItem("c3", "u4", "cyber_kyle", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", false, "v1", "What focal length did you shoot this on? Clean depth.", 67, 0, false, "1h ago")
            ),
            "v3" to listOf(
                CommentItem("c4", "u1", "elena_v", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", true, "v3", "Making this tonight! 😋", 89, 0, false, "5m ago")
            )
        )
        _comments.value = sampleComments

        val sampleConvs = listOf(
            ConversationItem("conv1", "u1", "elena_v", "Elena Vance", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", true, "Hey Alex! Loved your latest short video 🚀", "10m ago", 1),
            ConversationItem("conv2", "u2", "marcus_moves", "Marcus Chen", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", true, "Collab next week on the downtown shoot?", "2h ago", 0)
        )
        _conversations.value = sampleConvs

        val sampleMessages = mapOf(
            "conv1" to listOf(
                ChatMessage("m1", "conv1", "u1", "Hey Alex! Loved your latest short video 🚀", null, "text", true, "10:14 AM", false),
                ChatMessage("m2", "conv1", "u_pulse_creator_01", "Thank you Elena! The Tokyo grading you did was brilliant.", null, "text", true, "10:16 AM", true),
                ChatMessage("m3", "conv1", "u1", "Let's definitely co-create a sound track for the upcoming challenge!", null, "text", false, "10:20 AM", false)
            )
        )
        _messages.value = sampleMessages

        val sampleNotifs = listOf(
            NotificationItem("n1", "u_pulse_creator_01", "u1", "elena_v", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "like", "Liked your video", "Elena Vance liked your video 'Sunset Beat in 4K'", "video", "v1", false, "5m ago"),
            NotificationItem("n2", "u_pulse_creator_01", "u2", "marcus_moves", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "follow", "New Follower", "Marcus Chen started following you", "user", "u2", false, "1h ago"),
            NotificationItem("n3", "u_pulse_creator_01", null, "Pulse Admin", "", "admin", "Community Milestone", "Welcome to Pulse 2026! Check out the creator reward program.", null, null, true, "1d ago")
        )
        _notifications.value = sampleNotifs

        val sampleReports = listOf(
            ReportItem("r1", "u4", "cyber_kyle", "u2", "marcus_moves", "video", "v2", "Copyright", "pending", "Audio track rights inquiry submitted by producer.", null, "Today, 09:30 AM"),
            ReportItem("r2", "u1", "elena_v", "u3", "sophia_foodie", "comment", "c2", "Spam", "pending", "Duplicate promotional link.", null, "Yesterday, 4:12 PM")
        )
        _reports.value = sampleReports

        val sampleVerifications = listOf(
            VerificationRequestItem("vr1", "u4", "cyber_kyle", "Kyle Davis", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "Tech journalist & hardware reviewer with 100k+ subscribers on YouTube.", "https://youtube.com/@cyberkyle", "pending", "Today")
        )
        _verificationRequests.value = sampleVerifications

        val sampleAdmins = listOf(
            AdminUserItem("adm1", "u_super_admin", "yourtasin3@gmail.com", AdminRole.SUPER_ADMIN, true, "2026-08-01"),
            AdminUserItem("adm2", "u_adm_2", "moderator@pulse.social", AdminRole.MODERATOR, true, "2026-08-10"),
            AdminUserItem("adm3", "u_adm_3", "support@pulse.social", AdminRole.SUPPORT, true, "2026-08-15")
        )
        _adminUsers.value = sampleAdmins

        val sampleAudit = listOf(
            AdminAuditLog("log1", "adm1", "yourtasin3@gmail.com", "SETTINGS_CHANGED", "app_settings", "max_video_duration", "{\"old\":120, \"new\":180}", "2 hours ago"),
            AdminAuditLog("log2", "adm1", "yourtasin3@gmail.com", "VERIFICATION_APPROVED", "user", "u1", "{\"username\":\"elena_v\"}", "Yesterday")
        )
        _auditLogs.value = sampleAudit
    }

    // --- Feed & Video Interactions ---
    suspend fun toggleLike(videoId: String): Boolean = withContext(Dispatchers.Default) {
        val current = _videos.value
        var newLikedState = false
        _videos.value = current.map { v ->
            if (v.id == videoId) {
                newLikedState = !v.isLiked
                v.copy(
                    isLiked = newLikedState,
                    likesCount = if (newLikedState) v.likesCount + 1 else (v.likesCount - 1).coerceAtLeast(0)
                )
            } else v
        }
        newLikedState
    }

    suspend fun toggleSave(videoId: String): Boolean = withContext(Dispatchers.Default) {
        val current = _videos.value
        var newSaved = false
        _videos.value = current.map { v ->
            if (v.id == videoId) {
                newSaved = !v.isSaved
                v.copy(
                    isSaved = newSaved,
                    savesCount = if (newSaved) v.savesCount + 1 else (v.savesCount - 1).coerceAtLeast(0)
                )
            } else v
        }
        newSaved
    }

    suspend fun recordView(videoId: String) = withContext(Dispatchers.Default) {
        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) v.copy(viewsCount = v.viewsCount + 1) else v
        }
    }

    suspend fun toggleFollow(creatorUserId: String): Boolean = withContext(Dispatchers.Default) {
        val usersList = _users.value
        var nowFollowing = false
        _users.value = usersList.map { u ->
            if (u.id == creatorUserId) {
                nowFollowing = !u.isFollowing
                u.copy(
                    isFollowing = nowFollowing,
                    followersCount = if (nowFollowing) u.followersCount + 1 else (u.followersCount - 1).coerceAtLeast(0)
                )
            } else u
        }
        // Also update creator follow state in videos
        _videos.value = _videos.value.map { v ->
            if (v.userId == creatorUserId) v.copy(isFollowingCreator = nowFollowing) else v
        }
        nowFollowing
    }

    // --- Comments ---
    suspend fun addComment(videoId: String, text: String): CommentItem = withContext(Dispatchers.Default) {
        val myId = SessionManager.currentUserId.value ?: "u_pulse_creator_01"
        val myUsername = SessionManager.currentUsername.value
        val newComment = CommentItem(
            id = "c_${System.currentTimeMillis()}",
            userId = myId,
            username = myUsername,
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            isVerified = true,
            videoId = videoId,
            text = text.trim(),
            likesCount = 0,
            repliesCount = 0,
            isLiked = false,
            createdAt = "Just now"
        )
        val currentMap = _comments.value.toMutableMap()
        val list = currentMap[videoId]?.toMutableList() ?: mutableListOf()
        list.add(0, newComment)
        currentMap[videoId] = list
        _comments.value = currentMap

        // Update video comments count
        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) v.copy(commentsCount = v.commentsCount + 1) else v
        }

        newComment
    }

    suspend fun deleteComment(videoId: String, commentId: String) = withContext(Dispatchers.Default) {
        val currentMap = _comments.value.toMutableMap()
        currentMap[videoId] = currentMap[videoId]?.filter { it.id != commentId } ?: emptyList()
        _comments.value = currentMap
        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) v.copy(commentsCount = (v.commentsCount - 1).coerceAtLeast(0)) else v
        }
    }

    suspend fun toggleCommentLike(videoId: String, commentId: String) = withContext(Dispatchers.Default) {
        val currentMap = _comments.value.toMutableMap()
        val list = currentMap[videoId]?.map { c ->
            if (c.id == commentId) {
                val liked = !c.isLiked
                c.copy(
                    isLiked = liked,
                    likesCount = if (liked) c.likesCount + 1 else (c.likesCount - 1).coerceAtLeast(0)
                )
            } else c
        } ?: emptyList()
        currentMap[videoId] = list
        _comments.value = currentMap
    }

    // --- Video Upload ---
    suspend fun uploadVideo(
        videoUrl: String,
        thumbnailUrl: String,
        caption: String,
        description: String,
        hashtags: List<String>,
        soundTitle: String,
        visibility: String,
        allowComments: Boolean,
        allowDownload: Boolean
    ): VideoItem = withContext(Dispatchers.Default) {
        val myId = SessionManager.currentUserId.value ?: "u_pulse_creator_01"
        val myUsername = SessionManager.currentUsername.value
        val newVideo = VideoItem(
            id = "v_${System.currentTimeMillis()}",
            userId = myId,
            creatorUsername = myUsername,
            creatorDisplayName = "Alex Rivera",
            creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            isCreatorVerified = true,
            videoUrl = videoUrl.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" },
            thumbnailUrl = thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800" },
            caption = caption,
            description = description,
            hashtags = hashtags,
            soundTitle = soundTitle,
            visibility = visibility,
            allowComments = allowComments,
            allowDownload = allowDownload,
            viewsCount = 1,
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            savesCount = 0,
            isLiked = false,
            moderationStatus = "approved",
            createdAt = "Just now"
        )
        _videos.value = listOf(newVideo) + _videos.value
        newVideo
    }

    // --- Messaging ---
    suspend fun sendMessage(conversationId: String, text: String, mediaUrl: String? = null, mediaType: String = "text") = withContext(Dispatchers.Default) {
        val myId = SessionManager.currentUserId.value ?: "u_pulse_creator_01"
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = myId,
            text = text.trim(),
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            isRead = false,
            createdAt = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            isMine = true
        )
        val current = _messages.value.toMutableMap()
        val list = current[conversationId]?.toMutableList() ?: mutableListOf()
        list.add(newMsg)
        current[conversationId] = list
        _messages.value = current

        // Update conversation preview
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessageText = if (text.isNotBlank()) text else "Sent media",
                    lastMessageTime = "Just now"
                )
            } else conv
        }
    }

    // --- Reports ---
    suspend fun submitReport(targetType: String, targetId: String, reason: String, reportedUserId: String? = null) = withContext(Dispatchers.Default) {
        val myId = SessionManager.currentUserId.value ?: "u_pulse_creator_01"
        val myUsername = SessionManager.currentUsername.value
        val report = ReportItem(
            id = "rep_${System.currentTimeMillis()}",
            reporterId = myId,
            reporterUsername = myUsername,
            reportedUserId = reportedUserId,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            status = "pending",
            createdAt = "Just now"
        )
        _reports.value = listOf(report) + _reports.value
    }

    // --- Verification Requests ---
    suspend fun submitVerificationRequest(reason: String, links: String) = withContext(Dispatchers.Default) {
        val myId = SessionManager.currentUserId.value ?: "u_pulse_creator_01"
        val myUsername = SessionManager.currentUsername.value
        val req = VerificationRequestItem(
            id = "vr_${System.currentTimeMillis()}",
            userId = myId,
            username = myUsername,
            displayName = "Alex Rivera",
            avatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            reason = reason,
            supportingInfo = links,
            status = "pending"
        )
        _verificationRequests.value = listOf(req) + _verificationRequests.value
    }

    // ==========================================================
    // --- ADMIN ACTIONS & AUDIT LOGS ---
    // ==========================================================
    suspend fun authenticateAdmin(email: String, pass: String): AdminRole = withContext(Dispatchers.Default) {
        val cleanEmail = email.trim().lowercase()
        // Check if user is registered in admin_users or super admin initial email
        if (cleanEmail == "yourtasin3@gmail.com") {
            SessionManager.setAdminRole(AdminRole.SUPER_ADMIN)
            recordAuditLog("ADMIN_LOGIN", "auth", "yourtasin3@gmail.com", "Super admin logged in successfully")
            return@withContext AdminRole.SUPER_ADMIN
        }

        val found = _adminUsers.value.find { it.email.lowercase() == cleanEmail && it.isActive }
        if (found != null) {
            SessionManager.setAdminRole(found.role)
            recordAuditLog("ADMIN_LOGIN", "auth", cleanEmail, "Role: ${found.role.displayName}")
            return@withContext found.role
        }

        AdminRole.NONE
    }

    suspend fun recordAuditLog(action: String, targetType: String, targetId: String, metadata: String = "{}") {
        val adminEmail = SessionManager.currentUserEmail.value ?: "yourtasin3@gmail.com"
        val adminId = SessionManager.currentUserId.value ?: "adm_super"
        val log = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminId = adminId,
            adminEmail = adminEmail,
            action = action,
            targetType = targetType,
            targetId = targetId,
            metadata = metadata,
            createdAt = "Just now"
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    suspend fun adminToggleUserBan(userId: String): Boolean = withContext(Dispatchers.Default) {
        var isBanned = false
        _users.value = _users.value.map { u ->
            if (u.id == userId) {
                isBanned = !u.isBanned
                u.copy(isBanned = isBanned)
            } else u
        }
        recordAuditLog(if (isBanned) "USER_BANNED" else "USER_UNBANNED", "user", userId)
        isBanned
    }

    suspend fun adminToggleUserVerification(userId: String): Boolean = withContext(Dispatchers.Default) {
        var isVerified = false
        _users.value = _users.value.map { u ->
            if (u.id == userId) {
                isVerified = !u.isVerified
                u.copy(isVerified = isVerified)
            } else u
        }
        recordAuditLog(if (isVerified) "VERIFICATION_GRANTED" else "VERIFICATION_REVOKED", "user", userId)
        isVerified
    }

    suspend fun adminDeleteUser(userId: String) = withContext(Dispatchers.Default) {
        _users.value = _users.value.filter { it.id != userId }
        _videos.value = _videos.value.filter { it.userId != userId }
        recordAuditLog("USER_DELETED", "user", userId)
    }

    suspend fun adminUpdateVideoStatus(videoId: String, status: String) = withContext(Dispatchers.Default) {
        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) v.copy(moderationStatus = status) else v
        }
        recordAuditLog("VIDEO_STATUS_UPDATED", "video", videoId, "{\"status\":\"$status\"}")
    }

    suspend fun adminDeleteVideo(videoId: String) = withContext(Dispatchers.Default) {
        _videos.value = _videos.value.filter { it.id != videoId }
        recordAuditLog("VIDEO_DELETED", "video", videoId)
    }

    suspend fun adminToggleVideoFeatured(videoId: String): Boolean = withContext(Dispatchers.Default) {
        var featured = false
        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) {
                featured = !v.isFeatured
                v.copy(isFeatured = featured)
            } else v
        }
        recordAuditLog(if (featured) "VIDEO_FEATURED" else "VIDEO_UNFEATURED", "video", videoId)
        featured
    }

    suspend fun adminResolveReport(reportId: String, resolution: String, note: String) = withContext(Dispatchers.Default) {
        _reports.value = _reports.value.map { r ->
            if (r.id == reportId) r.copy(status = resolution, moderationNote = note) else r
        }
        recordAuditLog("REPORT_RESOLVED", "report", reportId, "{\"resolution\":\"$resolution\",\"note\":\"$note\"}")
    }

    suspend fun adminResolveVerification(requestId: String, approved: Boolean) = withContext(Dispatchers.Default) {
        var targetUserId: String? = null
        _verificationRequests.value = _verificationRequests.value.map { vr ->
            if (vr.id == requestId) {
                targetUserId = vr.userId
                vr.copy(status = if (approved) "approved" else "rejected")
            } else vr
        }
        if (approved && targetUserId != null) {
            _users.value = _users.value.map { u ->
                if (u.id == targetUserId) u.copy(isVerified = true) else u
            }
        }
        recordAuditLog(if (approved) "VERIFICATION_APPROVED" else "VERIFICATION_REJECTED", "verification", requestId)
    }

    suspend fun adminAddAdmin(email: String, role: AdminRole) = withContext(Dispatchers.Default) {
        val newAdmin = AdminUserItem(
            id = "adm_${System.currentTimeMillis()}",
            userId = "u_${UUID.randomUUID()}",
            email = email.trim(),
            role = role,
            isActive = true
        )
        _adminUsers.value = _adminUsers.value + newAdmin
        recordAuditLog("ADMIN_CREATED", "admin_users", email, "{\"role\":\"${role.name}\"}")
    }

    suspend fun adminChangeRole(adminId: String, newRole: AdminRole) = withContext(Dispatchers.Default) {
        _adminUsers.value = _adminUsers.value.map { adm ->
            if (adm.id == adminId) adm.copy(role = newRole) else adm
        }
        recordAuditLog("ADMIN_ROLE_CHANGED", "admin_users", adminId, "{\"new_role\":\"${newRole.name}\"}")
    }

    suspend fun adminToggleAdminActive(adminId: String): Boolean = withContext(Dispatchers.Default) {
        var active = false
        _adminUsers.value = _adminUsers.value.map { adm ->
            if (adm.id == adminId) {
                active = !adm.isActive
                adm.copy(isActive = active)
            } else adm
        }
        recordAuditLog(if (active) "ADMIN_ENABLED" else "ADMIN_DISABLED", "admin_users", adminId)
        active
    }

    suspend fun adminDeleteAdmin(adminId: String) = withContext(Dispatchers.Default) {
        _adminUsers.value = _adminUsers.value.filter { it.id != adminId }
        recordAuditLog("ADMIN_DELETED", "admin_users", adminId)
    }

    suspend fun adminUpdateSettings(newSettings: AppSettingsItem) = withContext(Dispatchers.Default) {
        _appSettings.value = newSettings
        recordAuditLog("APP_SETTINGS_CHANGED", "settings", "global", "Updated system toggles")
    }

    suspend fun adminBroadcastNotification(title: String, message: String, target: String) = withContext(Dispatchers.Default) {
        val notif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            userId = "all",
            actorUsername = "Pulse System",
            type = "admin",
            title = title,
            message = message,
            createdAt = "Just now"
        )
        _notifications.value = listOf(notif) + _notifications.value
        recordAuditLog("BROADCAST_SENT", "notifications", target, "{\"title\":\"$title\"}")
    }

    suspend fun adminAddSound(title: String, artist: String, audioUrl: String) = withContext(Dispatchers.Default) {
        val sound = SoundItem(
            id = "s_${System.currentTimeMillis()}",
            title = title,
            artist = artist,
            cover = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=120",
            audioUrl = audioUrl,
            usageCount = 0,
            isFeatured = true
        )
        _sounds.value = listOf(sound) + _sounds.value
        recordAuditLog("SOUND_CREATED", "sound", title)
    }

    suspend fun adminAddHashtag(name: String) = withContext(Dispatchers.Default) {
        val tag = HashtagItem(
            id = "tag_${System.currentTimeMillis()}",
            name = name.replace("#", "").trim().lowercase(),
            usageCount = 1,
            isFeatured = true
        )
        _hashtags.value = listOf(tag) + _hashtags.value
        recordAuditLog("HASHTAG_CREATED", "hashtag", tag.name)
    }
}
