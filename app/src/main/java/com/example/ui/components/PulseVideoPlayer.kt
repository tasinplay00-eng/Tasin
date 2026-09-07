package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary

private const val TAG = "PulseVideoPlayer"

@Composable
fun PulseVideoSurface(
    video: VideoItem,
    isActive: Boolean,
    onDoubleTapLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    val heartScale by animateFloatAsState(
        targetValue = if (showHeartAnimation) 1.4f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        finishedListener = { showHeartAnimation = false }
    )

    // Safely cleanup and release media resources on dispose or video switch
    DisposableEffect(video.id) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
                videoViewRef?.suspend()
            } catch (e: Exception) {
                Log.w(TAG, "Error cleaning up VideoView: ${e.message}")
            }
            videoViewRef = null
            mediaPlayerRef = null
            isPrepared = false
            isBuffering = false
        }
    }

    // Handle play / pause based on active visibility and preparation state
    LaunchedEffect(isActive, isPrepared) {
        val vv = videoViewRef
        if (vv != null && isPrepared && !hasError) {
            try {
                if (isActive) {
                    vv.start()
                    isPlaying = true
                } else {
                    vv.pause()
                    isPlaying = false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Playback state transition failed: ${e.message}")
            }
        }
    }

    // Handle mute state reactively
    LaunchedEffect(isMuted) {
        try {
            mediaPlayerRef?.let { mp ->
                if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
            }
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(video.id) {
                detectTapGestures(
                    onDoubleTap = {
                        showHeartAnimation = true
                        onDoubleTapLike()
                    },
                    onTap = {
                        if (hasError) {
                            // Retry loading
                            hasError = false
                            isBuffering = true
                            try {
                                videoViewRef?.setVideoURI(Uri.parse(video.videoUrl))
                            } catch (_: Exception) {}
                        } else {
                            val vv = videoViewRef
                            if (vv != null && isPrepared) {
                                try {
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                } catch (_: Exception) {
                                    isPlaying = !isPlaying
                                }
                            } else {
                                isPlaying = !isPlaying
                            }
                        }
                    }
                )
            }
            .testTag("video_player_surface")
    ) {
        // Thumbnail poster as fallback / loading placeholder
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Native Android Video Surface (safely initialized)
        if (isActive && video.videoUrl.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        // Crucial: Set error listener BEFORE setting URI to catch asynchronous native errors
                        setOnErrorListener { _, what, extra ->
                            Log.w(TAG, "Native MediaPlayer error handled: what=$what, extra=$extra")
                            hasError = true
                            isBuffering = false
                            true // Handled: suppresses native popup & crash
                        }
                        setOnPreparedListener { mp ->
                            mediaPlayerRef = mp
                            isPrepared = true
                            isBuffering = false
                            hasError = false
                            try {
                                mp.isLooping = true
                                if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                                if (isActive) {
                                    start()
                                    isPlaying = true
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error starting playback in onPrepared", e)
                            }
                        }
                        setOnCompletionListener {
                            if (isActive) {
                                try {
                                    seekTo(0)
                                    start()
                                } catch (_: Exception) {}
                            }
                        }
                        try {
                            setVideoURI(Uri.parse(video.videoUrl))
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to set video URI", e)
                            hasError = true
                            isBuffering = false
                        }
                        videoViewRef = this
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient overlays for readable text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
        )

        // Buffering Indicator
        if (isBuffering && !hasError) {
            CircularProgressIndicator(
                color = PulsePrimary,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
            )
        }

        // Preview Mode indicator if network or codec stream is unavailable
        if (hasError) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 52.dp, start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = null,
                        tint = PulsePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pulse Preview",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Mute / Unmute Button in Top Corner
        IconButton(
            onClick = {
                isMuted = !isMuted
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .size(36.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center Pause Indicator
        AnimatedVisibility(
            visible = !isPlaying && !isBuffering,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Double-Tap Animated Floating Heart
        if (heartScale > 0f) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = PulsePrimary,
                modifier = Modifier
                    .size(110.dp)
                    .scale(heartScale)
                    .align(Alignment.Center)
            )
        }
    }
}

