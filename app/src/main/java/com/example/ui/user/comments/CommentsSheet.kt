package com.example.ui.user.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.SessionManager
import com.example.data.model.CommentItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulsePrimary
import com.example.ui.user.feed.formatCount
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    videoId: String,
    repository: PulseRepository,
    onDismiss: () -> Unit,
    onReportComment: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val commentsMap by repository.comments.collectAsState()
    val commentsList = commentsMap[videoId] ?: emptyList()
    var commentInput by remember { mutableStateOf("") }
    val myUserId by SessionManager.currentUserId.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) },
        modifier = Modifier.fillMaxHeight(0.75f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "${commentsList.size} comments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Comments List
            if (commentsList.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Be the first to comment!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp)
                ) {
                    items(commentsList, key = { it.id }) { comment ->
                        CommentRow(
                            comment = comment,
                            isOwn = comment.userId == myUserId,
                            onLike = {
                                coroutineScope.launch { repository.toggleCommentLike(videoId, comment.id) }
                            },
                            onDelete = {
                                coroutineScope.launch { repository.deleteComment(videoId, comment.id) }
                            },
                            onReport = { onReportComment(comment.id) }
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Input Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                PulseAvatar(
                    imageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                    size = 36.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                TextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = { Text("Add a nice comment...", fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        if (commentInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val text = commentInput
                                    commentInput = ""
                                    coroutineScope.launch {
                                        repository.addComment(videoId, text)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Post Comment",
                                    tint = PulsePrimary
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("comment_input_field")
                )
            }
        }
    }
}

@Composable
fun CommentRow(
    comment: CommentItem,
    isOwn: Boolean,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("comment_item_${comment.id}")
    ) {
        PulseAvatar(
            imageUrl = comment.userAvatar,
            size = 36.dp,
            isVerified = comment.isVerified
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.username,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.createdAt,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Reply",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isOwn) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PulsePrimary,
                        modifier = Modifier.clickable { onDelete() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Report",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onReport() }
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onLike() }
        ) {
            Icon(
                imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like Comment",
                tint = if (comment.isLiked) PulsePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            if (comment.likesCount > 0) {
                Text(
                    text = formatCount(comment.likesCount),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
