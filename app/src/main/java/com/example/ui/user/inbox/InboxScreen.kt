package com.example.ui.user.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.ConversationItem
import com.example.data.model.NotificationItem
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(
    repository: PulseRepository,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Activities") } // "Activities" or "Messages"
    var activeConversation by remember { mutableStateOf<ConversationItem?>(null) }

    val notifications by repository.notifications.collectAsState()
    val conversations by repository.conversations.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (activeConversation != null) {
            // 1-on-1 Chat Screen View
            ChatConversationView(
                conversation = activeConversation!!,
                repository = repository,
                onBack = { activeConversation = null },
                onUserClick = onUserClick
            )
        } else {
            // Main Inbox View
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                listOf("Activities", "Messages").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Text(
                        text = tab,
                        fontSize = if (isSelected) 18.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            if (selectedTab == "Activities") {
                // Notifications List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationRow(notif = notif, onUserClick = onUserClick)
                    }
                }
            } else {
                // Direct Messages List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationRow(
                            conversation = conv,
                            onClick = { activeConversation = conv }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    notif: NotificationItem,
    onUserClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_item_${notif.id}")
    ) {
        if (notif.type == "admin") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PulsePrimary.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Admin Broadcast",
                    tint = PulsePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            PulseAvatar(
                imageUrl = notif.actorAvatar,
                size = 46.dp,
                onClick = { notif.actorId?.let(onUserClick) }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notif.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = notif.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notif.createdAt,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ConversationRow(
    conversation: ConversationItem,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_item_${conversation.id}")
    ) {
        PulseAvatar(
            imageUrl = conversation.otherAvatar,
            size = 48.dp,
            isVerified = conversation.isOtherVerified
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.otherDisplayName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conversation.lastMessageText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = conversation.lastMessageTime,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(PulsePrimary)
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatConversationView(
    conversation: ConversationItem,
    repository: PulseRepository,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allMessages by repository.messages.collectAsState()
    val messagesList = allMessages[conversation.id] ?: emptyList()
    var inputMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            PulseAvatar(
                imageUrl = conversation.otherAvatar,
                size = 38.dp,
                isVerified = conversation.isOtherVerified,
                onClick = { onUserClick(conversation.otherUserId) }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.otherDisplayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Active now",
                    fontSize = 11.sp,
                    color = PulseSecondary
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Messages Bubble List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messagesList, key = { it.id }) { msg ->
                ChatBubble(msg = msg)
            }
        }

        // Bottom Input Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            TextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Send a message...", fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if (inputMessage.isNotBlank()) {
                        IconButton(onClick = {
                            val text = inputMessage
                            inputMessage = ""
                            coroutineScope.launch {
                                repository.sendMessage(conversation.id, text)
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = PulsePrimary)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .testTag("chat_input_field")
            )
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (msg.isMine) 16.dp else 4.dp,
                        bottomEnd = if (msg.isMine) 4.dp else 16.dp
                    )
                )
                .background(if (msg.isMine) PulsePrimary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = msg.text,
                    color = if (msg.isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = msg.createdAt,
                    color = if (msg.isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
