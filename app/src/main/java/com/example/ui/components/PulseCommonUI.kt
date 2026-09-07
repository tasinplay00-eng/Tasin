package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun PulseAvatar(
    imageUrl: String,
    size: Dp = 48.dp,
    isVerified: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        AsyncImage(
            model = imageUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150" },
            contentDescription = "Avatar",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
            contentScale = ContentScale.Crop
        )
        if (isVerified) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified Creator",
                tint = PulseSecondary,
                modifier = Modifier
                    .size(size * 0.38f)
                    .align(Alignment.BottomEnd)
                    .background(Color.Black, CircleShape)
                    .padding(1.dp)
            )
        }
    }
}

@Composable
fun PulseGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(listOf(PulsePrimary, PulseTertiary)),
                shape = RoundedCornerShape(14.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .heightIn(min = 48.dp)
            .testTag("pulse_gradient_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun PulseFollowButton(
    isFollowing: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgModifier = if (isFollowing) {
        Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
    } else {
        Modifier
            .background(
                Brush.horizontalGradient(listOf(PulsePrimary, PulsePrimaryVariant)),
                RoundedCornerShape(10.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(bgModifier)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(
            text = if (isFollowing) "Following" else "Follow",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun PulseBadge(
    text: String,
    color: Color = PulsePrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PulseGlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(PulseDarkCard.copy(alpha = 0.75f))
            .border(1.dp, PulseDarkBorder, shape)
            .padding(16.dp),
        content = content
    )
}
